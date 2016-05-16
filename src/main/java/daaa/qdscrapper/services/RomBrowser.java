package daaa.qdscrapper.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import daaa.qdscrapper.Props;
import daaa.qdscrapper.utils.QDUtils;


/**
 * Utility class to find roms in a folder
 * 
 * @author daaa
 *
 */
public class RomBrowser {

	private RomBrowser(){} // do not instanciate
	
	/**
	 * Finds roms in a directory, non recursively
	 * 
	 * @param inFolder the folder to look for the roms
	 * @return the list of found roms
	 * @throws IOException
	 */
	public static List<String> listRomsInFolder(String inFolder) 
	throws IOException 
	{
		List<String> fileNames = new ArrayList<>();
		
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
	         public boolean accept(Path path) throws IOException {
	             
	        	 String filename = path.getFileName().toString();
	        	 String ext = FilenameUtils.getExtension(path.getFileName().toString());
	        	 File file = path.toFile();
	        			 
	        	 
	        	 // no directory
	        	 if(file.isDirectory()) return false;
	        	 
	        	 // no dupe that may have been created vy a previous run
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
        		
        for (Path path : directoryStream) 
        {
            fileNames.add(path.toString());
        }
        
        return fileNames;
	}
	
	/**
	 * Reads rom names from a file, each line should be a rom.
	 * Lines starting with # are ignored as are blank lines
	 * @param file the file to read the roms from
	 * @return the list of roms
	 * @throws IOException
	 */
	public static List<String> listRomsFromFile(String file) 
	throws IOException
	{
		String content = QDUtils.readFile(file);
		String[] roms = content.split("[\n\r]");
		List<String> res = new ArrayList<String>(roms.length);
		
		for(String rom:roms)
		{
			// remove # so we can have comments
			// remove blanks so we can have empty lines
			if(!rom.startsWith("#") && !StringUtils.isBlank(rom))
			{
				res.add(rom.trim());
			}
		}
		
		return res;
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











