package daaa.qdscrapper.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import daaa.qdscrapper.utils.QDUtils;



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
	 * List of roms
	 */
	private static Map<String, String> ROMS = null;
	/**
	 * List of files to load
	 */
	private static String[] FILES = {"MAME.xml", "Final Burn Alpha.xml"}; //TODO: externalize
	
	
	
	/**
	 * Lazy init of rom loading
	 * @return the roms
	 */
	private static Map<String, String> getRoms() 
	{
		if(ROMS == null)
		{
			try {
				loadRoms();
			} catch (XPathExpressionException | ParserConfigurationException | SAXException | IOException e) {
				e.printStackTrace();
				System.exit(12);
			}
		}
		return ROMS;
	}
	
	/**
	 * Loads the files with the arcade roms
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 */
	// TODO: this is way too slow, try parsing on demand instead
	private static void loadRoms() 
	throws ParserConfigurationException, SAXException, IOException, XPathExpressionException
	{
		System.out.println("Loading arcade games...");
		
		ROMS = new HashMap<>();
		for(String file: FILES)
		{
			Document document = QDUtils.loadClasspathXML(file);
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			
			// header
			String listName = xpath.evaluate("menu/header/listname", document);
			String lastlistupdate = xpath.evaluate("menu/header/lastlistupdate", document);
			String listversion = xpath.evaluate("menu/header/listversion", document);
			String exporterversion = xpath.evaluate("menu/header/exporterversion", document);
			
			System.out.println("Processing " + file);
			System.out.println("\tlist name: " + listName);
			System.out.println("\tlast list update: " + lastlistupdate);
			System.out.println("\tlist version: " + listversion);
			System.out.println("\texported version: " + exporterversion);
			
			// games
			int i=1;
			for(i=1; ; i++)
			{
				String rom = xpath.evaluate("menu/header/game["+i+"]/@name", document);
				if(rom == null) break; // nothing more to process
				rom += ".zip";
				
				if(i!=1 && (i-1)%80==0)
				{
					System.out.println();
				}
				System.out.print(".");
				
				if(!ROMS.containsKey(rom))
				{
					// TODO: could also store manufacturer, year, genre in the case theGamesDb doesn't know anything
					String title = xpath.evaluate("menu/header/game["+i+"]/description", document);
					ROMS.put(rom,  title);
				}
			}
			
			System.out.println();
			System.out.println("processed " + (i-1) + "games");
			System.out.println();
		}
	}
	
	/**
	 * Get the name of the game from the name of the rom
	 * @param rom the name of the rom, with the .zip extension
	 * @return the name of the game if it's known
	 */
	public static String getRomTitle(String rom)
	{
		// TODO: query xml here
		Map<String, String> roms = getRoms();
		return roms.get(rom);
	}
}








