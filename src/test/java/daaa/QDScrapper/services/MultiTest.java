package daaa.QDScrapper.services;

import daaa.qdscrapper.utils.RomCleaner;
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
	
}
