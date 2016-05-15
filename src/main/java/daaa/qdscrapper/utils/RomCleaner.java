package daaa.qdscrapper.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * Utilities to clean rom names 
 * 
 * @author kerndav
 *
 */
public class RomCleaner
{
	// TODO: precompile all regexes
	
	private RomCleaner() {} //do not instanciate
	
	/**
	 * Replaces multiple blank characters with a single space
	 * @param s the string to clean
	 * @return the cleaned string
	 */
	private static String removeMultiSpaces(String s)
	{
		return s.replaceAll("\\s+", " ");
	}
	
	/**
	 * removes everything between () or [], if hard is specified also removes special characters
	 * @param hard
	 * @return
	 */
	public static String cleanRomName(String rom, boolean hard)
	{
		String cleanRom = removeExtension(rom);
		cleanRom = cleanRom.replaceAll("\\([^\\)]*\\)", "");
		cleanRom = cleanRom.replaceAll("\\[[^\\]]*\\]", "");
		
		if(hard)
		{
			cleanRom = cleanRom.replaceAll("[!-:,;.%?_']", "");
		}
		
		cleanRom = removeMultiSpaces(cleanRom);
		cleanRom = cleanRom.trim();
		
		return cleanRom;
	}
	
	/**
	 * compares the hard cleaned names of 2 roms 
	 * @param rom1
	 * @param rom2
	 * @return
	 */
	public static boolean isSameRom(String rom1, String rom2)
	{
		rom1 = cleanRomName(rom1, true);
		rom1 = rom1.toLowerCase();
		rom2 = cleanRomName(rom2, true);
		rom2 = rom2.toLowerCase();
		
		return rom1.equals(rom2);
	}
	
	/**
	 * Removes a file's extension, limitation is that the extension cannot be more than 3 chars long 
	 * or it will be ignored. this is because some games have a dot in their names (Capcom Vs. SNK)
	 * @param filename the filename to clean
	 * @return the filename without its extension
	 */
	public static String removeExtension(String filename) {
		String clean = filename;
		if(filename.lastIndexOf(".") >= filename.length()-4)
		{
			clean = FilenameUtils.removeExtension(filename);			
		}
		return clean;
	}
	
	
	/**
	 * Removes content between () and/or []
	 * @param filename the filename to clean
	 * @param cleanFilename things to clean, if it contains '(' remove everything between (), if it contains '[', remove everything between []
	 * @return the cleaned filename
	 */
	public static String cleanWithArgs(String filename, String cleanFilename)
	{
		String clean = removeExtension(filename);
		if(!StringUtils.isEmpty(cleanFilename))
		{
			if(cleanFilename.contains("(")) {
				clean = clean.replaceAll("\\([^\\)]*\\)", "");
			}
			if(cleanFilename.contains("[")) {
				clean = clean.replaceAll("\\[[^\\]]*\\]", "");
			}

			clean = removeMultiSpaces(clean);
		}
		
		return clean.trim();
	}


	private static final Pattern PATTERN_LANGUAGES = Pattern.compile("\\((?:(?:En|Fr|De|Es|It|Nl|Sv|Ja|Pt|No|Da)[,+]? ?)*\\)", Pattern.CASE_INSENSITIVE); 
	/**
	 * Removes the language indicators, in this format: (En,Fr,De)
	 * Currently knows about En|Fr|De|Es|It|Nl|Sv|Ja|Pt|No|Da
	 * 
	 * @param filename the file name to clean 
	 * @return the cleaned filename
	 */
	public static String removeLanguageIndication(String filename)
	{
		Matcher m = PATTERN_LANGUAGES.matcher(filename);
		return removeMultiSpaces(m.replaceAll(""));
	}
	
	private static final Pattern PATTERN_REGION = Pattern.compile("\\((?:(?:USA|Europe|World|Japan|UE|France|Germany|Spain|Denmark|Italy),? ?)*\\)", Pattern.CASE_INSENSITIVE);
	/**
	 * Removes the region indicator, in this format: (USA,Europe)
	 * Currently knows about Europe|World|Japan|UE|France|Germany|Spain|Denmark|Italy
	 * 
	 * @param filename the file name to clean 
	 * @return the cleaned filename
	 */
	public static String removeRegionIndication(String filename) 
	{
		Matcher m = PATTERN_REGION.matcher(filename);
		return removeMultiSpaces(m.replaceAll(""));
	}
	
	private static final Pattern PATTERN_TYPE =  Pattern.compile("\\((?:Rev .|Beta|[^()]*Proto|(?:19|20)..[^()]*|Unl|Demo|Sample)\\)", Pattern.CASE_INSENSITIVE);
	/**
	 * Removes the type attribute in the form (Beta 1)
	 * Currently knows about Rev x, Beta, *Proto, 19xx-xx-xx, Unl, Demo, Sample (Should Unl &amp;Sample be stripped?)
	 * 
	 * @param filename the file name to clean 
	 * @return the cleaned filename
	 */
	public static String removeTypeIndicators(String filename)
	{
		Matcher m = PATTERN_TYPE.matcher(filename);
		return removeMultiSpaces(m.replaceAll(""));	
	}
}








