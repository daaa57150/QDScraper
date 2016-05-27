package daaa.qdscrapper.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

import daaa.qdscrapper.utils.QDUtils;
import daaa.qdscrapper.utils.RomCleaner;


/**
 * This class knows the name of the game given the name of the scummvm rom.
 * => sfiii3.zip = Street Fighter III 3rd strike
 * It uses files from http://hyperlist.hyperspin-fe.com/
 * 
 * @author daaa
 */
public class ScummVMRoms {
	
	/** Translates short name to real name, lazily loaded */
	private static Map<String, String> roms = null;
	
	/**
	 * Get the name of the game from the name of the rom
	 * @param rom the name of the rom, with the .zip extension
	 * @return the name of the game if it's known
	 * @throws IOException
	 */
	public static String getRomTitle(String rom)
	throws IOException 
	{
		Map<String, String> allRoms = getRoms();
		rom = RomCleaner.removeExtension(rom);
		return allRoms.get(rom);
	}
	
	/**
	 * Get all the roms (short name => real name) available
	 * @return All the roms
	 * @throws IOException
	 */
	public static Map<String, String> getRoms() 
	throws IOException
	{
		if(roms == null)
		{
			roms = new HashMap<String, String>();
			
			HSSFWorkbook wb = QDUtils.loadClasspathXls("ScummVM.xls");
			HSSFSheet sheet = wb.getSheetAt(0);
			int nbRows = sheet.getLastRowNum();
			int colShortName = 1;
			int colFullName = 0;
			
			for(int i=0; i<=nbRows; i++)
			{
				HSSFRow row = sheet.getRow(i);
				
				HSSFCell shortCell = row.getCell(colShortName, MissingCellPolicy.CREATE_NULL_AS_BLANK);
				String shortName = shortCell.getStringCellValue();
				
				HSSFCell fullCell = row.getCell(colFullName, MissingCellPolicy.CREATE_NULL_AS_BLANK);
				String fullName = fullCell.getStringCellValue();
				
				if(StringUtils.isNoneEmpty(shortName, fullName))
				{
					roms.put(shortName.trim(), fullName.trim());
				}
			}
		}
		
		return roms;
	}

}
