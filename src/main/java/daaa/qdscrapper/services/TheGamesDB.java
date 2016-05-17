package daaa.qdscrapper.services;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;

import daaa.qdscrapper.Args;
import daaa.qdscrapper.Props;
import daaa.qdscrapper.model.Game;
import daaa.qdscrapper.utils.QDUtils;
import daaa.qdscrapper.utils.QDUtils.HttpAnswer;
import daaa.qdscrapper.utils.RomCleaner;

/**
 * Utilities to query http://thegamesdb.net/ using its API
 * 
 * @author daaa
 *
 */
public class TheGamesDB
{
	private TheGamesDB(){} // do not instanciate
	
	private static final String URL_GAMESDB_API = Props.get("thegamesdb.url");//"http://thegamesdb.net/api/"; 
	private static final String GET_GAME = "GetGame.php";
	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
	private static final String DUPE_IMAGES_FOLDER = Props.get("dupe.images.folder");
	private static final String THEGAMESDB_API_ID = "TheGamesDB";
	
	
	
	/**
	 * Builds the 'getGame' api url
	 * @param name name of the game
	 * @param platform optional platform (ES platform name)
	 * @return the xml result from thegamesdb
	 * @throws URISyntaxException
	 */
	private static String buildGetGame(String name, String platform) throws URISyntaxException
	{
		URIBuilder uri = new URIBuilder(URL_GAMESDB_API + GET_GAME);
		uri.addParameter("name", name);
		String p = Platform.asTheGamesDB(platform);
		if(StringUtils.isNotEmpty(p))
		{
			uri.addParameter("platform", p);
		}
		
		return uri.toString();
	}
	
	/**
	 * Searches a game on thegamesdb, return its xml answer
	 * @param name the name of the game to search
	 * @param platform the platform filter
	 * @param args the app's arguments (uses the platform)
	 * @return the xml answer from thegamesdb
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static String searchXml(String name, String platform, Args args)
	throws ClientProtocolException, IOException, URISyntaxException
	{
		String url = buildGetGame(name, platform);
		HttpAnswer answer = QDUtils.httpGet(args, url);
		if(answer.getCode() != HttpStatus.SC_OK)
		{
			System.err.println("Error querying TheGamesDB (code "+answer.getCode()+") with url " + url);
			return null;
		}
		return answer.getContent();
	}
	
	
	
	/**
	 * Builds a unique filename for an image
	 * @param name name of the game
	 * @param matchIndex index of the match
	 * @param imageType extension for the image (png/jpg)
	 * @return a unique filename for this run
	 */
	private static String buildFileName(String name, int matchIndex, String imageType)
	{
		return QDUtils.sanitizeFilename(name) + "-daaa-" + matchIndex + "-img" + (imageType == null ? "" : ("." + imageType));
	}
	
	/**
	 * Downloads the boxart from thegamesdb
	 * @param name name of the game
	 * @param imageUrl relative url of the boxart
	 * @param imageBaseUrl base url of all images on thegamesdb
	 * @param matchIndex the current match index
	 * @param args app's arguments
	 * @return the name of the downloaded image (not the path, just the file name)
	 * @throws Exception
	 */
	private static String downloadImage(String name, String imageUrl, String imageBaseUrl, int matchIndex, Args args) 
	throws Exception
	{
		HttpClient httpclient = QDUtils.getHttpClient(args);
		String uri = imageBaseUrl + imageUrl;
		HttpGet httpGet = new HttpGet(uri);
		HttpResponse response1 = httpclient.execute(httpGet);
		
	    //System.out.println(response1.getStatusLine());
	    HttpEntity entity1 = null;
	    InputStream in = null;
	    BufferedImage image = null;
	    try {
		    entity1 = response1.getEntity();
			String contentType = entity1.getContentType().getValue();
			String imageType = "";
			if("image/png".equals(contentType))
			{
				imageType = "png";
			}
			else if("image/jpeg".equals(contentType))
			{
				imageType = "jpg";
			}
			else
			{
				throw new Exception("Image type " + contentType + " not supported");
			}
		    
		    in = entity1.getContent();
		    image = QDUtils.resizeImage(in);
		    
		    String filename = buildFileName(name, matchIndex, imageType);
			String path = (matchIndex > 1 ? args.dupesDir + DUPE_IMAGES_FOLDER + File.separatorChar : args.romsDir + "downloaded_images"+File.separatorChar) + filename;
		    File f = new File(path);
			Files.deleteIfExists(f.toPath());
		    Files.createDirectories(Paths.get(f.getParent()));
			
		    ImageIO.write(image, imageType, f);
		    image.flush();
		    return filename;
	    }
	    finally {
	    	if(entity1 != null) 	try { EntityUtils.consume(entity1); } finally{}
	    	if(in != null) 			try { in.close(); 					} finally{}
	    	if(image != null) 		try { image.flush(); 				} finally{}
	    }
	}
	
