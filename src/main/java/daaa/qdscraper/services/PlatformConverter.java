package daaa.qdscrapper.services;

import java.util.Properties;

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
public class PlatformConverter
{
	// TODO: each api has different supported platforms
	private static final Properties PLATFORMS = QDUtils.loadClasspathProperties("platform.properties");
	/*private static Set<String> SUPPORTED_PLATFORMS;
	static {
		SUPPORTED_PLATFORMS = new HashSet<>();
		for(Object key: Collections.list(PLATFORMS.keys()))
		{
			SUPPORTED_PLATFORMS.add(key.toString().split("[.]")[0]);
		}
	}*/
	
	/**
	 * Special platform to process arcade games (key in platform.properties).
	 */
	public static final String ARCADE = "arcade";
	
	
	/**
	 * Input the ES name, get the GamesDB name
	 * @param esName name of the ES platform (snes, nes, ngpc...)
	 * @return
	 */ // TODO: return List<Platform>
	public static String[] asTheGamesDB(String esName)
	{
		if(esName == null) return null;
		String property = PLATFORMS.getProperty(esName + ".thegamesdb");
		if(property != null) {
			return property.split(",");
		}
		return null;
	}
	
	/**
	 * Input the ES name, get the GamesDB name
	 * @param esName name of the ES platform (snes, nes, ngpc...)
	 * @return
	 */ // TODO: return List<Platform>
	public static String[] asGiantBomb(String esName)
	{
		if(esName == null) return null;
		String property = PLATFORMS.getProperty(esName + ".giantbomb");
		if(property != null) {
			return property.split(",");
		}
		return null;
	}
	
	/* *
	 * Does this app know about the platform?
	 * 
	 * @param platform the platform
	 * @return
	 * /
	public static boolean isSupported(String platform) {
		return SUPPORTED_PLATFORMS.contains(platform + ".thegamesdb");
	}
	
	/* *
	 * Get the list of all platforms this app knows about, in ES format
	 * @return the list of all platforms this app knows about, in ES format
	 * /
	public static Set<String> getSupportedPlatforms() {
		return SUPPORTED_PLATFORMS;
	}*/
}




