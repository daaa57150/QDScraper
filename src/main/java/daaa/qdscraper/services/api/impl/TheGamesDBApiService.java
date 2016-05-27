package daaa.qdscraper.services.api.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.xpath.XPath;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;

import daaa.qdscraper.Args;
import daaa.qdscraper.Props;
import daaa.qdscraper.model.Game;
import daaa.qdscraper.services.PlatformConverter;
import daaa.qdscraper.services.api.ApiService;
import daaa.qdscraper.utils.QDUtils;
import daaa.qdscraper.utils.RomCleaner;
import daaa.qdscraper.utils.QDUtils.HttpAnswer;

/**
 * Utilities to query http://thegamesdb.net/ using its API
 * 
 * @author daaa
 *
 */
public class TheGamesDBApiService extends ApiService
{
	
	private static final String URL_GAMESDB_API = Props.get("thegamesdb.url");//"http://thegamesdb.net/api/"; 
	private static final String GET_GAME = "GetGame.php";
	private static final SimpleDateFormat SDF = new SimpleDateFormat("MM/dd/yyyy");
	private static final String THEGAMESDB_API_ID = "TheGamesDB";
	private static final String IMAGES_FOLDER = Props.get("images.folder");
	
	
	
	/**
	 * Builds the 'getGame' api url
	 * @param name name of the game
	 * @param wantedPlatform optional platform (TGDB platform name)
	 * @return the xml result from thegamesdb
	 * @throws URISyntaxException
	 */
	private String buildGetGame(String name, String wantedPlatform) throws URISyntaxException
	{
		URIBuilder uri = new URIBuilder(URL_GAMESDB_API + GET_GAME);
		uri.addParameter("name", name);
		if(StringUtils.isNotEmpty(wantedPlatform))
		{
			uri.addParameter("platform", wantedPlatform);
		}
		
		return uri.toString();
	}
	
	/**
	 * Searches a game on thegamesdb, return its xml answer
	 * @param name the name of the game to search
	 * @param wantedPlatform the platform filter in TGDB format
	 * @param args the app's arguments (uses the platform)
	 * @return the xml answer from thegamesdb
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private String searchXml(String name, String wantedPlatform, Args args)
	throws ClientProtocolException, IOException, URISyntaxException
	{
		String url = buildGetGame(name, wantedPlatform);
		HttpAnswer answer = QDUtils.httpGet(args, url);
		if(answer.getCode() != HttpStatus.SC_OK)
		{
			System.err.println("Error querying TheGamesDB (code "+answer.getCode()+": '"+answer.getReason()+"') with url " + url);
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
	private String buildImageFileName(String name, int matchIndex, String imageType)
	{
		return QDUtils.sanitizeFilename(name) + "-" + THEGAMESDB_API_ID + "-" + matchIndex + (imageType == null ? "" : ("." + imageType));
	}
	
	
	
	/**
	 * Parses a date in MM/dd/yyyy format
	 * @param input
	 * @return
	 */
	private Date parseDate(String input)
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
	private List<Game> toGames(String rom, String translatedName, String xml, Args args) 
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
			String image = null;
			if(!StringUtils.isEmpty(imageUrl))
			{
				String filename = buildImageFileName(rom, i, null);
				String path = args.romsDir + IMAGES_FOLDER + File.separatorChar + filename;
				path = QDUtils.downloadImage(imageBaseUrl+imageUrl, path, args);
				String pathExt = FilenameUtils.getExtension(path);
				image = StringUtils.isEmpty(pathExt) ? filename : (filename + "." + pathExt);
			}
			
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
			
			games.add(game);
			
			setGameScore(game, translatedName, title);
			if(game.isPerfectMatch())
			{	
				// 100% match on the name, we can stop
				if(games.size() >= 2) //because some ... were output TODO: delegate to super class
				{
					System.out.println("");
				}
				
				return games; // stop 
			}
			//TODO: also stop if score below a threshold
		}
		
		if(games.size() >= 2) //because some ... were output
		{
			System.out.println("");
		}
		
		return games;
	}
	
	/* *
	 * Moves an image from one folder to another
	 * @param from
	 * @param to
	 * @param nameWithoutExtension
	 * /
	private void moveImage(String from, String to, String nameWithoutExtension)
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
	} */
	
	/**
	 * Searches TheGamesDB
	 * @param rom name of the rom to look for (file name)
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @return the list of games found
	 * @throws Exception
	 */
	@Override
	public List<Game> search(String rom, String translatedName, Args args) 
	{
		String cleanName = RomCleaner.cleanRomName(translatedName, false);
			
		String xml = null;
		
		// we'll search for these platforms
		String[] wantedPlatforms = PlatformConverter.asTheGamesDB(args.platform); // if args.arcade, platform is already 'arcade'
		if(wantedPlatforms == null) wantedPlatforms = new String[]{null};
		
		// will contain our matches
		List<Game> games = new ArrayList<Game>();
		
		// search
		for(String wantedPlatform: wantedPlatforms)
		{
			try
			{
				xml = searchXml(cleanName, wantedPlatform, args);
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
				List<Game> gamesThisPlatform = toGames(rom, translatedName, xml, args);
				if(QDUtils.findBestPerfectMatch(gamesThisPlatform) != null)
				{
					return gamesThisPlatform;
				}
				// else continue searching
				games.addAll(gamesThisPlatform);
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
		 
		return games;
	}
}























