package daaa.QDScrapper.services;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicHeader;

import daaa.qdscrapper.Args;
import daaa.qdscrapper.utils.QDUtils;
import daaa.qdscrapper.utils.QDUtils.HttpAnswer;

public class IGDBTest extends TestCase {

	
	public static void testApi() 
	throws ClientProtocolException, IOException
	{
		Args args = new Args(new String[]{"-dir=.", "-platform=test"});
		//HttpClient client = QDUtils.getHttpClient(args);
		//httpGet.setHeader("Authorization", "Token token=\"x2-XsIKkkmEFnAhj5oE33TtqPivfgs5hvfYbtV-Mpl8\"");
		HttpAnswer answer = QDUtils.httpGet(
				args, "https://www.igdb.com/api/v1/games/search?q=metal%20slug", 
				new Header[]{new BasicHeader("Authorization", "Token token=\"x2-XsIKkkmEFnAhj5oE33TtqPivfgs5hvfYbtV-Mpl8\"")} 
			);
		System.out.println(answer);
	}
}
