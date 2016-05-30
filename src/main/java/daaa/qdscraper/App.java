package daaa.qdscraper;

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

import daaa.qdscraper.model.Game;
import daaa.qdscraper.model.GamelistXML;
import daaa.qdscraper.model.Rom;
import daaa.qdscraper.services.ArcadeRoms;
import daaa.qdscraper.services.RomBrowser;
import daaa.qdscraper.services.ScummVMRoms;
import daaa.qdscraper.services.api.ApiService;
import daaa.qdscraper.services.api.impl.GiantBombApiService;
import daaa.qdscraper.services.api.impl.TheGamesDBApiService;
import daaa.qdscraper.utils.QDUtils;
import daaa.qdscraper.utils.RomCleaner;

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
	 * Get the list of roms from the filesystem or from the provided text file 
	 * @param args the app's arguments
	 * @return the list of roms to process
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws XPathExpressionException 
	 */
	private static List<Rom> findRoms(Args args) 
	throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
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
		empty.setFile(rom);
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
		List<Rom> roms = findRoms(args);
		for(Rom rom: roms)
		{
			System.out.println("# Processing " + rom.getFile() + " ...");
			
			// is it a bios?
			if(rom.isBios())
			{
				Game game = new Game(NO_API_ID);
				game.setFile(rom.getFile());
				game.setBios(true);
				gameList.addGame(game);
				System.out.println("  => This is a bios file, added as hidden");
			}
			else
			{
				String name = rom.getTranslatedName(); // for arcade/scumm, the name is our match in the DB, for the rest it's the rom
				if(name == null) // needed translation but wasn't found in our DB, highly unlikely
				{
					addEmptyGame(notFound, rom.getFile(), "");
					System.out.println("  => Nothing found for " + rom.getFile() + " in our data files");
					continue;
				}
				if(rom.isTranslated())
				{
					System.out.println(rom.getFile() + " is the rom file name of " + name);
				}
				
				// ask the services
				List<Game> games = new ArrayList<>();
				for(ApiService service: apiServices)
				{
					List<Game> apiGames = service.search(rom, args);
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
						GamelistXML gameListDupes = new GamelistXML(args.dupesDir + DUPE_PREFIX + QDUtils.sanitizeFilename(rom.getFile()) + ".xml", args.appendToName);
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
					addEmptyGame(notFound, rom.getFile(), name);
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

































