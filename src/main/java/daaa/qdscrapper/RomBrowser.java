package daaa.qdscrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;


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
	public List<String> listRoms(String inFolder) 
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
	        	 if(filename.startsWith("DUPE")) return false;
	        	 
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
	 * Is this file a known bios?
	 * @param filename
	 * @return true if we know it's a bios file
	 */
	public boolean isBios(String filename)
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
		for(String bios: getBiosFiles())
		{
			BIOS_FILES.add(bios.toLowerCase());
		}
	}
	
	/**
	 * List of all bios files I know of, should be externalized in a file
	 * @return
	 */
	private static String[] getBiosFiles()
	{
		String[] files = {
				"32X_G_BIOS.BIN",
				"32X_M_BIOS.BIN",
				"32X_S_BIOS.BIN",
				"5200.rom",
				"7800 BIOS (U).rom",
				"acpsx.zip",
				"ar_bios.zip",
				"atluspsx.zip",
				"atpsx.zip",
				"BIOS_CD_E.bin",
				"BIOS_CD_J.bin",
				"BIOS_CD_U.bin",
				"bios.rom",
				"BS-X.bin",
				"CARTS.SHA",
				"coleco.rom",
				"crysbios.zip",
				"CYRILLIC.FNT",
				"decocass.zip",
				"DISK.ROM",
				"disksys.rom",
				"Dtlh3000.bin",
				"Dtlh3002.bin",
				"ecs.bin",
				"eu_mcd2_9306.bin",
				"exec.bin",
				"FMPAC.ROM",
				"FMPAC16.ROM",
				"gba_bios.bin",
				"grom.bin",
				"hng64.zip",
				"ITALIC.FNT",
				"ivoice.bin",
				"jp_mcd1_9112.bin",
				"KANJI.ROM",
				"konamigv.zip",
				"konamigx.zip",
				"lynxboot.img",
				"macsbios.zip",
				"maxaflex.zip",
				"megaplay.zip",
				"megatech.zip",
				"MSX.ROM",
				"MSX2.ROM",
				"MSX2EXT.ROM",
				"MSX2P.ROM",
				"MSX2PEXT.ROM",
				"MSXDOS2.ROM",
				"neogeo.zip",
				"nss.zip",
				"o2rom.bin",
				"PAINTER.ROM",
				"panafz10.bin",
				"pcfx.rom",
				"pgm.zip",
				"playch10.zip",
				"psarc95.zip",
				"RS232.ROM",
				"SCPH1001.BIN",
				"scph5500.bin",
				"scph5501.bin",
				"scph5502.bin",
				"scph5552.bin",
				"scph7003.bin",
				"skns.zip",
				"stvbios.zip",
				"syscard3.pce",
				"tos.img",
				"us_scd2_9306.bin"
			};
		return files;
	}
	
}











