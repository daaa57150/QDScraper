package daaa.QDScraper.services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.ibm.icu.text.Normalizer2;

import daaa.qdscraper.Args;
import daaa.qdscraper.services.api.ApiService;
import daaa.qdscraper.utils.CryptoUtils;
import daaa.qdscraper.utils.QDUtils;
import daaa.qdscraper.utils.QDUtils.HttpAnswer;
import daaa.qdscraper.utils.RomCleaner;
import junit.framework.TestCase;

/**
 * Some random tests
 * 
 * @author kerndav
 */
public class MultiTest extends TestCase {

	public static void testRegexWordBoundary()
	{
		String rom1 = "The legend of zelda";
		String rom2 = "Legend of zelda, the";
		
		rom1 = RomCleaner.cleanRomName(rom1, true);
		rom1 = rom1.toLowerCase();
		rom1 = rom1.replaceAll("\\bthe\\b", "");
		rom1 = RomCleaner.removeMultiSpaces(rom1);
		
		rom2 = RomCleaner.cleanRomName(rom2, true);
		rom2 = rom2.toLowerCase();
		rom2 = rom2.replaceAll("\\bthe\\b", "");
		rom2 = RomCleaner.removeMultiSpaces(rom2);
		
		assertTrue(rom1.trim().equals(rom2.trim()));
	}
	
	public static void testJaroWinklerScore()
	{
		List<String> compareMe = new ArrayList<String>();
		
		compareMe.add("The legend of zelda");
		compareMe.add("Legend of zelda, the");
		
		compareMe.add("Metal Slug - Super Vehicle-001");
		compareMe.add("Metal Slug");
		
		compareMe.add("Metal Slug - Super Vehicle-001");
		compareMe.add("Metal Slug X");
		
		compareMe.add("Mærchen Maze");
		compareMe.add("Märchen Maze");

		compareMe.add("Marchen Maze");
		compareMe.add("Märchen Maze");
		
		for(int i=0; i<compareMe.size()-1; i+=2)
		{
			processJarodWinkler(compareMe.get(i), compareMe.get(i+1));
		}
	}
	
	private static void processJarodWinkler(String s1, String s2)
	{
		double score = ApiService.scoreComparison(s1, s2);//StringUtils.getJaroWinklerDistance(s1, s2);
		System.out.println(s1 + " // " + s2 + " => " + score);
	}
	
	public static void testNormalizeString()
	{
		String[] ss = {"Mærchen Maze", "Märchen Maze", "Maerchen Maze", "¼½¾ÆæĲĳŒœﬀﬁﬂﬃﬄﬅﬆ"};
		
		
		
		
		for(String s: ss)
		{
			System.out.println("## " + s + " : ");
			System.out.println("Apache => " + StringUtils.stripAccents(s));
			System.out.println("J NFC  => " + Normalizer.normalize(s, Form.NFC));
			System.out.println("J NFD  => " + Normalizer.normalize(s, Form.NFD));
			System.out.println("J NFKC => " + Normalizer.normalize(s, Form.NFKC));
			System.out.println("J NFKD => " + Normalizer.normalize(s, Form.NFKD));
			System.out.println("I NFC  => " + Normalizer2.getNFCInstance().normalize(s));
			System.out.println("I NFD  => " + Normalizer2.getNFDInstance().normalize(s));
			System.out.println("I NFKCC=> " + Normalizer2.getNFKCCasefoldInstance().normalize(s));
			System.out.println("I NFKC => " + Normalizer2.getNFKCInstance().normalize(s));
			System.out.println("I NFCKD=> " + Normalizer2.getNFKDInstance().normalize(s));
			System.out.println();
		}
	}
	
	public static void testAsList()
	{
		/*String[] wantedPlatforms = null;
		List<String> wpList = Arrays.asList(wantedPlatforms);
		wpList.contains("coucou");
		*/
		// EXCEPTION
	}
	
	public static void testEncryptDecrypt() 
	throws GeneralSecurityException, IOException
	{	
		String toEncrypt = "bob";
		String crypted = CryptoUtils.encrypt(toEncrypt);
		System.out.println(crypted);
		String decrypted = CryptoUtils.decrypt(crypted);
		assertEquals(toEncrypt, decrypted);
	}
	
	public static void testSSL() throws ClientProtocolException, IOException, NoSuchAlgorithmException, KeyManagementException {
		String ss = "https://www.screenscraper.fr/api/jeuInfos.php?devid=daaa&devpassword=AScaredAnonZee&softname=QDScraper&output=xml&romtype=rom&systemeid=105&md5=220b172a4b2d029d352b16299210b369";
		Args args = new Args(new String[]{"-dir=plop", "-platform=snes"});
		HttpAnswer answer = QDUtils.httpGet(args, ss);
		System.out.println(answer);
		
//		/*
//	     *  fix for
//	     *    Exception in thread "main" javax.net.ssl.SSLHandshakeException:
//	     *       sun.security.validator.ValidatorException:
//	     *           PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException:
//	     *               unable to find valid certification path to requested target
//	     */
//	    TrustManager[] trustAllCerts = new TrustManager[] {
//	       new X509TrustManager() {
//	          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
//	            return null;
//	          }
//
//	          public void checkClientTrusted(X509Certificate[] certs, String authType) {  }
//
//	          public void checkServerTrusted(X509Certificate[] certs, String authType) {  }
//
//	       }
//	    };
//
//	    SSLContext sc = SSLContext.getInstance("SSL");
//	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
//	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//
//	    // Create all-trusting host name verifier
//	    HostnameVerifier allHostsValid = new HostnameVerifier() {
//	        public boolean verify(String hostname, SSLSession session) {
//	          return true;
//	        }
//	    };
//	    // Install the all-trusting host verifier
//	    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//	    /*
//	     * end of the fix
//	     */
//	    
//	    URL url = new URL(ss);
//	    URLConnection con = url.openConnection();
//	    Reader reader = new InputStreamReader(con.getInputStream());
//	    while (true) {
//	      int ch = reader.read();
//	      if (ch==-1) {
//	        break;
//	      }
//	      System.out.print((char)ch);
//	    }
	}
}













