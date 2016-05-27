package daaa.qdscrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import daaa.qdscrapper.model.Game;
import daaa.qdscrapper.model.GamelistXML;
import daaa.qdscrapper.services.ArcadeRoms;
import daaa.qdscrapper.services.RomBrowser;
import daaa.qdscrapper.services.ScummVMRoms;
import daaa.qdscrapper.services.api.ApiService;
import daaa.qdscrapper.services.api.impl.GiantBombApiService;
import daaa.qdscrapper.services.api.impl.TheGamesDBApiService;
import daaa.qdscrapper.utils.QDUtils;
import daaa.qdscrapper.utils.RomCleaner;

/**
 * The scrapper's main entry
 * 
 * @author daaa
 *
 */
public class App
{
	/**
	 * Files containing duplicate entries are prefixed with this
	 */
	private static final String DUPE_PREFIX = Props.get("dupes.prefix");
	/**
	 * Dupe images will go into this folder
	 */
	private static final String DUPE_IMAGES_FOLDER = Props.get("dupe.images.folder");
	/**
	 * Images will go inside this folder
	 */
	private static final String IMAGES_FOLDER = Props.get("images.folder");
	/**
	 * Empty and bios games are marked with this api flag
	 */
	private static final String NO_API_ID = "QDScrapper";
	
	/**
	 * Get the list of roms from the filesystem 
	 * @param args the app's arguments
	 * @return the list of roms to process
	 * @throws IOException 
	 */
	private static List<String> findRoms(Args args) 
	throws IOException {
		
//		if(args.romFile != null) {
//			return RomBrowser.listRomsFromFile(args.romFile);
//		}
//		//else
		return RomBrowser.listRoms(args);
	}
	
	/**
	 * Builds a String describing a game meant to output to the user in the console
	 * @param game the game to format
	 * @return the game as a String
	 */
	private static String formatGameForSysout(Game game)
	{
		String str = game.getTitle() + " (" + game.getApi() + " " + game.getId() + " / " + (game.getScore() * 100) + "%)";
		String legal = game.getLegalText();
		if(!StringUtils.isEmpty(legal))
		{
			str += ("  // " + legal);
		}
		return str;
	}
	
