package daaa.qdscraper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import daaa.qdscraper.model.Game;
import daaa.qdscraper.model.GameCollection;
import daaa.qdscraper.model.GamelistXML;
import daaa.qdscraper.model.Rom;
import daaa.qdscraper.services.Console;
import daaa.qdscraper.services.RomBrowser;
import daaa.qdscraper.services.api.ApiService;
import daaa.qdscraper.services.api.impl.GiantBombApiService;
import daaa.qdscraper.services.api.impl.ScreenScraperApiService;
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
		String str = game.getTitle() + " (" + game.getApi() + " " + game.getId() + " / " + game.getScoreInPercent() + (game.getDistance() == 0 ? "" : (" ["+game.getDistance()+"]")) +")";
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
	 * @throws IOException 
	 */
	private static void addEmptyGame(GamelistXML notFound, String rom, String name) 
	throws IOException
	{
		Game empty = new Game(QDConst.NO_API_ID);
		empty.setFile(rom);
		empty.setName(name);
		notFound.addGame(empty);
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
		
		String pathGamelistXML = args.romsDir + "gamelist.xml";
		if(args.overwrite)
		{
			Console.println("Overwriting file " + pathGamelistXML);
		}
		GamelistXML gameList = new GamelistXML(pathGamelistXML, args.appendToName, args.overwrite);
		GamelistXML notFound = new GamelistXML(args.romsDir + "NOT_FOUND.xml", args.appendToName, true);
		GamelistXML gameListDupes = null;
		
		// services to use, in that order, to look for a perfect match
		List<ApiService> apiServices = new ArrayList<>();
		apiServices.add(new TheGamesDBApiService());
		apiServices.add(new ScreenScraperApiService());
		
		if(args.giantBombApiKey != null)
		{
			//Console.println("GiantBomb api key is present, we'll ask GiantBomb");
			apiServices.add(new GiantBombApiService());
		}
		
		Console.println();
		List<Rom> roms = findRoms(args);
		try
		{
			// process roms
			for(Rom rom: roms)
			{
				Console.println("# Processing " + rom.getFile() + " ...");
				
				// is it already in the gamelist.xml (in the case we don't overwrite it) ?
				if(gameList.contains(rom))
				{
					Console.println("The file " + rom.getFile() + " is already in gamelist.xml");
				}
				// is it a bios?
				else if(rom.isBios())
				{
					Game game = new Game(QDConst.NO_API_ID);
					game.setFile(rom.getFile());
					game.setBios(true);
					gameList.addGame(game);
					Console.println("  => This is a bios file, added as hidden");
				}
				// is it an auxiliary file (.bin, .img, .sub...) ?
				else if(rom.isAuxiliary())
				{
					Game game = new Game(QDConst.NO_API_ID);
					game.setFile(rom.getFile());
					game.setAuxiliary(true);
					gameList.addGame(game);
					Console.println("  => This is an auxiliary file, added as hidden");
				}
				// we have to process it
				else
				{
					String name = rom.getTranslatedName(); // for arcade/scumm, the name is our match in the DB, for the rest it's the rom
					if(name == null) // needed a translation but wasn't found in our DB, highly unlikely
					{
						addEmptyGame(notFound, rom.getFile(), "");
						Console.println("  => Nothing found for " + rom.getFile() + " in our data files");
						Console.println();
						continue;
					}
					if(rom.isTranslated())
					{
						Console.println(rom.getFile() + " is the rom file name of " + name);
					}
					
					// ask the services
					GameCollection games = new GameCollection();
					try
					{
						for(ApiService service: apiServices)
						{
							GameCollection apiGames = service.search(rom, args);
							/*for(Game apiGame: apiGames)
							{
								ApiService.setGameScores(apiGame, name, apiGame.getTitle());
							}*/
							if(!CollectionUtils.isEmpty(apiGames))
							{
								games.addAll(apiGames);
								if(games.countPerfectMatches() >= 2)
								{
									//we already have 2 great matches, so we can stop 
									//ask giantbomb and igdb only if really needed, as their keys will expire very fast
									break; 
								}
							}
						}
					}
					finally
					{
						//ApiService.stopProgress();
					}
					
					
					// now we have found games TODO: option to merge games with score = 1
					if(CollectionUtils.isNotEmpty(games))
					{
						games.sortByBestMatch();
						Game topResult = games.getBestPerfectMatch();
						if(topResult != null) // perfect match found
						{
							Console.println("  => Found a perfect match: " + formatGameForSysout(topResult));
							gameList.addGame(topResult);
							
							if(games.size() > 1)
							{
								Console.println("  => Also found dupes:");
							}
						}
						else
						{
							// sort by best match
							topResult = games.get(0);
							gameList.addGame(topResult);
							if(games.size() == 1) // only one match
							{
								Console.println("  => Found a match: " + formatGameForSysout(topResult));
							}
							else // many results found
							{
								Console.println("  => Found "+ games.size() +" matches:");
								Console.println("\t- "+ formatGameForSysout(topResult));
							}
						}
	
							
						//dupes
						if(games.size() > 1)
						{
							String file = Paths.get(rom.getFile()).getFileName().toString();
							gameListDupes = new GamelistXML(args.dupesDir + DUPE_PREFIX + QDUtils.sanitizeFilename(file) + ".xml", args.appendToName, true);
							for(Game dupe: games)
							{
								if(dupe != topResult) // it's really a dupe
								{
									// if perfect match, the user is not interested in dupes?
									//if(!topResult.isPerfectMatch())
									//{
									Console.println("\t- "+ formatGameForSysout(dupe));
									//}
									
									gameListDupes.addGame(dupe);
									if(!StringUtils.isEmpty(dupe.getImage()))
									{
										// move the image to the dupe images directory
										String from = args.romsDir + IMAGES_FOLDER + File.separatorChar + dupe.getImage();
										String to = args.dupesDir + DUPE_IMAGES_FOLDER + File.separatorChar + dupe.getImage();
										moveFile(from, to);
									}
								}
							}
							gameListDupes.close();
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
						Console.println("  => Nothing found for " + rom.getFile() + (precision == null ? "" : precision));
					}
				}
				
				Console.println();
			}
		}
		finally
		{	
			// close the main gamelist
			gameList.close();
			Console.println("Processed " + (roms.size() - notFound.getNbGames()) +" roms into file " + gameList.getPath());
			
			// close the not found list
			if(notFound.getNbGames() > 0)
			{
				notFound.close();
				Console.println(notFound.getNbGames() + " game(s) were not found, see " + notFound.getPath());
			}
			
			// if in error, maybe a dupe is still open
			if(gameListDupes != null && !gameListDupes.isClosed() && gameListDupes.isOpen()) {
				gameListDupes.close();
			}
		}
	}
}

































