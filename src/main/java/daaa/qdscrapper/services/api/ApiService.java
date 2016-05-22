package daaa.qdscrapper.services.api;

import java.util.List;

import daaa.qdscrapper.Args;
import daaa.qdscrapper.model.Game;
import daaa.qdscrapper.utils.RomCleaner;

public abstract class ApiService 
{
	/**
	 * Searches the api for games
	 * @param rom name of the rom to look for (file name)
	 * @param translatedName the name to use for searches, might be == rom or something else (arcade games)
	 * @return the list of games found, first match should be the one
	 */
	public abstract List<Game> search(String rom, String translatedName, Args args);
	
	
	/**
	 * compares the hard cleaned names of 2 roms 
	 * @param rom1
	 * @param rom2
	 * @return
	 */
	protected boolean isSameRom(String rom1, String rom2)
	{
		rom1 = RomCleaner.cleanRomName(rom1, true);
		rom1 = rom1.toLowerCase();
		rom2 = RomCleaner.cleanRomName(rom2, true);
		rom2 = rom2.toLowerCase();
		
		return rom1.equals(rom2);
	}
	
	// TODO: remove "Action" if there are other genres + use only 2 max
	protected String cleanGenres(List<String> genres)
	{
		return null;
	}
}
