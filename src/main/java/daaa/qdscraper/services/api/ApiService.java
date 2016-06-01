package daaa.qdscraper.services.api;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.icu.text.Normalizer2;

import daaa.qdscraper.Args;
import daaa.qdscraper.model.Game;
import daaa.qdscraper.model.GameCollection;
import daaa.qdscraper.model.Rom;
import daaa.qdscraper.utils.RomCleaner;

public abstract class ApiService 
{
	/**
	 * Searches the api for games
	 * @param rom name of the rom to look for (file name)
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @return the list of games found, first match should be the one
	 */
	public abstract GameCollection search(Rom rom, Args args);
	
	
	
	private static String normalize(String rom)
	{
		rom = RomCleaner.cleanRomName(rom, true);
		rom = StringUtils.stripAccents(rom);
		rom = Normalizer2.getNFKCCasefoldInstance().normalize(rom);//rom.toLowerCase();
		rom = rom.replaceAll("\\bthe\\b", ""); // are there other words sometimes misplaced?
		rom = rom.replaceAll("æ", "ae");
		rom = rom.replaceAll("œ", "oe");
		rom = RomCleaner.removeMultiSpaces(rom);
		rom = rom.trim();
		
		return rom;
	}
	
	/**
	 * Get the name of the game that the user wants:
	 * - the title from the api
	 * - the raw rom name without the extension
	 * - a cleaned rom name
	 * @param rom name of the rom
	 * @param translatedName the name of the rom file, or a converted name for arcade games
	 * @param title title from thegamesdb
	 * @param args app args
	 * @return the desired name
	 */
	protected static String getUserDesiredFilename(String rom, String translatedName, String title, Args args)
	{
		String name = null;
		if(args.useFilename || StringUtils.isEmpty(title))
		{
			if(!StringUtils.isEmpty(args.cleanFilename))
			{
				name = RomCleaner.cleanWithArgs(translatedName, args.cleanFilename);
			}
			else
			{
				name = RomCleaner.removeExtension(translatedName);
			}
		}
		else
		{
			name = title;
		}
		return name;
	}
	
	/**
	 * compares the hard cleaned names of 2 roms 
	 * @param rom1
	 * @param rom2
	 * @return
	 */
	public static boolean isSameRom(String rom1, String rom2) 
	{
		rom1 = normalize(rom1);
		rom2 = normalize(rom2);
		
		return rom1.equals(rom2);
	}
	
	/**
	 * Performs the Jaro Winkler Distance algo on normalized rom names
	 * @param rom1 
	 * @param rom2
	 * @return
	 */
	public static double scoreComparison(String rom1, String rom2)
	{
		rom1 = normalize(rom1);
		rom2 = normalize(rom2);
		
		double jarodWinkler = StringUtils.getJaroWinklerDistance(rom1, rom2);
		return jarodWinkler;
	}
	
	/**
	 * Performs the Levenshtein distance algo on normalized rom names
	 * @param rom1
	 * @param rom2
	 * @return
	 */
	public static int scoreDistance(String rom1, String rom2)
	{
		rom1 = normalize(rom1);
		rom2 = normalize(rom2);
		
		int levenshtein = StringUtils.getLevenshteinDistance(rom1, rom2);
		return levenshtein;
	}
	
	/**
	 * Calculates the jarod winkler score and the Levenshtein distance and sets them
	 * @param game
	 * @param translatedName
	 * @param apiTitle
	 */
	protected void setGameScores(Game game, String translatedName, String apiTitle)
	{
		double score = scoreComparison(translatedName, apiTitle);
		game.setScore(score);
		int distance = scoreDistance(translatedName, apiTitle);
		game.setDistance(distance);
	}
	
	// TODO: remove "Action" if there are other genres + use only 2 max
	protected String cleanGenres(List<String> genres)
	{
		return null;
	}
	
	// progression
	private static int nbResultsProcessed = 0;
	public static void startProgress() {
		nbResultsProcessed = 0;
	}
	public static void doProgress() {
		nbResultsProcessed ++;
		if(nbResultsProcessed == 2) { 
			System.out.print("..");
		} else if(nbResultsProcessed > 2) {
			System.out.print(".");
		}
	}
	public static void stopProgress() {
		if(nbResultsProcessed >= 2) {
			System.out.println();
		}
	}
	
	
}


















