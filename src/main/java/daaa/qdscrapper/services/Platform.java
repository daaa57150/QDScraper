package daaa.qdscrapper.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import daaa.qdscrapper.utils.QDUtils;

/**
 * Converts ES platform names to api platform names
 * Loads its content from the platform.properties file
 * 
 * Complete lists are available in docs/platforms
 * 
 * @author daaa
 *
 */
public class Platform
{
	// TODO: each api has different supported platforms
	private static final Properties PLATFORMS = QDUtils.loadClasspathProperties("platform.properties");
	private static Set<String> SUPPORTED_PLATFORMS;
	static {
		SUPPORTED_PLATFORMS = new HashSet<>();
		for(Object key: Collections.list(PLATFORMS.keys()))
		{
			SUPPORTED_PLATFORMS.add(key.toString().split("[.]")[0]);
		}
	}
	
	// TODO: one ES platform = many API platforms
	/**
	 * Special platform to process arcade games (key in platform.properties).
	 */
	public static final String ARCADE = "arcade";
	/**
	 * Special platform to process neogeo games (key in platform.properties).
	 */
	public static final String NEOGEO = "neogeo"; //remove, it belongs to the arcade list
	
	/**
	 * Input the ES name, get the GamesDB name
	 * @param esName name of the ES platform (snes, nes, ngpc...)
	 * @return
	 */
	public static String asTheGamesDB(String esName)
	{
		if(esName == null) return null;
		return PLATFORMS.getProperty(esName + ".thegamesdb");
	}
	
	/**
	 * Input the ES name, get the GamesDB name
	 * @param esName name of the ES platform (snes, nes, ngpc...)
	 * @return
	 */
	public static String asGiantBomb(String esName)
	{
		if(esName == null) return null;
		return PLATFORMS.getProperty(esName + ".giantbomb");
	}
	
	/**
	 * Does this app know about the platform?
	 * 
	 * @param platform the platform
	 * @return
	 */
	public static boolean isSupported(String platform) {
		return SUPPORTED_PLATFORMS.contains(platform + ".thegamesdb");
	}
	
	/**
	 * Get the list of all platforms this app knows about, in ES format
	 * @return the list of all platforms this app knows about, in ES format
	 */
	public static Set<String> getSupportedPlatforms() {
		return SUPPORTED_PLATFORMS;
	}
}




