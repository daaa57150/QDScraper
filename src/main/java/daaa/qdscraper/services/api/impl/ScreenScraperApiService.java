package daaa.qdscraper.services.api.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.xml.xpath.XPath;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
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
import daaa.qdscraper.utils.CryptoUtils;
import daaa.qdscraper.utils.QDUtils;
import daaa.qdscraper.utils.QDUtils.HttpAnswer;

/**
 * Utilities to query http://www.screenscraper.fr/ using its API
 * 
 * @author daaa
 *
 */
public class ScreenScraperApiService extends ApiService 
{
	/** Translates the genres FR->EN and drops some */
	private Properties genresTranslation;
	
	private String devid;
	private String devpassword;
	private static final String URL_SCREENSCRAPER_API = Props.get("screenscraper.url");
	private static final String GET_GAME = "jeuInfos.php";
	private static final String SOFTNAME = Props.get("screenscraper.softname");
	private static final String SCREENSCRAPER_API_ID = "ScreenScraper";
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");
	private static final String IMAGES_FOLDER = Props.get("images.folder");
	
	
	/**
	 * Constructor
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	public ScreenScraperApiService() 
	throws GeneralSecurityException, IOException 
	{
		genresTranslation = QDUtils.loadClasspathProperties("screenscraper_genres.properties");
		devid  = CryptoUtils.decrypt(Props.get("screenscraper.devid"));
		devpassword = CryptoUtils.decrypt(Props.get("screenscraper.devpassword"));
	}
	
	/** Builds the base of the url, with credentials and all fixed stuff  */
	private URIBuilder buildUrlBase() throws URISyntaxException {
		URIBuilder uri = new URIBuilder(URL_SCREENSCRAPER_API + GET_GAME);
		uri.addParameter("devid", devid);
		uri.addParameter("devpassword", devpassword);
		uri.addParameter("softname", SOFTNAME);
		uri.addParameter("output", "xml");
		uri.addParameter("romtype", "rom");
		return uri;
	}
	
	/**
	 * Builds the url to get a game based on its md5 hash
	 * @param rom the rom
	 * @param wantedPlatform the wantedPlatform in screenscraper format
	 * @param args app's args
	 * @return the url
	 */
	//http://www.screenscraper.fr/api/jeuInfos.php?devid=xxx&devpassword=yyy&softname=zzz&output=xml&systemeid=75&romtype=rom&md5=2EB971ABDD410A743AD9D6680C522030
	private String buildUrlMd5(Rom rom, String wantedPlatform, Args args) {
		String md5 = rom.getMd5();
		if(md5 == null) return null; // not a real file
		
		try
		{
			URIBuilder uri = buildUrlBase();
			uri.addParameter("systemeid", wantedPlatform);
			uri.addParameter("md5", md5);
			
			return uri.toString();
		}
		catch(URISyntaxException e)
		{
			Console.printErr(e); // should not happen
		}
		return null;
	}
	
	/**
	 * Builds the url to get a game based on its name
	 * @param rom the rom
	 * @param wantedPlatform the wantedPlatform in screenscraper format
	 * @param args app's args
	 * @return the url
	 */
	//http://www.screenscraper.fr/api/jeuInfos.php?devid=xxx&devpassword=yyy&softname=zzz&output=xml&systemeid=75&romtype=rom&romnom=pnickj.zip
	private String buildUrlRomNom(Rom rom, String wantedPlatform, Args args) {
		try
		{
			String romnom = FilenameUtils.getName(rom.getFile());
			URIBuilder uri = buildUrlBase();
			uri.addParameter("systemeid", wantedPlatform);
			uri.addParameter("romnom", romnom);
			
			return uri.toString();
		}
		catch(URISyntaxException e)
		{
			Console.printErr(e); // should not happen
		}
		return null;
	}
	
	/**
	 * Get the content of the url, verifying everything went ok
	 * @param url the url to query
	 * @param args app's args
	 * @return the content of the url if everything went ok, null otherwise
	 */
	private String readUrl(String url, Args args)
	{
		try {
			HttpAnswer answer = QDUtils.httpGet(args, url);
			if(answer.getCode() != HttpStatus.SC_OK)
			{
				Console.printErr("Error querying ScreenScraper (code "+answer.getCode()+": '"+answer.getReason()+"')"); // don't print the url with the password
				return null;
			}
			
			if(answer.getContent().startsWith("Erreur"))
			{
				// removed sysout as it's printed when no match
				//Console.printErr("Error querying ScreenScraper: "+answer.getContent()); // don't print the url with the password
				return null;
			}
			
			return answer.getContent();
			
		} catch (IOException e) {
			Console.printErr(e);
		}
		
		return null;
	}
	
	/**
	 * Builds a unique filename for an image
	 * @param rom relative path to the rom
	 * @param matchIndex index of the match
	 * @param gameId the game unique identifier
	 * @return a unique filename for this game
	 */
	private String buildImageFileName(String rom, String gameId)
	{
		String id = Paths.get(rom).getFileName().toString();
		return QDUtils.sanitizeFilename(id) + "-" + SCREENSCRAPER_API_ID  + "-" + gameId + "-" + QDUtils.nextInt();
	}
	
