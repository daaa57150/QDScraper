package daaa.qdscrapper.services.api.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import daaa.qdscrapper.Args;
import daaa.qdscrapper.Props;
import daaa.qdscrapper.model.Game;
import daaa.qdscrapper.services.Platform;
import daaa.qdscrapper.services.api.ApiService;
import daaa.qdscrapper.utils.QDUtils;
import daaa.qdscrapper.utils.QDUtils.HttpAnswer;
import daaa.qdscrapper.utils.RomCleaner;


// http://www.giantbomb.com/api/search/?api_key=xxxx&query=Metal%20Slug%20-%20Super%20Vehicle-001&resources=game&field_list=deck,description,id,image,name,original_release_date,platforms,api_detail_url
// => suivre api_detail_url
// http://www.giantbomb.com/api/game/3030-6941/?api_key=xxx&field_list=deck,description,id,image,name,original_release_date,developers,genres,publishers&format=json
/**
 * Utilities to query http://www.giantbomb.com/api using its API.
 * @see http://www.giantbomb.com/api/documentation
 * @author daaa
 *
 */
public class GiantBombApiService extends ApiService
{
	private static final String URL_GIANTBOMB_API = Props.get("giantbomb.url"); //"http://www.giantbomb.com/api/"; 
	private static final String GIANTBOMB_API_ID = "GiantBomb";
	private static final int SEARCH_LIMIT = Integer.valueOf(Props.get("giantbomb.search.limit"));
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final String IMAGES_FOLDER = Props.get("images.folder");
	
	
	/**
	 * Builds the url to perform the search for the game we want on giantbomb
	 * @param name the name of the game to search
	 * @param apiKey the api key
	 * @return the forged url 
	 * @throws URISyntaxException
	 */
	private String buildUrlSearch(String name, String apiKey) 
	throws URISyntaxException
	{
		URIBuilder uri = new URIBuilder(URL_GIANTBOMB_API + "search/");
		uri.addParameter("resources", "game");
		uri.addParameter("api_key", apiKey);
		uri.addParameter("query", name);
		uri.addParameter("field_list", "platforms,api_detail_url");
		return uri.toString();
	}
	
	/**
	 * Builds the complete url to a game on giantbomb
	 * @param url the url given by the search
	 * @param apiKey the api key
	 * @return the forged url
	 * @throws URISyntaxException 
	 */
	private String buildUrlGame(String url, String apiKey) 
	throws URISyntaxException
	{
		URIBuilder uri = new URIBuilder(url);
		uri.addParameter("field_list", "deck,developers,genres,id,image,name,publishers,original_release_date");// description?
		uri.addParameter("api_key", apiKey);
		return uri.toString();
	}
	
