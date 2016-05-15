package daaa.QDScrapper.services;

import daaa.qdscrapper.services.ArcadeRoms;
import junit.framework.TestCase;

/**
 * Tests the ArcadeRoms class
 * 
 * @author daaa
 */
public class ArcadeRomsTest extends TestCase {
	
	/**
	 * Tests getting one rom name, triggers the loading of the files
	 */
	public void testGetRoms()
	{
		String title = ArcadeRoms.getRomTitle("sfiii3.zip");
		assertNotNull(title);
		System.out.println("sfiii3.zip = " + title);
	}
}
