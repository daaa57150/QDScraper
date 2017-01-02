package daaa.qdscraper.services.api.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
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
import daaa.qdscraper.model.GameCollection;
import daaa.qdscraper.model.MatchingType;
import daaa.qdscraper.model.Rom;
import daaa.qdscraper.services.Console;
import daaa.qdscraper.services.PlatformConverter;
import daaa.qdscraper.services.api.ApiService;
import daaa.qdscraper.utils.QDUtils;
import daaa.qdscraper.utils.QDUtils.HttpAnswer;
import daaa.qdscraper.utils.RomCleaner;

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
	private static final float SEARCH_SCORE_THRESHOLD = Float.valueOf(Props.get("thegamesdb.score.threshold"));
	
	
	
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
			Console.printErr("Error querying TheGamesDB (code "+answer.getCode()+": '"+answer.getReason()+"') with url " + url);
			return null;
		}
		return answer.getContent();
	}
	
	
	
	/**
	 * Builds a unique filename for an image, for this run
	 * @param rom relative path to the rom
	 * @param gameId identifier of the game
	 * @return a unique filename for this run
	 */
	private String buildImageFileName(String rom, String gameId)
	{
		String id = Paths.get(rom).getFileName().toString();
		return QDUtils.sanitizeFilename(id) + "-" + THEGAMESDB_API_ID  + "-" + gameId+ "-" + QDUtils.nextInt();
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
	private GameCollection toGames(String rom, String translatedName, String xml, Args args) 
	throws Exception
	{
		// xml parsing stuff //TODO: don't break on exception
		Document document = QDUtils.parseXML(xml); 
		XPath xpath = QDUtils.getXPath();
		
		GameCollection games = new GameCollection();
		for(int i=1; ; i++)
		{
			Console.doProgress();
			
			String id = xpath.evaluate("/Data/Game["+i+"]/id", document);
			if(StringUtils.isEmpty(id))
			{
				break; // stop
			}
			
			// the game
			Game game = new Game(THEGAMESDB_API_ID);
			String desc = xpath.evaluate("/Data/Game["+i+"]/Overview", document);
			String rating = xpath.evaluate("/Data/Game["+i+"]/Rating", document); // on 10
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
			String image = null;
			if(!StringUtils.isEmpty(imageUrl))
			{
				String filename = buildImageFileName(rom, id);
				String path = args.romsDir + IMAGES_FOLDER + File.separatorChar + filename;
				path = QDUtils.downloadImage(imageBaseUrl+imageUrl, path, args);
				if(path != null)
				{
					String pathExt = FilenameUtils.getExtension(path);
					image = StringUtils.isEmpty(pathExt) ? filename : (filename + "." + pathExt);
				}
			}
			
			game.setName(name);
			game.setDesc(desc);
			game.setDeveloper(developer);
			game.setGenres(genres);
			game.setImage(image);
			game.setRating(StringUtils.isEmpty(rating) ? 0 : Float.valueOf(rating) / 10.f);
			game.setReleasedate(StringUtils.isEmpty(releasedate) ? null : parseDate(releasedate));
			game.setFile(rom);
			game.setPlayers(players);
			game.setPublisher(publisher);
			game.setId(id);
			game.setTitle(title);
			game.setMatchingType(MatchingType.SEARCH);
			
			games.add(game);
			
			setGameScores(game, translatedName, title);
			if(game.isPerfectMatch())
			{	
				// 100% match on the name, we can stop
				return games; // stop 
			}
			
			//also stop if score below a threshold, below 0.6 seems good
			if(SEARCH_SCORE_THRESHOLD > 0.f && game.getScore() < SEARCH_SCORE_THRESHOLD)
			{
				break;
			}
		}
		
		return games;
	}
	
	private void onApiDown() {
		Console.println("TheGamesDB is down (happens regularly), waiting a minute...");
		QDUtils.sleep(60000, "TheGamesDB is down (happens regularly)");
	}
	
	/**
	 * Searches TheGamesDB
	 * @param rom name of the rom to look for (file name)
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @return the list of games found
	 * @throws Exception
	 */
	@Override
	public GameCollection search(Rom rom, Args args) throws Exception 
	{	
		// will contain our matches
		GameCollection games = new GameCollection();
		
		try
		{
			String translatedName = rom.getTranslatedName();
			String cleanName = RomCleaner.cleanRomName(translatedName, false);
				
			String xml = null;
			
			// we'll search for these platforms
			String[] wantedPlatforms = PlatformConverter.asTheGamesDB(args.platform); // if args.arcade, platform is already 'arcade'
			if(wantedPlatforms == null) wantedPlatforms = new String[]{null};
			
			// search
			for(String wantedPlatform: wantedPlatforms)
			{
				// I got this once, add a 1 minute wait :
				/*
				 	<?xml version="1.0" encoding="UTF-8" ?>
					Could not connect: Can't connect to local MySQL server through socket '/var/run/mysqld/mysqld.sock' (2)
				*/
				
				int maxTries = 3;
				for(int currentTry = 0; currentTry < maxTries; currentTry ++)
				{
					try {
						xml = searchXml(cleanName, wantedPlatform, args); 
						
						if(xml == null || xml.substring(0, Math.min(140, xml.length()-1)).contains("Could not connect")) {
							onApiDown();
						}
						else
						{
							break; //we're good
						}
					} 
					catch(IOException | URISyntaxException e)
					{
						if(currentTry == maxTries - 1) {
							Console.printErr("TheGamesDB is down for good, try later?");
							Console.printErr(e);
							throw e;
						} else {
							onApiDown();
						}
					}
				}
				

				try
				{
					GameCollection gamesThisPlatform = toGames(rom.getFile(), translatedName, xml, args);
					games.addAll(gamesThisPlatform);
					
					if(gamesThisPlatform.getBestPerfectMatch() != null)
					{
						return games;
					}
					// else continue searching
				}
				catch(Exception e)
				{
					Console.printErr("Error parsing xml from TheGamesDB!");
					Console.printErr(e);
					Console.printErr("XML content:");
					Console.printErr(xml);
					System.exit(12);
				};
			}
		}
		finally
		{
			//super.stopProgress();
		}
		 
		return games;
	}

	/** {@inheritDoc} */
	@Override
	public String getApiName() {
		return "TheGamesDB";
	}
}























