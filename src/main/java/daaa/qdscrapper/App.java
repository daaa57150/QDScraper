package daaa.qdscrapper;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import daaa.qdscrapper.model.Game;
import daaa.qdscrapper.model.GamelistXML;
import daaa.qdscrapper.services.ArcadeRoms;
import daaa.qdscrapper.services.RomBrowser;
import daaa.qdscrapper.services.TheGamesDB;
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
	 * Get the list of roms from the filesystem 
	 * @param args the app's arguments
	 * @return the list of roms to process
	 * @throws IOException 
	 */
	private static List<String> findRoms(Args args) 
	throws IOException {
		
		if(args.romFile != null) {
			System.out.println("Using rom file " + args.romFile + " as the list of roms");
			return RomBrowser.listRomsFromFile(args.romFile);
		}
		//else {
			return RomBrowser.listRomsInFolder(args.romsDir);
		//}
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
		
		List<String> roms = findRoms(args);
		for(String rom: roms)
		{
			if(StringUtils.isEmpty(rom)) continue; // may happen if from a text file 
			
			System.out.println("# Processing " + rom + " ...");
			
			// is it a bios?
			if(RomBrowser.isBios(rom))
			{
				Game game = new Game();
				game.setRom(rom);
				game.setBios(true);
				gameList.addGame(game);
				System.out.println("this is a bios file, added as hidden");
			}
			else
			{
				//if it's an arcade game, look in the arcade roms "DB"
				String name = rom; // for arcade, the name is our match in the DB, for the rest it's the rom
				if(args.arcade)
				{
					name = ArcadeRoms.getRomTitle(rom);
					if(StringUtils.isEmpty(name))
					{
						Game empty = new Game();
						empty.setRom(rom);
						empty.setName(name);
						notFound.addGame(empty);

						System.out.println("Nothing found for " + rom + " in the arcade roms files");
						
						continue;
					}
					//else
					System.out.println(rom + " is the rom name of " + name);
				}
				
				// find matches
				List<Game> games = TheGamesDB.search(rom, name, args);
				if(CollectionUtils.isNotEmpty(games))
				{
					Game first = games.get(0);
					if(games.size() == 1)
					{
						System.out.println("found a match: " + first.getTitle() + " (" + first.getId() + ")");
					}
					else
					{
						System.out.println("found "+ games.size() +" matches:");
						System.out.println("\t- "+ first.getTitle() + " (" + first.getId() + ")");
					}
					
					gameList.addGame(games.get(0));
					
					//dupes
					GamelistXML gameListDupes = new GamelistXML(args.dupesDir + DUPE_PREFIX + QDUtils.sanitizeFilename(rom) + ".xml", args.appendToName);
					for(int i=1; i<games.size(); i++)
					{
						Game dupe = games.get(i);
						System.out.println("\t- "+ dupe.getTitle() + " (" + dupe.getId() + ")");
						gameListDupes.addGame(dupe);
						gameListDupes.writeFile();
					}
				}
				else // not found
				{
					Game empty = new Game();
					empty.setRom(rom);
					empty.setName(name);
					notFound.addGame(empty);
					System.out.println("Nothing found for " + rom + (args.arcade ? (" (" + RomCleaner.cleanRomName(name, false) + ")" ): ""));
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

