	/**
	 * Parses a date in MM/dd/yyyy format and sets the time to 00:00:00
	 * @param input
	 * @return
	 */
	private static Date parseDate(String input)
	{
		try
		{
			return SDF.parse(input);
		}
		catch(ParseException pe)
		{
			try
			{
				int year = Integer.valueOf(input);
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, year);
				cal.set(Calendar.MONTH, 0);
				cal.set(Calendar.DAY_OF_MONTH, 1);
				return cal.getTime();
			}
			catch(Exception e)
			{
				return null;
			}
		}
	}
	
	/**
	 * Get the name of the game that the user wants:
	 * - the title from thegames db
	 * - the raw rom name without the extension
	 * - a cleaned rom name
	 * @param rom name of the rom
	 * @param translatedName the name of the rom file, or a converted name for arcade games
	 * @param title title from thegamesdb
	 * @param args app args
	 * @return the desired name
	 */
	public static String getUserDesiredFilename(String rom, String translatedName, String title, Args args)  // TODO: move somewhere else, it should be shared
	{
		String name = null;
		if(args.useFilename || StringUtils.isEmpty(title))
		{
			if(!StringUtils.isEmpty(args.cleanFilename))
			{
				name = RomCleaner.cleanWithArgs(translatedName, args.cleanFilename);
			}
			else
			{
				name = RomCleaner.removeExtension(translatedName);
			}
		}
		else
		{
			name = title;
		}
		return name;
	}

	/**
	 * Transforms an xml answer from thegamesdb to a list of games.
	 * If a 100% match on the name is found, stops the process and returns the games processed.
	 * 
	 * @param rom name of the rom
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @param xml answer from thegamesdb in xml format
	 * @param args app's arguments
	 * @return the list of games
	 * @throws Exception
	 */
	private static List<Game> toGames(String rom, String translatedName, String xml, Args args) 
	throws Exception
	{
		// xml parsing stuff
		Document document = QDUtils.parseXML(xml);
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		
		List<Game> games = new ArrayList<>();
		for(int i=1; ; i++)
		{
			String id = xpath.evaluate("/Data/Game["+i+"]/id", document);
			if(StringUtils.isEmpty(id))
			{
				break; // stop
			}
			
			if(i == 2)
			{
				System.out.print("\t...");
			}
			else if(i>2)
			{
				System.out.print(".");
			}
			
			// the game
			Game game = new Game(THEGAMESDB_API_ID);
			String desc = xpath.evaluate("/Data/Game["+i+"]/Overview", document);
			String rating = xpath.evaluate("/Data/Game["+i+"]/Rating", document);
			String releasedate = xpath.evaluate("/Data/Game["+i+"]/ReleaseDate", document);
			String developer = xpath.evaluate("/Data/Game["+i+"]/Developer", document);
			String publisher = xpath.evaluate("/Data/Game["+i+"]/Publisher", document);
			String title = xpath.evaluate("/Data/Game["+i+"]/GameTitle", document);
			
			// the user desired name
			String name = getUserDesiredFilename(rom, translatedName, title, args);
			
			// genre
			String genre = "";
			List<String> genres = new ArrayList<>();
			for(int g=1; ; g++)
			{
				genre = xpath.evaluate("/Data/Game["+i+"]/Genres/genre["+g+"]", document);
				if(StringUtils.isEmpty(genre))
				{
					break; //stop
				}
				else
				{
					genres.add(genre);
				}
			}
			String players = xpath.evaluate("/Data/Game["+i+"]/Players", document);
			
			//image 
			String imageBaseUrl = xpath.evaluate("/Data/baseImgUrl", document);
			//String imageBaseUrl = "http://thegamesdb.net/banners/_gameviewcache/"; // better image as it's the japanes original, but always width = 300
			String imageUrl = xpath.evaluate("/Data/Game["+i+"]/Images/boxart[@side = 'front']", document);
			String image = downloadImage(name, imageUrl, imageBaseUrl, i, args);
			
			game.setName(name);
			game.setDesc(desc);
			game.setDeveloper(developer);
			game.setGenres(genres);
			game.setImage(image);
			game.setRating(StringUtils.isEmpty(rating) ? 0 : Float.valueOf(rating));
			game.setReleasedate(StringUtils.isEmpty(releasedate) ? null : parseDate(releasedate));
			game.setRom(rom);
			game.setPlayers(players);
			game.setPublisher(publisher);
			game.setId(id);
			game.setTitle(title);
			
			
			if(RomCleaner.isSameRom(rom, title))
			{
				// 100% match on the name, ensure this gets to top result
				List<Game> sure = new ArrayList<Game>();
				sure.add(game);
				sure.addAll(games);
				
				// move the image from dupes to first if not first
				if(i > 1) {
					moveImage(args.dupesDir + DUPE_IMAGES_FOLDER + File.separatorChar, args.romsDir + "downloaded_images" + File.separatorChar, buildFileName(name, i, null));
					moveImage(args.romsDir + "downloaded_images" + File.separatorChar, args.dupesDir + DUPE_IMAGES_FOLDER + File.separatorChar, buildFileName(name, 1, null));
				}
				
				if(sure.size() >= 2) //because some ... were output
				{
					System.out.println("");
				}
				
				return sure; 
			}
			else
			{
				games.add(game);
			}
		}
		
		if(games.size() >= 2) //because some ... were output
		{
			System.out.println("");
		}
		
		return games;
	}
	
	/**
	 * Moves an image from one folder to another
	 * @param from
	 * @param to
	 * @param nameWithoutExtension
	 */
	private static void moveImage(String from, String to, String nameWithoutExtension)
	{
		File f = Paths.get(from, nameWithoutExtension + ".jpg").toFile();
		String ext = "jpg";
		if(!f.exists())
		{
			f = Paths.get(from, nameWithoutExtension + ".png").toFile();
			ext = "png";
		}
		try
		{
			Files.move(Paths.get(f.getAbsolutePath()), Paths.get(to, nameWithoutExtension + "." + ext), StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException e)
		{
			e.printStackTrace(); // should not happen
		}
	}
	
	/**
	 * 
	 * @param rom name of the rom to look for (file name)
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @return the list of games found, first match should be the one
	 * @throws Exception
	 */
	public static List<Game> search(String rom, String translatedName, Args args) 
	{
		String cleanName = RomCleaner.cleanRomName(translatedName, false);
			
		String xml = null;
		
		// we'll search for these platforms
		String[] platforms = null; 	
		if(args.arcade)
		{
			platforms = new String[]{Platform.NEOGEO, Platform.ARCADE};
		}
		else
		{
			platforms = new String[]{args.platform};
		}
		
		// search
		for(String platform: platforms)
		{
			try
			{
				xml = searchXml(cleanName, platform, args);
			}
			catch(IOException | URISyntaxException e)
			{
				System.out.println("This error should not happen...");
				e.printStackTrace();
				System.exit(11);
			}
			// TODO: try to search and if nothing comes out, retry the search by cleaning more chars (-,!) etc..
			try
			{
				List<Game> games = toGames(rom, translatedName, xml, args);
				if(!CollectionUtils.isEmpty(games))
				{
					return games;
				}
				// else continue searching
			}
			catch(Exception e)
			{
				System.err.println("Error parsing xml from TheGamesDB!");
				e.printStackTrace();
				System.err.println("XML content:");
				System.err.println(xml);
				System.exit(12);
			};
		}
		 
		return null;
	}
}























