package daaa.qdscrapper.services;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import daaa.qdscrapper.Args;
import daaa.qdscrapper.Props;
import daaa.qdscrapper.model.Game;
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
public class GiantBomb 
{
	private GiantBomb(){} // do not instanciate

	private static final String URL_GIANTBOMB_API = Props.get("giantbomb.url"); //"http://www.giantbomb.com/api/"; 
	//private static final String SEARCH = "search/?resources=game";
	private static final String GIANTBOMB_API_ID = "GiantBomb";
	
	
	/**
	 * Builds the url to perform the search for the game we want on giantbomb
	 * @param name the name of the game to search
	 * @param apiKey the api key
	 * @return the forged url 
	 * @throws URISyntaxException
	 */
	private static String buildUrlSearch(String name, String apiKey) 
	throws URISyntaxException
	{
		URIBuilder uri = new URIBuilder(URL_GIANTBOMB_API + "search/");
		uri.addParameter("resources", "game");
		uri.addParameter("api_key", apiKey);
		uri.addParameter("query", name);
		uri.addParameter("field_list", "deck,description,id,image,name,original_release_date,platforms,api_detail_url"); //TODO: moins de trucs?
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
	private static boolean validateXmlAnswer(Document document)
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
	private static Document search(String name, Args args) 
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
	
	private static String getUrlOfFirstGameForPlatforms(Document searchDocument, String[] wantedPlatforms)
	{
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
					System.out.println(platformName);
					// TODO: if platform matches, return the url!
				}
				
				System.out.println(game);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Searches giantbomb for a game on the wanted platform
	 * @param rom the rom to search
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @param args app's arguments
	 * @return the first match
	 */
	public static Game search(String rom, String translatedName, Args args) 
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
			wantedPlatforms = new String[]{Platform.asGiantBomb(args.platform)};
		}
		
		Document searchDocument = search(cleanName, args);
		if(searchDocument == null) return null; // bail out
		
		String urlToGame = getUrlOfFirstGameForPlatforms(searchDocument, wantedPlatforms);
		
		//TODO: parse, find the first game with the right platform, transform to game
		
		return null;
	}
	
	
}
























