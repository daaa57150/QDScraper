package daaa.qdscrapper;

import java.util.Arrays;
import java.util.List;

/**
 * The scrapper's main entry
 * 
 * @author kerndav
 *
 */
public class App
{
//	private static String platform = "ngpc";
//	private static String baseDir = "D:/JavaBundle/workspaces/Developpement/QDScrapper/files/" + platform + "/";
	private static final String[] ROMS = { //TODO: load from filesystem!
//		"Alcahest.smc",
//		"Bahamut Lagoon.smc",
//		"Battle Soccer - Field no Hasha.smc",
//		"Burning Heroes.smc",
//		"Clock Tower.smc",
//		"Do-Re-Mi Fantasy - Milon no Dokidoki Daibouken.smc",
//		"Dossun! Ganseki Battle.smc",
//		"Dual Orb II.smc",
//		"Energy Breaker.smc",
//		"Famicom Detective Club Part II.smc",
//		"Ganpuru - Gunman's Proof.smc",
//		"Go Go Ackman.smc",
//		"Gourmet Sentai - Bara Yarou.smc",
//		"Hiouden Legend of the Scarlet King - The Demonic Oath.smc",
//		"Holy Umbrella - Dondera Wild.smc",
//		"King Of Demons.smc",
//		"Kunio-kun no Dodge Ball - Zenin Shuugou!.smc",
//		"Lodoss Tou Senki - Record of Lodoss War.smc",
//		"Magical Pop'n.smc",
//		"MegaMan & Bass.smc",
//		"Mickey to Donald - Magical Adventure 3.smc",
//		"Mystic Ark.smc",
//		"Nankoku Shounen Papuwa-kun.smc",
//		"Radical Dreamers Le Trésor Interdit.sfc",
//		"Rockman 7 - Duel of Fate.smc",
//		"Shin Nekketsu Kouha - Kunio-tachi no Banka.smc",
//		"Shodai Nekketsu Kouha Kunio-kun.smc",
//		"Super Bomberman 4.smc",
//		"Super Puyo Puyo 2.smc",
//		"Tactics Ogre - Let Us Cling Together.smc",
//		"Tales of Phantasia.smc"
		
//		"Cat Ninden Teyandee.nes",
//		"Nekketsu Kakutou Densetsu.nes",
//		"Nekketsu! Street Basket - Ganbare Dunk Heroes.nes"

//		"Dive Alert - Becky's Version (UE).ngc",
//		"Dive Alert - Matt's Version (UE).ngc",
//		"SNK Vs Capcom - Card Fighters Clash - Capcom Version (UE).ngc",
//		"SNK Vs Capcom - Card Fighters Clash - SNK Version (UE).ngc",
//		"SNK Vs Capcom - Card Fighters Clash 2 - Expand Edition.ngc"
		
//		"Fantastic Night Dreams Cotton (Europe).ngc",
//		"Faselei! (Europe).ngc",
//		"Metal Slug - 1st Mission (World) (En,Ja).ngc",
//		"Metal Slug - 2nd Mission (World) (En,Ja).ngc",
//		"Sonic The Hedgehog - Pocket Adventure (World).ngc"

		"Arkanoid II - Revenge of Doh (1987)(Imagine).st",
		"Hard 'n' Heavy (1989)(ReLINE Software)[mod].st",
		"Metro-Cross (1985)(Probe Software).st",
		"Rick Dangerous (1989)(Core Design - Firebird)[!][a].stx", 
		"Rick Dangerous II (1990)(Micro Style).stx",
		"Time Bandit v2.1 (1985)(MichTron).st",
		"Vroom (1991)(Lankhor).stx",
		"Aventures de Moktar, Les (1991)(Titus)(Fr)[cr Vmax].st",
		"Beyond the Ice Palace (1988)(Elite).st",
		"Xor (1987)(Atari)[cr Bladerunners].st"

	};
	
	/**
	 * Get the list of roms from the filesystem 
	 * @param args the app's arguments
	 * @return the list of roms to process
	 */
	private static List<String> findRoms(Args args) {
		return Arrays.asList(ROMS); // TODO: implement this
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
		
		GamelistXML gameList = new GamelistXML("gamelist.xml", args);
		GamelistXML notFound = new GamelistXML("NOT_FOUND.xml", args);
		
		// TODO: give the option to input a list of file names instead of browsing the filesystem
		List<String> roms = findRoms(args);
		for(String rom: roms)
		{
			System.out.println("# Processing " + rom + " ...");
			List<Game> games = GamesDB.search(rom, args);
			if(games.size() > 0)
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
				GamelistXML gameListDupes = new GamelistXML("DUPE-" + rom + ".xml", args);
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
				empty.setName(GamesDB.getUserDesiredFilename(rom, "", args));
				notFound.addGame(empty);
			}
			
			System.out.println();
		}
		
		
		
		String gamelistXmlFile = gameList.writeFile();
		System.out.println("Processed " + roms.size() +" roms into file " + gamelistXmlFile);

		// write the not found list
		if(notFound.getNbGames() > 0)
		{
			String notFoundXmlFile = notFound.writeFile();
			System.out.println(notFound.getNbGames() + " game(s) were not found, see " + notFoundXmlFile);
		}
	}

}

































