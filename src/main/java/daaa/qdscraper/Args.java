package daaa.qdscraper;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import daaa.qdscraper.services.Console;
import daaa.qdscraper.services.PlatformConverter;

/**
 * Program arguments parsed and usable throughout the app
 * 
 * @author daaa
 *
 */
public class Args
{
	public String platform;				// ES platform name
	public String appendToName = null;	// something to append to each game name
	public boolean useFilename = false;	// use the filename / arcade rom name instead of TheGamesDB game title
	public String cleanFilename;		// clean the name of the rom: if it contains ( remove everything between (), same for [
	public String romsDir = null;		// where do we locate the roms? were do we output our files?
	public String dupesDir = null;		// folder inside romsDir where all duplicated are put, uses the properties for its name
	public String proxyHost;			// proxy
	public int proxyPort = -1;			// proxy port
	public String user;					// proxy user
	public String password;				// proxy password
	public String romFile = null;		// instead of listing the romsDir, use the rom names in this file
	public boolean arcade = false;		// we are processing arcade roms, they don't have pretty names
	public String giantBombApiKey=null; // key for giantBomb's api, used as a last resort if nothing is found on theGamesDB
	
	/**
	 * Parses the arguments of the program
	 * @param commands
	 */
	public Args(String[] commands)
	{
		for(String arg: commands)
		{
			String[] keyval = arg.split("=");
			String key = keyval[0];
			String val = keyval.length == 2 ? keyval[1] : "true"; // some args can be given without a value, wich means "activate this", ie true
			
			switch(key) {
				case "-platform": {
					platform = val;
					break;
				}
				case "-dir": { 
					romsDir = val;
					if(!romsDir.endsWith("" + File.separatorChar)) {
						romsDir += File.separatorChar;
					}
					//dupesDir = romsDir + "DUPES" + File.separatorChar;
					// set at the end because we need to know if the user wants a custom properties file first
					break;
				}
				case "-appendToName": {
					appendToName = val;
					break;
				}
				case "-useFilename": {
					useFilename = Boolean.valueOf(val);
					break;
				}
				case "-cleanFilename": {
					cleanFilename = val;
					break;
				}
				case "-romFile": {
					romFile = val;
					break;
				}
				case "-arcade": {
					arcade=true;
					break;
				}
				case "-giantBombApiKey": {
					giantBombApiKey = val;
					break;
				}
					
				// network
				case "-proxyHost": {
					proxyHost = val;
					break;
				}
				case "-proxyPort": {
					proxyPort = Integer.valueOf(val);
					break;
				}
				case "-user": {
					user = val;
					break;
				}
				case "-password": {
					password = val;
					break;
				}
				
				// properties
				case "-properties": {
					Props.setFile(val);
					break;
				}
				
				
				case "-help": {
					Console.println("TODO: print help");
					System.exit(0);
				}
				default: {
					Console.println("unknown parameter: " + key+", use -help to get help");
					System.exit(1);
				}
			}
		}
		
		if(StringUtils.isEmpty(romsDir)) {
			Console.println("Where are the roms and where do we output everything? use -dir");
			System.exit(15);
		}
		
		// now if the user has a custom properties file, it's set
		dupesDir = romsDir + Props.get("dupes.folder") + File.separatorChar;
		
		if(arcade) //TODO: assume 'arcade' if fba, mame, neogeo, fba_libretro
		{
			platform = PlatformConverter.ARCADE;
		}
		else
		{
			/*if(!Platform.isSupported(platform))
			{
				Console.println("Platform '" + platform + "' not supported, will make queries without specifying it");
				Console.println("Supported platforms: " + StringUtils.join(Platform.getSupportedPlatforms(), ", "));
			}*/
		}
		
		// TODO: list of error numbers?
		if(StringUtils.isNotEmpty(proxyHost) && proxyPort < 0) {
			Console.println("A proxy host needs a proxy port");
			System.exit(3);
		}
		
		if(StringUtils.isNotEmpty(proxyHost) && StringUtils.isEmpty(proxyHost)) {
			Console.println("I only know how to login on a proxy, so give me a proxy with -proxyHost");
			System.exit(4);
		}
		
		if(StringUtils.isEmpty(platform)) {
			Console.println("Set a platform with -platform, it's the folder with the roms in recalbox");
			System.exit(5);
		}
		
		//TODO: validate giantbomb api key ?
		
		
		// messages 
		Console.println("Working in rom directory " + this.romsDir);
		if(romFile != null)
		{
			Console.println("Using rom file " + romFile + " as the list of roms");
		}
		if(StringUtils.isEmpty(platform)) {
			Console.println("No platform specified, will make queries without specifying it");
		}
		Console.println();
	}
}
