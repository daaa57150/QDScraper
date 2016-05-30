package daaa.qdscraper.services.api;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.ibm.icu.text.Normalizer2;

import daaa.qdscraper.Args;
import daaa.qdscraper.model.Game;
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
	public abstract List<Game> search(Rom rom, Args args);
	
	
	
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
	
	public static double scoreComparison(String rom1, String rom2)
	{
		rom1 = normalize(rom1);
		rom2 = normalize(rom2);
		
		return StringUtils.getJaroWinklerDistance(rom1, rom2);
	}
	
	protected void setGameScore(Game game, String translatedName, String apiTitle)
	{
		double score = scoreComparison(translatedName, apiTitle);
		game.setScore(score);
	}
	
	// TODO: remove "Action" if there are other genres + use only 2 max
	protected String cleanGenres(List<String> genres)
	{
		return null;
	}
	
	// TODO: add some progress info (... from TGDB)
}


