	/**
	 * Parses a date in yyyy-MM-dd format
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
	 * Parses the xml sent by ScreenScraper to retrieve our infos
	 * @param rom the rom to process
	 * @param translatedName the cleaned name of the rom
	 * @param xml the xml to parse
	 * @param args app's args
	 * @return the parsed game
	 * @throws Exception
	 */
	private Game toGame(String rom, String translatedName, String xml, Args args) 
	throws Exception
	{
		// xml parsing stuff
		Document document = QDUtils.parseXML(xml);
		XPath xpath = QDUtils.getXPath();
		
		// there should be a result, but just in case...
		String id = xpath.evaluate("Data/jeu[1]/id", document);
		if(!StringUtils.isEmpty(id))
		{
		
			Game game = new Game(SCREENSCRAPER_API_ID);
			String desc = xpath.evaluate("Data/jeu[1]/synopsis/synopsis_us", document);
			String rating = xpath.evaluate("Data/jeu[1]/note", document); // on 20
			String developer = xpath.evaluate("Data/jeu[1]/developpeur", document);
			String publisher = xpath.evaluate("Data/jeu[1]/editeur", document);
			String title = xpath.evaluate("Data/jeu[1]/nom", document);
			String players = xpath.evaluate("Data/jeu[1]/joueurs", document);
			if(!StringUtils.isEmpty(players))
			{
				players = players.replaceAll("[P|p]layers?", "").trim();
				players = players.replaceAll("[J|j]oueurs?", "").trim();
			}
			
			// the user desired name
			String name = getUserDesiredFilename(rom, translatedName, title, args);
			
			// genres
			String genre = "";
			List<String> genres = new ArrayList<>();
			for(int g=1; ; g++)
			{
				genre = xpath.evaluate("Data/jeu/genres/genre["+g+"]", document);
				if(StringUtils.isEmpty(genre))
				{
					break; //stop
				}
				else
				{
					String translatedGenre = genresTranslation.getProperty(genre.trim());
					if(!StringUtils.isEmpty(translatedGenre))
					{
						genres.add(translatedGenre);
					}
				}
			}
			
			// date, we'll get these regions in order of preference
			String[] regions = {"world", "usa", "japan", "france"};
			String releasedate = null; // 1992-11-20
			for(String region: regions)
			{
				releasedate = xpath.evaluate("Data/jeu[1]/dates/"+region, document);
				if(!StringUtils.isEmpty(releasedate)) break; // found one
			}
			
			//image 
			/*
			 	In this order:
			 	medias/
			 		media_boxs/
			 			media_boxs2d/
			 				media_box2d_us
			 				media_box2d_eu
			 				media_box2d_jp
			 		media_screenshot
			 		media_fanart
			*/
			String imageUrl = null;
			String image = null;
			String[] medias = {
				"medias/media_boxs/media_boxs2d/media_box2d_us",
				"medias/media_boxs/media_boxs2d/media_box2d_eu",
				"medias/media_boxs/media_boxs2d/media_box2d_jp",
				"medias/media_screenshot",
				"medias/media_fanart",
			};
			for(String media: medias)
			{
				imageUrl = xpath.evaluate("Data/jeu[1]/"+media, document);
				if(!StringUtils.isEmpty(imageUrl)) break; // found one
			}
			if(!StringUtils.isEmpty(imageUrl))
			{
				String filename = buildImageFileName(rom, id);
				String path = args.romsDir + IMAGES_FOLDER + File.separatorChar + filename;
				path = QDUtils.downloadImage(imageUrl, path, args);
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
			game.setRating(StringUtils.isEmpty(rating) ? 0 : Float.valueOf(rating) / 20.f);
			game.setReleasedate(StringUtils.isEmpty(releasedate) ? null : parseDate(releasedate));
			game.setFile(rom);
			game.setPlayers(players);
			game.setPublisher(publisher);
			game.setId(id);
			game.setTitle(title);
			
			// should be 100% since ScreenScraper uses a very strict matching based on hash or rom name
			// setGameScores(game, translatedName, title);
			game.setScore(1);
			game.setDistance(0);
			
			
			return game;
		}
		
		return null;
	}
	
	
	/**
	 * Searches ScreenScraper, returns a unique game
	 * @param rom name of the rom to look for (file name)
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @return the list of games found (only one in this case)
	 * @throws Exception
	 */
	@Override
	public GameCollection search(Rom rom, Args args) 
	{
		//super.startProgress();
		
		// will contain our matches
		GameCollection games = new GameCollection();
		
		try
		{
		
			// we'll search for these platforms
			String[] wantedPlatforms = PlatformConverter.asScreenScraper(args.platform); // if args.arcade, platform is already 'arcade'
			
			// search
			for(String wantedPlatform: wantedPlatforms)
			{
				// 1) req with md5 if possible
				if(rom.isRealFile() && !"scummvm".equals(args.platform)) //no scummvm on screenscraper anyway
				{
					String url = buildUrlMd5(rom, wantedPlatform, args);
					String xml = null;
					if(url != null)
					{
						Console.doProgress();
						xml = readUrl(url, args);
					}
					
					MatchingType matchingType = MatchingType.MD5;
					if(xml == null)
					{
						url = buildUrlRomNom(rom, wantedPlatform, args);
						Console.doProgress();
						xml = readUrl(url, args);
						matchingType = MatchingType.FILENAME;
					}
					
					if(xml != null)
					{
						try
						{
							Game game = toGame(rom.getFile(), rom.getTranslatedName(), xml, args);
							game.setMatchingType(matchingType);
							games.add(game);
						}
						catch(Exception e)
						{
							Console.printErr("Error parsing xml from ScreenScraper!");
							Console.printErr(e);
							Console.printErr("XML content:");
							Console.printErr(xml);
							System.exit(22);
						}
					}
				}
			}
		}
		finally
		{
			//super.stopProgress();
		}
		
		
		return games;
	}

}


//pour chaque platforme (systemeid obligatoire, mame/75 pour arcade)
// 1) req avec md5
// 2) req avec nom-rom
// 3) peut-être rechercher avec leur formulaire le nom du jeu ?







