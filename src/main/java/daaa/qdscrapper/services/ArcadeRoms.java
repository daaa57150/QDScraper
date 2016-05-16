package daaa.qdscrapper.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import daaa.qdscrapper.Props;
import daaa.qdscrapper.utils.QDUtils;
import daaa.qdscrapper.utils.RomCleaner;



/**
 * This class knows the name of the game given the name of the arcade rom.
 * => sfiii3.zip = Street Fighter III 3rd strike
 * It uses files from http://hyperlist.hyperspin-fe.com/
 * 
 * @author daaa
 */
public class ArcadeRoms {
	private ArcadeRoms(){} // do not instanciate
	
	/**
	 * List of files to load
	 */
	private static String[] FILES = Props.get("arcade.files").split(",");//{"MAME.xml", "Final Burn Alpha.xml"};

	/**
	 * The xml documents
	 */
	private static List<Document> DOCUMENTS = null;
	
	/**
	 * Parses the xml files do documents ready for xpath
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static List<Document> getRomFiles() 
	throws ParserConfigurationException, SAXException, IOException
	{
		if(DOCUMENTS == null)
		{
			DOCUMENTS = new ArrayList<>();
			for(String file: FILES)
			{
				Document document = QDUtils.loadClasspathXML(file);
				DOCUMENTS.add(document);
			}
		}
		return DOCUMENTS;
	}
	
	
	
	// static xpath to go a bit faster
	private static XPathFactory XPATHFACTORY = XPathFactory.newInstance();
	private static XPath XPATH = XPATHFACTORY.newXPath();
	
	/**
	 * Get the name of the game from the name of the rom
	 * @param rom the name of the rom, with the .zip extension
	 * @return the name of the game if it's known
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 */
	public static String getRomTitle(String rom) 
	throws IOException, XPathExpressionException, ParserConfigurationException, SAXException
	{
		rom = RomCleaner.removeExtension(rom);
		for(Document doc: getRomFiles())
		{
			Element node = (Element) XPATH.evaluate("menu/game[@name='"+rom+"']", doc, XPathConstants.NODE);
			if(node != null)
			{
				try
				{
					String desc = node.getElementsByTagName("description").item(0).getTextContent();
					return desc;
				}
				catch(Exception e) // for nullPointers, temporary
				{
					e.printStackTrace();
					return null;
				}
			}
		}
		
		return null;
	}
}








