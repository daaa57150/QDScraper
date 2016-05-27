package daaa.QDScrapper.services;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import daaa.qdscraper.services.ScummVMRoms;

/**
 * Tests the ArcadeRoms class
 * 
 * @author daaa
 */
public class ScummVMRomsTest extends TestCase {
	
	/**
	 * Tests getting one rom name, triggers the loading of the files
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws XPathExpressionException 
	 */
	public void testGetRom() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException
	{
		String title = ScummVMRoms.getRomTitle("tentacle.scummvm");
		assertNotNull(title);
		System.out.println("tentacle.scummvm = " + title);
	}
	
	/**
	 * Tests getting all the roms
	 * @throws IOException
	 */
	public void testGetAllRoms() 
	throws IOException
	{
		for(Map.Entry<String, String> entry : ScummVMRoms.getRoms().entrySet())
		{
			System.out.println(entry.getKey() + " => " + entry.getValue());
			assertNotNull(entry.getValue());
			assertTrue(StringUtils.isNotEmpty(entry.getValue()));
		}
	}
	
}
