package daaa.qdscraper.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import daaa.qdscraper.Args;
import daaa.qdscraper.Props;
import daaa.qdscraper.model.Rom;
import daaa.qdscraper.utils.QDUtils;


/**
 * Utility class to find roms in a folder
 * 
 * @author daaa
 *
 */
public class RomBrowser {

	private RomBrowser(){} // do not instanciate
	
	/**
	 * If args.arcade or args.platform == scummvm, translate the name
	 * @param rom the rom to set the translated name to
	 * @param args app's args
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws XPathExpressionException 
	 */
	private static void setTranslatedName(Rom rom, Args args) 
	throws XPathExpressionException, IOException, ParserConfigurationException, SAXException
	{
		String file = rom.getFile();
		String name = rom.getFile(); // for arcade/scumm, the name is our match in the DB, for the rest it's the rom => should we get rid of the possible relative path?
		
		
		//if it's an arcade game, look in the arcade roms "DB"
		if(args.arcade)
		{
			name = ArcadeRoms.getRomTitle(name); // can be null here => checked by app later
			rom.setIsTranslated(true);
		}
		
		// if it's a scummvm game, look in the scummvm roms "DB"
		else if("scummvm".equals(args.platform))
		{
			name = ScummVMRoms.getRomTitle(file);  // can be null here => checked by app later
			rom.setIsTranslated(true);
		}
		
		rom.setTranslatedName(name);
	}
	
	
	
	/**
	 * Finds roms in a directory, non recursively
	 * 
	 * @param inFolder the folder to look for the roms
	 * @param args app's args
	 * @return the list of found roms
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 */
	public static List<Rom> listRomsInFolder(String inFolder, Args args) 
	throws IOException, XPathExpressionException, ParserConfigurationException, SAXException 
	{
		List<Rom> roms = new ArrayList<>();
		
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
	         public boolean accept(Path path) throws IOException {
	             
	        	 String filename = path.getFileName().toString();
	        	 String ext = FilenameUtils.getExtension(path.getFileName().toString());
	        	 File file = path.toFile();
	        			 
	        	 
	        	 // no directory
	        	 if(file.isDirectory()) return false;
	        	 
	        	 // no dupe that may have been created by a previous run
	        	 if(filename.startsWith(Props.get("dupes.prefix"))) return false;
	        	 
	        	 // OS or working files 
	        	 if(filename.startsWith(".")) return false;
	        	 
	        	 // don't process hidden files
	        	 if(file.isHidden()) return false;
	        	 
	        	 // remove extensions we know are not roms and may be there
	        	 if(ext.toLowerCase().matches("xml|txt|jpg|png|htm|html|doc|docx|ini|xls")) return false;
	        	 
	        	 
	        	 return true;
	         }
	     };
		
		
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(inFolder), filter);
        		
        for (Path path : directoryStream) // wrong for scummvm
        {
        	String filename = path.getFileName().toString();
        	Rom rom = new Rom();
        	rom.setPath(path.toString());
        	rom.setFile(filename);
        	rom.setIsBios(isBios(filename));
        	rom.setIsRealFile(true);
        	setTranslatedName(rom, args);
        	roms.add(rom);
        }
        
        return roms;
	}
	
	/**
	 * Lists roms given the args:
	 * - if a rom file is given, list that
	 * - if scummvm, find only .scummvm files in subdirectories 
	 * - otherwise list files non recursively
	 * @param args app's args
	 * @return the list of roms
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 */
	public static List<Rom> listRoms(Args args) 
	throws IOException, XPathExpressionException, ParserConfigurationException, SAXException
	{
		if(!StringUtils.isEmpty(args.romFile))
		{
			return listRomsFromFile(args.romFile, args);
		}
		
		if("scummvm".equals(args.platform))
		{
			//TODO: implement scummvm folders
		}
		
		// else
		return listRomsInFolder(args.romsDir, args);
	}
	
	/**
	 * Reads rom names from a file, each line should be a rom.
	 * Lines starting with # are ignored as are blank lines
	 * @param file the file to read the roms from
	 * @param args app's args
	 * @return the list of roms
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 */
	public static List<Rom> listRomsFromFile(String file, Args args) 
	throws IOException, XPathExpressionException, ParserConfigurationException, SAXException
	{
		String content = QDUtils.readFile(file);
		String[] lines = content.split("[\n\r]");
		List<Rom> roms = new ArrayList<Rom>(lines.length);
		
		for(String line:lines)
		{
			// remove # so we can have comments
			// remove blanks so we can have empty lines
			if(!line.startsWith("#") && !StringUtils.isBlank(line))
			{
				line = line.trim();
	        	Rom rom = new Rom();
	        	rom.setPath(args.romsDir + line);
	        	rom.setFile(line);
	        	rom.setIsBios(isBios(line));
	        	rom.setIsRealFile(false);
	        	setTranslatedName(rom, args);
	        	roms.add(rom);
			}
		}
		
		return roms;
	}
	
	/**
	 * Is this file a known bios?
	 * @param filename
	 * @return true if we know it's a bios file
	 */
	public static boolean isBios(String filename)
	{
		// no intro romsets mark their bios files with [BIOS]
		if(filename.contains("[BIOS]")) return true;
		
		// the bios files I found referenced here and there (recalbox bios pack, mamedb, openEmu)
		return BIOS_FILES.contains(filename.toLowerCase());
	}
	
	/**
	 * Bios files in lowercase
	 */
	private static List<String> BIOS_FILES = null;
	static {
		String content = QDUtils.loadClasspathFile("bios.txt");
		String[] biosFiles = content.split("\\n");
		BIOS_FILES = Arrays.asList(biosFiles);
	}
	
}











