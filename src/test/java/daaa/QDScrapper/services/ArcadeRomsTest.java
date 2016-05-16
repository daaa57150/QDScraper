package daaa.QDScrapper.services;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import junit.framework.TestCase;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.xml.sax.SAXException;

import daaa.qdscrapper.services.ArcadeRoms;

/**
 * Tests the ArcadeRoms class
 * 
 * @author daaa
 */
public class ArcadeRomsTest extends TestCase {
	
	/**
	 * Tests getting one rom name, triggers the loading of the files
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws XPathExpressionException 
	 */
	public void testGetRoms() throws XPathExpressionException, IOException, ParserConfigurationException, SAXException
	{
		String title = ArcadeRoms.getRomTitle("sfiii3.zip");
		assertNotNull(title);
		System.out.println("sfiii3.zip = " + title);
	}
	
	/**
	 * Tests if querying with xpath is fast enough
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * @throws XPathExpressionException
	 */
	public void testBigXpath() 
	throws ParserConfigurationException, SAXException, IOException, XPathExpressionException
	{
		// roms to look for
		String[] roms = {
				"4dwarrio.zip",
				"1942.zip",
				"benberob.zip",
				"gakupara.zip",
				"iceclmrj.zip",
				"magspot2.zip",
				"psychic5.zip",
				"splndrbt.zip",
				"tshoot.zip",
				"zzyzzyxx.zip"
			}; 
		
		long startTime = System.nanoTime();
		
		for(String rom: roms)
		{
			String title = ArcadeRoms.getRomTitle(rom);
			if(title != null)
			{
				System.out.println(title);
			}
			else
			{
				System.out.println("Didn't find " + rom);
			}
		}
		
		long estimatedTime = System.nanoTime() - startTime;
		long milli = TimeUnit.NANOSECONDS.toMillis(estimatedTime);
		String format = DurationFormatUtils.formatDuration(milli, "s,SSS");
		System.out.println("Took " + format + "s" + " to find " + roms.length + " roms");
	}
}
