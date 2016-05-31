package daaa.qdscraper.services.api.impl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.utils.URIBuilder;

import daaa.qdscraper.Args;
import daaa.qdscraper.Props;
import daaa.qdscraper.model.Game;
import daaa.qdscraper.model.Rom;
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
	private Properties genres;
	
	private String devid;
	private String devpassword;
	private static final String URL_SCREENSCRAPER_API = Props.get("screenscraper.url");
	private static final String GET_GAME = "jeuInfos.php";
	private static final String SOFTNAME = Props.get("screenscraper.softname");
	
	/**
	 * Constructor
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	public ScreenScraperApiService() 
	throws GeneralSecurityException, IOException 
	{
		genres = QDUtils.loadClasspathProperties("screenscraper_genres.properties");
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
			e.printStackTrace(); // should not happen
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
			e.printStackTrace(); // should not happen
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
				System.err.println("Error querying ScreenScraper (code "+answer.getCode()+": '"+answer.getReason()+"')"); // don't print the url with the password
				return null;
			}
			
			if(answer.getContent().startsWith("Erreur"))
			{
				System.err.println("Error querying ScreenScraper: "+answer.getContent()); // don't print the url with the password
				return null;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	/**
	 * Searches ScreenScraper
	 * @param rom name of the rom to look for (file name)
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @return the list of games found
	 * @throws Exception
	 */
	@Override
	public List<Game> search(Rom rom, Args args) 
	{
		// will contain our matches
		List<Game> games = new ArrayList<>();
		
		// we'll search for these platforms
		String[] wantedPlatforms = PlatformConverter.asScreenScraper(args.platform); // if args.arcade, platform is already 'arcade'
		
		// search
		for(String wantedPlatform: wantedPlatforms)
		{
			// 1) req with md5 if possible
			if(rom.isRealFile() && !"scummvm".equals(args.platform)) //no scummvm on screenscraper anyway
			{
				String url = buildUrlMd5(rom, wantedPlatform, args);
				String xml = readUrl(url, args);
				if(xml == null)
				{
					url = buildUrlRomNom(rom, wantedPlatform, args);
					xml = readUrl(url, args);
				}
				
				if(xml != null)
				{
					// TOGAME!
				}
			}
		}
		
		
		
		return games;
	}

}


//pour chaque platforme (systemeid obligatoire, mame/75 pour arcade)
// 1) req avec md5
// 2) req avec nom-rom
// 3) peut-être rechercher avec leur formulaire le nom du jeu







