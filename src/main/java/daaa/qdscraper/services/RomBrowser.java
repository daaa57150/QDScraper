package daaa.qdscraper.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import jwbroek.cuelib.CueParser;
import jwbroek.cuelib.CueSheet;
import jwbroek.cuelib.FileData;

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
		// for arcade/scumm, the name is our match in the DB, for the rest it's the same as the file, but only the file itself, no path
		String name = Paths.get(rom.getFile()).getFileName().toString(); 
		
		
		//if it's an arcade game, look in the arcade roms "DB"
		if(args.arcade)
		{
			name = ArcadeRoms.getRomTitle(name); // can be null here => checked by app later
			rom.setIsTranslated(true);
		}
		
		// if it's a scummvm game, look in the scummvm roms "DB"
		else if("scummvm".equals(args.platform))
		{
			name = ScummVMRoms.getRomTitle(name);  // can be null here => checked by app later
			rom.setIsTranslated(true);
		}
		
		rom.setTranslatedName(name);
	}
	
	/**
	 * Tells if a path can be a rom or a bios
	 * @param path tha path to test
	 * @return true if this can be a rom
	 * @throws IOException
	 */
	private static boolean acceptAsRom(Path path)
	throws IOException
	{
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
	   	String[] extensions = {
	   		// common files
	   		"xml","txt","jpg","png","gif","htm","html","doc","docx","ini","xls","dat",
	   		// built by fba
	   		"nv","fs"
	   	};
	   	if(ext.toLowerCase().matches(StringUtils.join(extensions, "|"))) return false;
	   	 
	   	 
	   	return true;
	}
	
	/**
	 * Finds the rom that has a specific name
	 * @param roms the roms to search into
	 * @param name the file name to find
	 * @return the found rom, null if none was found
	 */
	private static Rom findRomWithName(List<Rom> roms, String name)
	{
		for(Rom rom: roms)
		{
			if(name.toLowerCase().equals(rom.getFile().toLowerCase())) return rom;
		}
		return null;
	}
	
	/**
	 * Flags duplicates, for example psx isos usually have a .cue and several .bin, all .bin are duplicates
	 * @param roms the roms to flag
	 */
	// TODO: some games have no cuesheet and still have multiple .bin ?? Micro Maniacs Europe
	private static void flagAuxiliaries(List<Rom> roms)
	{
		for(Rom rom: roms) {
			String filename = rom.getFile(); // for scummvm, this also has a 1 folder path
			String ext = FilenameUtils.getExtension(filename).toLowerCase();
			String path = rom.getPath();
			
			// 1) cue sheets
			if("cue".equals(ext))
			{
				try 
				{
					CueSheet cue = CueParser.parse(new File(path));
					for(FileData fd: cue.getFileData())
					{
						String file = fd.getFile();
						if(file != null) {
							String name = FilenameUtils.getName(file);
							Rom dupe = findRomWithName(roms, name);
							if(dupe != null) {
								dupe.setIsAuxiliary(true); // all files in the cuesheet are duplicates
							}
						}
					}
				} catch (IOException e) {
					Console.printErr("Couldn't parse cuesheet " + path);
					Console.printErr(e);
				}
			}
			
			// 2) clone cd .sub
			else if("sub".equals(ext))
			{
				// is this correct?
				rom.setIsAuxiliary(true);
			}
			
			// 3) clone cd .ccd
			else if("ccd".equals(ext))
			{
				// TODO: here I'm not sure it's ok
				
				// if there is a .cue, the .cue is the main; otherwise it's the .ccd
				String filenameNoExt = filename.substring(0,filename.length() - ext.length() - 1);
				Rom main = findRomWithName(roms, filenameNoExt + ".cue");
				if(main != null) {
					rom.setIsAuxiliary(true);
				}
			}
		}
	}
	
	/**
	 * Finds roms/bioses in a directory, non recursively
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
	             return acceptAsRom(path);
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
        
        // alphabetical order:
        Collections.sort(roms, new Comparator<Rom>(){

			@Override
			public int compare(Rom o1, Rom o2) {
				return FilenameUtils.getName(o1.getFile()).compareTo(FilenameUtils.getName(o2.getFile()));
			}
        	
        });
        
        // cd images consisting of multiple files must have just 1 file not flagged as auxiliary
        flagAuxiliaries(roms);
        
        return roms;
	}
	
	/**
	 * Lists all roms/bioses in a folder, recursively, but only to a certain depth 
	 * @param inFolder starting folder
	 * @param depth the number of depth we can go; 0 => only files, 1 => also visit subfolders, 2 => also visit subfolders of subfolders...
	 * @param args app's args
	 * @return the list of paths found
	 * @throws IOException
	 */
	public static List<Path> listRomsInFolderWithDepth(final String inFolder, final int depth, final Args args)
	throws IOException
	{
		final Path start = Paths.get(inFolder);
		final List<Path> files = new ArrayList<>();
		Files.walkFileTree(start, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) 
			throws IOException 
			{
				Path relative = start.relativize(dir);
				//String[] components = relative.toString().split("" + File.separatorChar);
				if(relative.getNameCount() > depth)
				{
					return FileVisitResult.SKIP_SUBTREE;
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) 
			throws IOException 
			{
				if(acceptAsRom(file))
				{
					files.add(file);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
			throws IOException 
			{
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
			throws IOException 
			{
				return FileVisitResult.CONTINUE;
			}
			
		});
		
		return files;
	}
	
	/**
	 * Lists all scummvm roms. They need each to be in its own subfolder
	 * @param inFolder the folder where to look for the roms
	 * @param args app's args
	 * @return the list of scummvm roms found
	 * @throws IOException
	 * @throws XPathExpressionException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	private static List<Rom> listScummvmRoms(String inFolder, Args args) 
	throws IOException, XPathExpressionException, ParserConfigurationException, SAXException
	{
		List<Path> paths = listRomsInFolderWithDepth(inFolder, 1, args);
		List<Rom> roms = new ArrayList<Rom>(paths.size());
		
		final Path start = Paths.get(inFolder);
		for(Path path: paths)
		{
			String ext = FilenameUtils.getExtension(path.getFileName().toString());
			if("scummvm".equals(ext))
			{
				String relative = start.relativize(path).toString();
				Rom rom = new Rom();
				rom.setFile(relative);
				rom.setIsRealFile(true);
				//rom.setIsTranslated(true);
				rom.setPath(path.toString());
				setTranslatedName(rom, args);
				roms.add(rom);
			}
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
		
		// else
		if("scummvm".equals(args.platform))
		{
			return listScummvmRoms(args.romsDir, args);
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











