package daaa.qdscrapper;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

/**
 * Program arguments parsed and usable throughout the app
 * 
 * @author daaa
 *
 */
public class Args
{
	public String platform;
	//public String baseDir;
	public String appendToName = null;
	public boolean useFilename = false;
	public String cleanFilename;
	public String romsDir = null;
	public String dupesDir = null;
	public String proxyHost;
	public int proxyPort = -1;
	public String user;
	public String password;
	
	
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
					if(!Platform.isSupported(platform))
					{
						System.out.println("Platform '" + platform + "' not supported, will make queries without specifying it");
						System.out.println("Supported platforms: " + StringUtils.join(Platform.getSupportedPlatforms(), ", "));
					}
					break;
				}
				case "-dir": { 
					romsDir = val;
					if(!romsDir.endsWith("" + File.separatorChar)) {
						romsDir += File.separatorChar;
					}
					dupesDir = romsDir + "DUPES" + File.separatorChar;
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
				
				
				case "-help": {
					System.out.println("TODO: print help");
					System.exit(0);
				}
				default: {
					System.out.println("unknown parameter: " + key+", use -help to get help");
					System.exit(1);
				}
			}
		}
		
		// TODO: list of error numbers?
		if(StringUtils.isNotEmpty(proxyHost) && proxyPort < 0) {
			System.out.println("A proxy host needs a proxy port");
			System.exit(3);
		}
		
		if(StringUtils.isNotEmpty(proxyHost) && StringUtils.isEmpty(proxyHost)) {
			System.out.println("I only know how to login on a proxy, so give me a proxy with -proxyHost");
			System.exit(4);
		}
		
		if(StringUtils.isEmpty(platform)) {
			System.out.println("Set a platform with -platform, it's the folder with the roms");
			System.exit(5);
		}
		
		
		// messages 
		System.out.println("Working in rom directory " + this.romsDir);
		if(StringUtils.isEmpty(platform)) {
			System.out.println("No platform specified, will make queries without specifying it");
		}
		System.out.println("");
	}
}