	/**
	 * Searches in the giantbomb xml answer if there was an error
	 * @param document the parsed giantbomb xml answer
	 * @return true if there was no error
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	private boolean validateXmlAnswer(Document document)
	{
		try
		{
			XPath xpath = QDUtils.getXPath();
		
			String statusCode = xpath.evaluate("response/status_code", document);
			if("1".equals(statusCode)) return true;
			
			String errorMessage = xpath.evaluate("response/error", document);
			System.err.println("Error querying GiantBomb: " + errorMessage);
		}
		catch(XPathExpressionException e)
		{
			e.printStackTrace(); 
		}
			
		return false;
	}
	
	/**
	 * Performs a search on giantbomb
	 * @param name name of the game to search
	 * @param args app args
	 * @return the parsed xml answer if there was no problem, null otherwise
	 * @throws URISyntaxException
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private Document search(String name, Args args) 
	{
		try
		{
			// make search
			String url = buildUrlSearch(name, args.giantBombApiKey);
			HttpAnswer searchAnswer = QDUtils.httpGet(args, url);
			
			if(searchAnswer.getCode() != HttpStatus.SC_OK)
			{
				System.err.println("Error querying GiantBomb (code "+searchAnswer.getCode()+") with url " + url);
				return null;
			}
			
			Document searchDocument = QDUtils.parseXML(searchAnswer.getContent());
			
			// validate the answer is ok
			if(!validateXmlAnswer(searchDocument)) return null;
			
			return searchDocument;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Parses the document game after game, returns the first matches for one of the wanted platforms
	 * @param searchDocument
	 * @param wantedPlatforms
	 * @return the list of url to games that match
	 */
	private List<String> getUrlOfFirstGamesForPlatforms(Document searchDocument, String[] wantedPlatforms)
	{
		List<String> urls = new ArrayList<String>();
		List<String> wpList = Arrays.asList(wantedPlatforms);
		try {
			XPath xpath = QDUtils.getXPath();
			NodeList games = (NodeList) xpath.evaluate("response/results/game", searchDocument, XPathConstants.NODESET);
		
			for(int i=0; i<games.getLength(); i++) 
			{
				Element game = (Element)games.item(i);
				Element platforms = (Element)game.getElementsByTagName("platforms").item(0);
				for(int j=0; j<platforms.getChildNodes().getLength(); j++)
				{
					Element platform = (Element)platforms.getChildNodes().item(j);
					String platformName = platform.getElementsByTagName("name").item(0).getTextContent().trim();
					//System.out.println(platformName);
					
					if(wpList.contains(platformName.trim()))
					{
						urls.add(game.getElementsByTagName("api_detail_url").item(0).getTextContent());
						if(urls.size() == SEARCH_LIMIT) return urls;
						break; // next game
					}
				}
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return null;
		}
		
		return urls;
	}
	
	/**
	 * Gets the game details from giantbomb
	 * @param url the url from the search
	 * @param args app's args
	 * @return the parsed document
	 */
	private Document parseGame(String url, Args args)
	{
		try
		{
			// make search
			String urlGame = buildUrlGame(url, args.giantBombApiKey);
			HttpAnswer gameAnswer = QDUtils.httpGet(args, urlGame);
			
			if(gameAnswer.getCode() != HttpStatus.SC_OK)
			{
				System.err.println("Error querying GiantBomb (code "+gameAnswer.getCode()+") with url " + url);
				return null;
			}
			
			Document gameDocument = QDUtils.parseXML(gameAnswer.getContent());
			
			// validate the answer is ok
			if(!validateXmlAnswer(gameDocument)) return null;
			
			return gameDocument;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Parses a date in yyyy-MM-dd 00:00:00 format
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
			return null;
		}
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
		return QDUtils.sanitizeFilename(name) + "-" + GIANTBOMB_API_ID + "-" + matchIndex + (imageType == null ? "" : ("." + imageType));
	}
	
	/**
	 * Transforms an xml game answer from giantbomb to a list of games.
	 * @param rom name of the rom
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @param gameDocument the document to parse
	 * @param args app's args
	 * @param index index of the match
	 * @return the game
	 * @throws XPathExpressionException 
	 */
	private Game toGame(String rom, String translatedName, Document gameDocument, Args args, int index) 
	throws Exception
	{
		XPath xpath = QDUtils.getXPath();
		
		// the game
		Game game = new Game(GIANTBOMB_API_ID);
		//Element results = (Element)xpath.evaluate("", gameDocument, XPathConstants.NODE);
		String id = xpath.evaluate("response/results/id", gameDocument);
		String desc = xpath.evaluate("response/results/deck", gameDocument);
		//String rating = xpath.evaluate("", gameDocument);
		String releasedate = xpath.evaluate("response/results/original_release_date", gameDocument);
		String developer = xpath.evaluate("response/results/developers[1]/company/name", gameDocument); //only the first, we don't really care
		String publisher = xpath.evaluate("response/results/publishers[1]/publisher/name", gameDocument); //only the first, we don't really care
		String title = xpath.evaluate("response/results/name", gameDocument);
		
		// the user desired name
		String name = RomCleaner.getUserDesiredFilename(rom, translatedName, title, args);
		
		// genre
		String genre = "";
		List<String> genres = new ArrayList<>();
		for(int g=1; ; g++)
		{
			genre = xpath.evaluate("response/results/genres/genre["+g+"]/name", gameDocument);
			if(StringUtils.isEmpty(genre))
			{
				break; //stop
			}
			else
			{
				genres.add(genre);
			}
		}
		
		// image
		String imageUrl = xpath.evaluate("response/results/image/super_url", gameDocument);
		String image = null;
		if(!StringUtils.isEmpty(imageUrl))
		{
			String filename = buildImageFileName(rom, index, null);
			String path = args.romsDir + IMAGES_FOLDER + File.separatorChar + filename;
		    String imagePath = QDUtils.downloadImage(imageUrl, path, args);
		    String pathExt = FilenameUtils.getExtension(imagePath);
			image = StringUtils.isEmpty(pathExt) ? filename : (filename + "." + pathExt);
		}
	    // put everything together
	    game.setName(name);
		game.setDesc(desc);
		game.setDeveloper(developer);
		game.setGenres(genres);
		game.setImage(image);
		game.setReleasedate(StringUtils.isEmpty(releasedate) ? null : parseDate(releasedate));
		game.setRom(rom);
		game.setPublisher(publisher);
		game.setId(id);
		game.setTitle(title);
		
		return game;
	}
	
	/**
	 * Searches giantbomb for games on the wanted platform
	 * @param rom the rom to search
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @param args app's arguments
	 * @return the first few matches
	 */
	@Override
	public List<Game> search(String rom, String translatedName, Args args) 
	{
		if(args.giantBombApiKey == null)
		{
			return null;
		}
		
		// else we have a giant bomb api key 
		
		
		String cleanName = RomCleaner.cleanRomName(translatedName, false);
		
		// we'll search for these platforms
		String[] wantedPlatforms = null; 	
		if(args.arcade)
		{
			wantedPlatforms = new String[]{Platform.asGiantBomb(Platform.NEOGEO), Platform.asGiantBomb(Platform.ARCADE)};
		}
		else
		{
			wantedPlatforms = new String[]{Platform.asGiantBomb(args.platform)}; // TODO: many
		}
		
		// search
		Document searchDocument = search(cleanName, args);
		if(searchDocument == null) return null; // bail out
		
		// get the urls to the games from the search in the limit set in the properties
		List<String> urlsToGames = getUrlOfFirstGamesForPlatforms(searchDocument, wantedPlatforms);
		
		// for each url, we'll get the games
		List<Game> games = new ArrayList<>(urlsToGames.size());
		for(int i=0; i<urlsToGames.size(); i++)
		{
			String urlToGame = urlsToGames.get(i);
			
			// ask for the game details
			Document gameDocument = parseGame(urlToGame, args);
			if(gameDocument == null) continue; // bail out of this match
			
			// transform to game
			try {
				Game game = toGame(rom, translatedName, gameDocument, args, i+1);
				if(game!=null)
				{	
					games.add(game);
					if(isSameRom(translatedName, game.getTitle()))
					{
						// 100% match on the name, we can stop
						game.setPerfectMatch(true);
						return games;
					}
				}
			} catch (Exception e) {
				System.err.println("Error parsing xml from giantbomb!");
				e.printStackTrace();
				System.exit(15);
			}
			
		}
		
		return games;
	}
	
	
}
























