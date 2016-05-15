package daaa.qdscrapper;

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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.w3c.dom.Document;

/**
 * Utilities to query http://thegamesdb.net/ using its API
 * 
 * @author kerndav
 *
 */
public class GamesDB
{
	private GamesDB(){} // do not instanciate
	
	private static final String URL_GAMESDB_API = "http://thegamesdb.net/api/";
	private static final String GET_GAME = "GetGame.php";
	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
	private static int MAX_WIDTH = 400;
	
	// TODO: move it to a network or utils class
	private static HttpClient httpClient = null;
	private static HttpClient getClient(Args args)
	{		
		if(httpClient == null)
		{
			HttpClientBuilder builder = HttpClientBuilder.create();
			if(!StringUtils.isEmpty(args.proxyHost))
			{
				HttpHost proxy = new HttpHost(args.proxyHost, args.proxyPort);
				builder.setProxy(proxy);
			}
			
			if(!StringUtils.isEmpty(args.user))
			{
				Credentials credentials = new UsernamePasswordCredentials(args.user,args.password);
				AuthScope authScope = new AuthScope(args.proxyHost, args.proxyPort);
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(authScope, credentials);
				builder.setDefaultCredentialsProvider(credsProvider);
			}
			
			/*RequestConfig config = RequestConfig.custom()
				    .setSocketTimeout(10 * 1000)
				    .setConnectTimeout(10 * 1000)
				    .build();
			builder.setDefaultRequestConfig(config);*/
			
			httpClient = builder.build();
			
			
		}
		return httpClient;
	}
	
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
		String p = Platform.get(platform);
		if(StringUtils.isNotEmpty(p))
		{
			uri.addParameter("platform", p);
		}
		
		return uri.toString();
	}
	
	/**
	 * Searches a game on thegamesdb, return its xml answer
	 * @param name the name of the game to search
	 * @param args the app's arguments (uses the platform)
	 * @return the xml answer from thegamesdb
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static String searchXml(String name, Args args)
	throws ClientProtocolException, IOException, URISyntaxException
	{
		String answer = null;
		
		HttpClient httpclient = getClient(args);
		String uri = buildGetGame(name, args.platform);
		HttpGet httpGet = new HttpGet(uri);
		HttpResponse response1 = httpclient.execute(httpGet);
		HttpEntity entity1 = null;
		try {
		    //System.out.println(response1.getStatusLine());
		    entity1 = response1.getEntity();
		    answer = IOUtils.toString(entity1.getContent());
		} finally {
		    if(entity1 != null)  EntityUtils.consume(entity1);
		}
		
		return answer;
	}
	
	/**
	 * Resizes an image to a max witdh of 400px
	 * @param in the input image
	 * @return the resized image data
	 * @throws IOException
	 */
	private static BufferedImage resizeImage(InputStream in) 
	throws IOException
	{
		BufferedImage srcImage = null;
		try
		{
			srcImage = ImageIO.read(in);
			if(srcImage.getWidth() < MAX_WIDTH)
			{
				return srcImage;
			}
			
			BufferedImage scaledImage = Scalr.resize(srcImage, Method.QUALITY, Mode.FIT_TO_WIDTH, MAX_WIDTH); // Scale image
			return scaledImage;
		}
		finally
		{
			//if(srcImage != null) srcImage.flush();
		}
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
		return name + "-daaa-" + matchIndex + "-img" + (imageType == null ? "" : ("." + imageType));
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
		HttpClient httpclient = getClient(args);
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
		    image = resizeImage(in);
		    
		    String filename = buildFileName(name, matchIndex, imageType);
			String path = args.romsDir + (matchIndex > 1 ? "DUPE_images"+File.separatorChar : "downloaded_images"+File.separatorChar) + filename;
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
	 * @param title title from thegamesdb
	 * @param args app args
	 * @return the desired name
	 */
	public static String getUserDesiredFilename(String rom, String title, Args args) 
	{
		String name = null;
		if(args.useFilename || StringUtils.isEmpty(title))
		{
			if(!StringUtils.isEmpty(args.cleanFilename))
			{
				name = RomCleaner.cleanWithArgs(rom, args.cleanFilename);
			}
			else
			{
				name = RomCleaner.removeExtension(rom);
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
	 * @param xml answer from thegamesdb in xml format
	 * @param args app's arguments
	 * @return the list of games
	 * @throws Exception
	 */
	//TODO: put all DUPE files in a single folder
	private static List<Game> toGames(String rom, String xml, Args args) 
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
			Game game = new Game();
			String desc = xpath.evaluate("/Data/Game["+i+"]/Overview", document);
			String rating = xpath.evaluate("/Data/Game["+i+"]/Rating", document);
			String releasedate = xpath.evaluate("/Data/Game["+i+"]/ReleaseDate", document);
			String developer = xpath.evaluate("/Data/Game["+i+"]/Developer", document);
			String publisher = xpath.evaluate("/Data/Game["+i+"]/Publisher", document);
			String title = xpath.evaluate("/Data/Game["+i+"]/GameTitle", document);
			
			// the user desired name
			String name = getUserDesiredFilename(rom, title, args);
			
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
					moveImage(args.romsDir + "DUPE_images" + File.separatorChar, args.romsDir + "downloaded_images" + File.separatorChar, buildFileName(name, i, null));
					moveImage(args.romsDir + "downloaded_images" + File.separatorChar, args.romsDir + "DUPE_images" + File.separatorChar, buildFileName(name, 1, null));
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
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param rom name of the rom to look for (file name)
	 * @param platform the ES platform (folder in recalbox)
	 * @param base dir where images and dupes will be saved
	 * @return the list of games found, first match should be the one
	 * @throws Exception
	 */
	public static List<Game> search(String rom, Args args) 
	throws Exception 
	{
		String cleanRom = RomCleaner.cleanRomName(rom, false);
		
		String xml = searchXml(cleanRom, args);
		// TODO: try to search and if nothing comes out, retry the search by cleaning more chars (-,!) etc..
		//String xml = StringUtils.join(Files.readAllLines(Paths.get("D:/JavaBundle/workspaces/Developpement/QDScrapper/files/example.xml"), Charset.forName("UTF-8")), "\n");
		return toGames(rom, xml, args);
	}
}























