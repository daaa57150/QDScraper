package daaa.qdscrapper.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.imgscalr.Scalr.Mode;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import daaa.qdscrapper.Args;
import daaa.qdscrapper.Props;
import daaa.qdscrapper.model.Game;

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
	 * Finds the perfect match in the list of games
	 * @param games
	 * @return
	 */
	public static Game findPerfectMatch(List<Game> games)
	{
		for(Game game: games)
		{
			if(game.getScore() == 1.0) return game;
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
			return IOUtils.toString(url);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(8);
		}
		return null;
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
				e.printStackTrace();
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
			e.printStackTrace();
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
			HttpClientBuilder builder = HttpClientBuilder.create();
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
			builder.setUserAgent(Props.get("http.user-agent")); // "https://github.com/daaa57150/QDScrapper"
			
			/*RequestConfig config = RequestConfig.custom()
				    .setSocketTimeout(10 * 1000)
				    .setConnectTimeout(10 * 1000)
				    .build();
			builder.setDefaultRequestConfig(config);*/
			
			//TODO: add a configurable timeout
			
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
		HttpClient httpclient = getHttpClient(args);
		HttpGet httpGet = new HttpGet(url);
		if(headers != null)
		{
			for(Header header:headers)
			{
				httpGet.setHeader(header);
			}
		}
		HttpResponse response1 = httpclient.execute(httpGet);
		HttpEntity entity1 = null;
		try {
		    entity1 = response1.getEntity();
		    String content = IOUtils.toString(entity1.getContent(), Charset.forName("UTF-8"));
		    int code = response1.getStatusLine().getStatusCode();
		    String reason = response1.getStatusLine().getReasonPhrase();
		    return new HttpAnswer(code, content, reason);
		} finally {
		    if(entity1 != null) {
		    	EntityUtils.consume(entity1);
		    }
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
		try
		{
			HttpClient httpclient = getHttpClient(args);
			HttpGet httpGet = new HttpGet(imageUrl);
			HttpResponse response1 = httpclient.execute(httpGet);
			
		    //System.out.println(response1.getStatusLine());
		    HttpEntity entity1 = null;
		    InputStream in = null;
		    BufferedImage image = null;
		    try {
			    entity1 = response1.getEntity();
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
						// TODO: convert gif to jpeg or png
						throw new Exception("Image type " + contentType + " with extension "+imageType+" not supported");
					}
				}
			    
			    in = entity1.getContent();
			    image = QDUtils.resizeImage(in);
			    
			    //String filename = buildFileName(name, matchIndex, imageType);
				//String path = (matchIndex > 1 ? args.dupesDir + DUPE_IMAGES_FOLDER + File.separatorChar : args.romsDir + "downloaded_images"+File.separatorChar) + filename;
			    String path = savePathNoExt + "." + imageType;
			    File f = new File(path);
				Files.deleteIfExists(f.toPath());
			    Files.createDirectories(Paths.get(f.getParent()));
				
			    ImageIO.write(image, imageType, f);
			    image.flush();
			    return path;
		    }
		    finally {
		    	if(entity1 != null) 	try { EntityUtils.consume(entity1); } finally{}
		    	if(in != null) 			try { in.close(); 					} finally{}
		    	if(image != null) 		try { image.flush(); 				} finally{}
		    }
		}
		catch(Exception e)
		{
			System.err.println("Error downloading image " + imageUrl);
			throw e;
		}
	}
	
}











