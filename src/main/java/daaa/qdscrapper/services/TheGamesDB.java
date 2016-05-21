package daaa.qdscrapper.services;

import java.io.File;
import java.io.IOException;
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

import javax.xml.xpath.XPath;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
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
	private static String buildImageFileName(String name, int matchIndex, String imageType)
	{
		return QDUtils.sanitizeFilename(name) + "-" + THEGAMESDB_API_ID + "-" + matchIndex + (imageType == null ? "" : ("." + imageType));
	}
	
	
	
	/**
	 * Parses a date in MM/dd/yyyy format
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
				// sometimes they only have the year
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
		XPath xpath = QDUtils.getXPath();
		
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
			String name = RomCleaner.getUserDesiredFilename(rom, translatedName, title, args);
			
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
			String filename = buildImageFileName(rom, i, null);
			String path = (i > 1 ? args.dupesDir + DUPE_IMAGES_FOLDER + File.separatorChar : args.romsDir + "downloaded_images" + File.separatorChar) + filename;
			path = QDUtils.downloadImage(imageBaseUrl+imageUrl, path, args);
			String pathExt = FilenameUtils.getExtension(path);
			String image = StringUtils.isEmpty(pathExt) ? filename : (filename + "." + pathExt);
			
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
			
			
			if(RomCleaner.isSameRom(translatedName, title))
			{
				// 100% match on the name, ensure this gets to top result
				List<Game> sure = new ArrayList<Game>();
				sure.add(game);
				sure.addAll(games);
				
				// move the image from dupes to first if not first
				if(i > 1) {
					moveImage(args.dupesDir + DUPE_IMAGES_FOLDER + File.separatorChar, args.romsDir + "downloaded_images" + File.separatorChar, buildImageFileName(name, i, null));
					moveImage(args.romsDir + "downloaded_images" + File.separatorChar, args.dupesDir + DUPE_IMAGES_FOLDER + File.separatorChar, buildImageFileName(name, 1, null));
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























