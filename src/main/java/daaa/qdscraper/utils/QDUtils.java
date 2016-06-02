package daaa.qdscraper.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import daaa.qdscraper.Args;
import daaa.qdscraper.Props;
import daaa.qdscraper.services.Console;

/**
 * All-purposes utils that had no place anywhere else
 * 
 * @author kerndav
 *
 */
// TODO: this could be split
public class QDUtils
{
	private QDUtils() {} //do not instanciate
	
	private static int unique = 0;
	/**
	 * Generates a unique integer for a run, starting with 1
	 * @return
	 */
	public static int nextInt() {
		unique++;
		return unique;
	}
	
	/**
	 * add tabulations before each line of the input string
	 * @param in the input string to tabulate
	 * @param nb the number of tabulations to add to each line
	 * @return the tabulated string
	 */
	public static String tabulate(String in, int nb) 
	{
		StringBuilder sb = new StringBuilder(in.length());
		String[] lines = in.split("\n");
		
		for(int l=0; l<lines.length; l++)
		{
			String line = lines[l];
			for(int i=0; i<nb; i++) 
			{
				sb.append("\t");
			}
			sb.append(line);
			if(l != lines.length-1) // not the last line
			{
				sb.append("\n");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Removes illegal chars from filenames, in a greedy way (ie: removes windows illegal chars even on unix)
	 * @param name the filename to sanitize, not a path!
	 * @return the sanitized file name
	 */
	public static String sanitizeFilename(String name) {
	    if( null == name ) {
	        return "";
	    }

	    /*if( SystemUtils.IS_OS_LINUX ) {
	        return name.replaceAll( "/+", "" ).trim();
	    }*/

	    return name.replaceAll( "[\u0001-\u001f<>:\"/\\\\|?*\u007f]+", " " ).trim();
	}
	
	
	/**
	 * Max width of the boxart
	 */
	private static int MAX_WIDTH = Integer.valueOf(Props.get("images.maxWidth"));//400; 
	/**
	 * Resizes an image to a max witdh of 400px
	 * @param in the input image
	 * @return the resized image data
	 * @throws IOException
	 */
	public static BufferedImage resizeImage(InputStream in) 
	throws IOException
	{
		BufferedImage srcImage = null;
		try
		{
			srcImage = ImageIO.read(in);
			if(srcImage == null) return null; // happened once with GiantBomb
			
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
	 * Calculates the md5 hash of a file
	 * @param path path to the file to process
	 * @return the md5 hash of a file
	 */
	public static String getMD5(String path)
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(new File(path));
			String md5 = DigestUtils.md5Hex(fis);
			return md5;
		}
		catch(Exception e)
		{
			Console.printErr("Cannot compute md5 of file " + path);
			Console.printErr(e);
		}
		finally
		{
			try{if(fis!=null)fis.close();}catch(IOException e){}
		}
		return null;
	}
	
	
	/* ---------------------------------------------------- */
	/*					 FILE LOADING						*/
	/* ---------------------------------------------------- */
	/**
	 * Loads a text file from the classpath, in particular the files embedded in the released jar
	 * @param name the name of the file to load, which should be at the top of the resources folder
	 * @return the content of the read file
	 */
	public static String loadClasspathFile(String name)
	{
		URL url = QDUtils.class.getClassLoader().getResource(name);
		try {
			return IOUtils.toString(url, Charset.forName("utf-8"));
		} catch (IOException e) {
			Console.printErr(e);
			System.exit(8);
		}
		return null;
	}
	
	
	/**
	 * Loads an excel file from the classpath, in particular the files embedded in the released jar
	 * @param name the name of the file to load, which should be at the top of the resources folder
	 * @return the content of the read file
	 * @throws IOException
	 */
	public static HSSFWorkbook loadClasspathXls(String name) 
	throws IOException
	{
		InputStream in = QDUtils.class.getClassLoader().getResourceAsStream(name);
		HSSFWorkbook wb = new HSSFWorkbook(in);
		in.close();
		return wb;
	}
	
	/**
	 * Loads an xml file from the classpath, in particular the files embedded in the released jar
	 * @param name the name of the file to load, which should be at the top of the resources folder
	 * @return the content of the read file in XML Document format
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static Document loadClasspathXML(String name) 
	throws ParserConfigurationException, SAXException, IOException
	{
		String xml = loadClasspathFile(name);
		return parseXML(xml);
	}
	
	/**
	 * Loads a text properties file from the classpath, in particular the files embedded in the released jar
	 * @param name the name of the file to load, which should be at the top of the resources folder
	 * @return the content of the read file in Properties format
	 */
	public static Properties loadClasspathProperties(String name)
	{
		String content = loadClasspathFile(name);
		if(content != null)
		{
			try
			{
				Properties props = new Properties();
				props.load(new StringReader(content));
				return props;
			}
			catch(IOException e)
			{
				Console.printErr(e);
				System.exit(9);
			}
		}
		
		return null;
	}
	
	/**
	 * Reads a file into a String
	 * @param file the path to the file to open
	 * @return the content of the text file
	 * @throws IOException
	 */
	public static String readFile(String file)
	throws IOException
	{
		return FileUtils.readFileToString(new File(file), Charset.forName("UTF-8"));
	}
	
	/* ---------------------------------------------------- */
	/*							XML							*/
	/* ---------------------------------------------------- */
	
	/**
	 * Parses an xml String
	 * @param xml the xml to parse
	 * @return the XML Document, ready for xpath queries
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static Document parseXML(String xml) 
	throws ParserConfigurationException, SAXException, IOException
	{
		InputSource source = new InputSource(new StringReader(xml));
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		//try {
			db = dbf.newDocumentBuilder();
			Document document = db.parse(source);
			return document;
		/*} catch (ParserConfigurationException | SAXException | IOException e) {
			Console.printErr(e);
			System.exit(10);
		}*/
		//return null;
	}
	
	
	/**
	 * Builds an open xml tag
	 * @param tag the name of the tag to open
	 * @return the open xml tag
	 */
	public static String makeTagOpen(String tag) 
	{
		return makeTagOpen(tag, null);
	}
	/** 
	 * Builds an open xml tag with an attribute
	 * @param tag tag the name of the tag to open 
	 * @param attrName the name of the attribute to add
	 * @param attrValue the value of the attribute to add
	 * @return the open xml tag
	 */
	public static String makeTagOpen(String tag, String attrName, String attrValue)
	{
		Map<String, String> attrs = new HashMap<String, String>();
		if(attrValue != null)
		{
			attrs.put(attrName, attrValue);
		}
		return makeTagOpen(tag, attrs);
	}
	/**
	 * Builds an open xml tag with many attributes
	 * @param tag tag the name of the tag to open 
	 * @param attrs the attributes to add
	 * @return the open xml tag
	 */
	public static String makeTagOpen(String tag, Map<String, String> attrs) 
	{
		String ret =  "<" + tag;
		
		if(attrs != null)
		{
			for(Map.Entry<String, String> entry: attrs.entrySet())
			{
				String key = entry.getKey();
				String val = entry.getValue();
				if(!StringUtils.isEmpty(val))
				{
					ret = ret + " " + key + "=\"" + val + "\"";
				}
			}
		}
		
		ret += ">";
		
		return ret;
	}
	/**
	 * Builds a closed xml tag
	 * @param tag the name of the tag to close
	 * @return the closed xml tag
	 */
	public static String makeTagclosed(String tag) 
	{
		return "</" + tag + ">";
	}
	
	
	private static final XPath XPATH = XPathFactory.newInstance().newXPath();
	/**
	 * Get the xpath to query Documents
	 * @return the xpath to query Documents
	 */
	public static XPath getXPath() {
		return XPATH;
	}
	
	
	/**
	 * Thread.sleep with a message out
	 * @param ms ms to sleep
	 * @param cause a cause message, null or empty to not sysout the cause
	 */
	public static void sleep(long ms, String cause)
	{
		try {
			String message = "Need to sleep " + ms + "ms";
			if(!StringUtils.isEmpty(cause))
			{
				message = ", cause: " + cause;
			}
			Console.println(message);
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			Console.printErr(e);
		}
	}
	
	
	/* ---------------------------------------------------- */
	/*							HTTP						*/
	/* ---------------------------------------------------- */
	
	/**
	 * The lazy loaded http client
	 */
	private static HttpClient HTTP_CLIENT = null;
	
	
	/**
	 * Builds the shared  http client
	 * @param args the app args
	 * @return the http client
	 */
	public static HttpClient getHttpClient(Args args)
	{		
		if(HTTP_CLIENT == null)
		{
			//TODO: add a configurable timeout
			int timeout = 30000; //ms
			
			PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
			connManager.setMaxTotal(1);
			connManager.setDefaultMaxPerRoute(1);
			SocketConfig sc = SocketConfig.custom()
				    .setSoTimeout(timeout)
				    .build();

			connManager.setDefaultSocketConfig(sc);

			// configure the timeouts (socket and connection) for the request
			RequestConfig.Builder config = RequestConfig.copy(RequestConfig.DEFAULT);
			config.setConnectionRequestTimeout(timeout);
			config.setSocketTimeout(timeout);
			
			HttpClientBuilder builder = HttpClients.custom()
					.setConnectionManager(connManager)
		            .setConnectionManagerShared(false);
			
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
			
			// giantbomb wants a user agent absolutely
			builder.setUserAgent(Props.get("http.user-agent")); // "https://github.com/daaa57150/QDScraper"
			
//			RequestConfig config = RequestConfig.custom()
//				    .setSocketTimeout(30 * 1000)
//				    .setConnectTimeout(30 * 1000)
//				    .build();
//			builder.setDefaultRequestConfig(config);
			
			
			
			HTTP_CLIENT = builder.build();
			
			
		}
		return HTTP_CLIENT;
	}
	
	
	/**
	 * Represents an http answer with http code and response content
	 * @author daaa
	 *
	 */
	public static class HttpAnswer
	{
		private int code;
		private String content;
		private String reason;
		/** Constructor */
		public HttpAnswer(int code, String response, String reason){
			this.code = code;
			this.content = response;
			this.reason = reason;
		}
		/** @return the code */
		public int getCode() { return code; }
		/** @param code the code to set */
		public void setCode(int code) { this.code = code; }
		/** @return the content */
		public String getContent() { return content; }
		/** @param content the content to set */
		public void setContent(String content) { this.content = content; }
		/** @return the reason */
		public String getReason() { return reason; }
		/** @param reason the reason to set */
		public void setReason(String reason) { this.reason = reason; }
		/** {@inheritDoc}*/
		@Override
		public String toString(){return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);}
	}
	
	/**
	 * Performs an http get query and returns the code and content as String
	 * @param args app args
	 * @param url the url to get
	 * @return the content of the url with the status code
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpAnswer httpGet(Args args, String url) 
	throws ClientProtocolException, IOException
	{
		return httpGet(args, url, null);
	}
	
	/**
	 * Performs an http get query and returns the code and content as String
	 * @param args app args
	 * @param url the url to get
	 * @param headers headers to add to the request
	 * @return the content of the url with the status code
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public static HttpAnswer httpGet(Args args, String url, Header[] headers)
	throws ClientProtocolException, IOException
	{
	    HttpResponse response1 = null;
	    HttpEntity entity1 = null;
	    InputStream in = null;
	    
	    try
	    {
			HttpClient httpclient = getHttpClient(args);
			HttpGet httpGet = new HttpGet(url);
			if(headers != null)
			{
				for(Header header:headers)
				{
					httpGet.setHeader(header);
				}
			}
			
			int maxTries = 3;
		    String content = null; 
		    String reason = null;
		    int code = 0; 
			for(int currentTry = 0; currentTry<maxTries; currentTry++)
			{
				// close if we're looping
				if(entity1 != null) try { EntityUtils.consume(entity1); } finally{}
				
				response1 = httpclient.execute(httpGet);
				entity1 = response1.getEntity();
				code = response1.getStatusLine().getStatusCode();
			    if(code == 522) // TheGamesDB is down sometimes and throws this
			    {
			    	long sleepSec = (currentTry+1);
			    	sleep(sleepSec * 1000, "error 522");
			    	continue;
			    }
			    
				// else 
			    break;
			}
		
			if(entity1 != null)
			{
				in = entity1.getContent();
				content = IOUtils.toString(in, Charset.forName("UTF-8"));
			}
		    reason = response1.getStatusLine().getReasonPhrase();
		    
		    return new HttpAnswer(code, content, reason);
		}
		finally
		{
			if(entity1 != null) 	try { EntityUtils.consume(entity1); } finally{}
	    	if(in != null) 			try { in.close(); 					} finally{}
		}
	}
	
	
	/**
	 * Downloads an image
	 * @param imageUrl the image to download
	 * @param savePathNoExt the path where to save it, without the extension
	 * @param args app args
	 * @return the path where it was saved (with the extension)
	 * @throws Exception
	 */
	public static String downloadImage(String imageUrl, String savePathNoExt, Args args) 
	throws Exception
	{
		HttpEntity entity1 = null;
	    InputStream in = null;
	    BufferedImage image = null;
		HttpResponse response1 = null;
		
		try
		{
			HttpClient httpclient = getHttpClient(args);
			HttpGet httpGet = new HttpGet(imageUrl);
			
			int code = 0;
			int maxTries = 3;
			for(int currentTry = 0; currentTry<maxTries; currentTry++)
			{
				// close if we're looping
				if(entity1 != null) try { EntityUtils.consume(entity1); } finally{}
				
				response1 = httpclient.execute(httpGet);
				code = response1.getStatusLine().getStatusCode();
				entity1 = response1.getEntity();
				if(code == 522) // TheGamesDB is down sometimes and throws this
				{
					long sleepSec = (currentTry+1);
					sleep(sleepSec * 1000, "error 522");
			    	continue;
				}
				break;
			}
			
			if(code != HttpStatus.SC_OK)
			{
				Console.printErr("Downloading image " + imageUrl + " gave a status of " + code + " => " + response1.getStatusLine().getReasonPhrase());
				return null;
			}
			
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
				// giantbomb sends application/octetstream for some images
				//throw new Exception("Image type " + contentType + " not supported");
				imageType = FilenameUtils.getExtension(imageUrl);
				if(!"jpg".equals(imageType) && !"png".equals(imageType))
				{
					// back to default png, might be externalizable
					imageType="png";
				}
			}
		    
		    in = entity1.getContent();
		    image = QDUtils.resizeImage(in);
		    
		    // giantbomb pb once
		    if(in == null || image == null) {
		    	Console.printErr("Could not download/resize image " + imageUrl);
		    	return null; 
		    }
		    
		    String path = savePathNoExt + "." + imageType;
		    File f = new File(path);
			Files.deleteIfExists(f.toPath());
		    Files.createDirectories(Paths.get(f.getParent()));
			
		    ImageIO.write(image, imageType, f);
		    image.flush();
		    return path;
		}
		catch(Exception e)
		{
			Console.printErr("Error downloading image " + imageUrl);
			throw e;
		}
		finally {
	    	if(entity1 != null) 	try { EntityUtils.consume(entity1); } finally{}
	    	if(in != null) 			try { in.close(); 					} finally{}
	    	if(image != null) 		try { image.flush(); 				} finally{}
	    }
	}
	
}











