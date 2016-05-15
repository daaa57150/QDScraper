package daaa.qdscrapper.utils;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import daaa.qdscrapper.Args;

/**
 * All-purposes utils that had no place anywhere else
 * 
 * @author kerndav
 *
 */
public class QDUtils
{
	private QDUtils() {} //do not instanciate
	
	/**
	 * add tabulations before each lines of the input string
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
				ret = ret + " " + entry.getKey() + "=\"" + entry.getValue() + "\"";
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
	
	
	/* ---------------------------------------------------- */
	/*							HTTP						*/
	/* ---------------------------------------------------- */
	
	// TODO: move it to a network or utils class
	private static HttpClient httpClient = null;
	public static HttpClient getHttpClient(Args args)
	{		
		if(httpClient == null)
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
			
			/*RequestConfig config = RequestConfig.custom()
				    .setSocketTimeout(10 * 1000)
				    .setConnectTimeout(10 * 1000)
				    .build();
			builder.setDefaultRequestConfig(config);*/
			
			//TODO: add a configurable timeout
			
			httpClient = builder.build();
			
			
		}
		return httpClient;
	}
	
}