	/**
	 * Moves a file, ensures directory structure exists
	 * @param from the image to move
	 * @param to the path to move to
	 * @throws IOException 
	 */
	private static void moveFile(String from, String to) 
	throws IOException
	{
		File ff = new File(from);
		File ft = new File(to);
		
		if(ff.exists())
		{
			ft.mkdirs();
			Files.move(Paths.get(from), Paths.get(to), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	/**
	 * Adds an empty game to the "notfound" xml file
	 * @param notFound
	 * @param rom
	 * @param name
	 */
	private static void addEmptyGame(GamelistXML notFound, String rom, String name)
	{
		Game empty = new Game(NO_API_ID);
		empty.setRom(rom);
		empty.setName(name);
		notFound.addGame(empty);
	}
	
	// TODO: List<Game> should be a class by itself
	/**
	 * Sorts the game by best match
	 * @param games
	 * @return
	 */
	private static List<Game>sortByBestMatch(List<Game> games)
	{
		Collections.sort(games);
		return games;
	}
	
	private static String getRealNameOfRom(String rom, Args args) 
	throws IOException, XPathExpressionException, ParserConfigurationException, SAXException
	{
		String name = rom; // for arcade/scumm, the name is our match in the DB, for the rest it's the rom
		
		//if it's an arcade game, look in the arcade roms "DB"
		if(args.arcade)
		{
			name = ArcadeRoms.getRomTitle(rom);
			if(StringUtils.isEmpty(name))
			{
				//addEmptyGame(notFound, rom, name);
				System.out.println("  => Nothing found for " + rom + " in the arcade rom files");
				return null;
			}
			//else
			System.out.println(rom + " is the arcade rom name of " + name);
		}
		
		// if it's a scummvm game, look in the scummvm roms "DB"
		if("scummvm".equals(args.platform))
		{
			name = ScummVMRoms.getRomTitle(rom);
			if(StringUtils.isEmpty(name))
			{
				//addEmptyGame(notFound, rom, name);
				System.out.println("  => Nothing found for " + rom + " in the scummvm rom files");
				return null;
			}
			//else
			System.out.println(rom + " is the scummvm rom name of " + name);
		}
		
		return name;
	}
	
	/**
	 * Main!
	 * 
	 * @param commands the command line arguments
	 * @throws Exception
	 */
	public static void main(String[] commands) 
	throws Exception
	{
		Args args = new Args(commands);
		
		GamelistXML gameList = new GamelistXML(args.romsDir + "gamelist.xml", args.appendToName);
		GamelistXML notFound = new GamelistXML(args.romsDir + "NOT_FOUND.xml", args.appendToName);
		
		// services to use, in that order, to look for a perfect match
		List<ApiService> apiServices = new ArrayList<>();
		apiServices.add(new TheGamesDBApiService());
		
		if(args.giantBombApiKey != null)
		{
			//System.out.println("GiantBomb api key is present, we'll ask GiantBomb");
			apiServices.add(new GiantBombApiService());
		}
		
		
		// process roms
		List<String> roms = findRoms(args);
		for(String rom: roms)
		{
			if(StringUtils.isEmpty(rom)) continue; // may happen if from a text file 
			
			System.out.println("# Processing " + rom + " ...");
			
			// is it a bios?
			if(RomBrowser.isBios(rom))
			{
				Game game = new Game(NO_API_ID);
				game.setRom(rom);
				game.setBios(true);
				gameList.addGame(game);
				System.out.println("  => This is a bios file, added as hidden");
			}
			else
			{
				String name = getRealNameOfRom(rom, args); // for arcade/scumm, the name is our match in the DB, for the rest it's the rom
				if(name == null)
				{
					addEmptyGame(notFound, rom, "");
					continue;
				}
				
				// ask the services
				List<Game> games = new ArrayList<>();
				for(ApiService service: apiServices)
				{
					List<Game> apiGames = service.search(rom, name, args);
					if(!CollectionUtils.isEmpty(apiGames))
					{
						games.addAll(apiGames);
						Game perfectMatch = QDUtils.findBestPerfectMatch(apiGames);
						if(perfectMatch != null)
						{
							//break; // yes there is a perfect match, we stop here => TODO: should we still ask other apis to perhaps find a best perfect match ?
						}
					}
				}
				
				
				// now we have found games
				if(CollectionUtils.isNotEmpty(games))
				{
					Game topResult = QDUtils.findBestPerfectMatch(games);
					if(topResult != null) // perfect match found
					{
						System.out.println("  => Found a perfect match: " + formatGameForSysout(topResult));
						gameList.addGame(topResult);
					}
					else
					{
						// sort by best match
						sortByBestMatch(games);
						topResult = games.get(0);
						gameList.addGame(topResult);
						if(games.size() == 1) // only one match
						{
							System.out.println("  => Found a match: " + formatGameForSysout(topResult));
						}
						else // many results found
						{
							System.out.println("  => Found "+ games.size() +" matches:");
							System.out.println("\t- "+ formatGameForSysout(topResult));
						}
					}

						
					//dupes
					if(games.size() > 1)
					{
						GamelistXML gameListDupes = new GamelistXML(args.dupesDir + DUPE_PREFIX + QDUtils.sanitizeFilename(rom) + ".xml", args.appendToName);
						for(Game dupe: games)
						{
							if(dupe != topResult) // it's really a dupe
							{
								// if perfect match, the user is not interested in dupes
								if(!topResult.isPerfectMatch())
								{
									System.out.println("\t- "+ formatGameForSysout(dupe));
								}
								
								gameListDupes.addGame(dupe);
								if(!StringUtils.isEmpty(dupe.getImage()))
								{
									// move the image to the dupe images directory
									String from = args.romsDir + File.separatorChar + IMAGES_FOLDER + File.separatorChar + dupe.getImage();
									String to = args.dupesDir + File.separatorChar + DUPE_IMAGES_FOLDER + File.separatorChar + dupe.getImage();
									moveFile(from, to);
								}
							}
						}
						gameListDupes.writeFile();
					}
				}
				else // not found
				{
					addEmptyGame(notFound, rom, name);
					String precision = null;
					if(args.arcade || "scummvm".equals(args.platform))
					{
						precision = "(" + RomCleaner.cleanRomName(name, false) + ")";
					}
					System.out.println("  => Nothing found for " + rom + (precision == null ? "" : precision));
				}
			}
			
			System.out.println();
		}
		
		
		
		String gamelistXmlFile = gameList.writeFile();
		System.out.println("Processed " + (roms.size() - notFound.getNbGames()) +" roms into file " + gamelistXmlFile);

		// write the not found list
		if(notFound.getNbGames() > 0)
		{
			String notFoundXmlFile = notFound.writeFile();
			System.out.println(notFound.getNbGames() + " game(s) were not found, see " + notFoundXmlFile);
		}
	}

}

































