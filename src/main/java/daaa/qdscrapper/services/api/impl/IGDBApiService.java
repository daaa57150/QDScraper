package daaa.qdscrapper.services.api.impl;

import java.io.IOException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicHeader;

import daaa.qdscrapper.Args;
import daaa.qdscrapper.Props;
import daaa.qdscrapper.model.Game;
import daaa.qdscrapper.services.api.ApiService;
import daaa.qdscrapper.utils.QDUtils;
import daaa.qdscrapper.utils.QDUtils.HttpAnswer;

/**
 * Utilities to query https://www.igdb.com/ using its API
 * 
 * @author daaa
 *
 */
public class IGDBApiService extends ApiService {

	/** My key to query IGDB, this gives us 10000 queries per day */
	private static final String API_KEY = Props.get("igdb.apiKey");
	/** APi url https://www.igdb.com/api/v1/ */
	private static final String URL_IGDB_API = Props.get("igdb.url");
	
	
	/**
	 * Http get on the given url, with a valid token
	 * @param url the url to query
	 * @param args app's args
	 * @return the http answer content
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private String httpGet(String url, Args args)
	{
		try 
		{
			HttpAnswer answer = QDUtils.httpGet(
				args, url, 
				new Header[]{new BasicHeader("Authorization", "Token token=\"" + API_KEY + "\"")} 
			);
			
			if(answer.getCode() != HttpStatus.SC_OK) {
				String message = null;
				// see https://www.igdb.com/api/v1/documentation
				switch(answer.getCode()) {
					case 401: {
						message = "Not a valid API key";
						break;
					}
					case 429: {
						message = "You have made over 10000 requests on this API key today";
						break;
					}
					case 500: {
						message = "Some server error";
						break;
					}
					default:
					{
						message = "Unknown error";
					}
				}
				System.err.println("Error querying IGDB: '" + message + "' for url " + url);
				return null;
			}
			
			return answer.getContent();
		}
		catch(Exception e)
		{
			System.err.println("Error querying IGDB: '" + e.getMessage() + "' for url " + url);
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	/**
	 * Searches IGDB
	 * @param rom name of the rom to look for (file name)
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @return the list of games found
	 * @throws Exception
	 */
	@Override
	public List<Game> search(String rom, String translatedName, Args args) 
	{
		return null; //TODO: implement 
		// test git 2?
	}

}
