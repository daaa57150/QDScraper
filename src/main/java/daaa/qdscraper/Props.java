package daaa.qdscraper;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import daaa.qdscraper.utils.QDUtils;

/**
 * Contains all the properties for the app; ie values that should not
 * change but advanced users can modify them. 
 * 
 * @author daaa
 */
public class Props {
	
	private Props(){}; // do not instanciate
	
	/**
	 * The file to read the properties from, if null use the default
	 */
	private static String FILE = null;
	
	/**
	 * Contains the properties
	 */
	private static Properties PROPS;
	
	/**
	 * Set the file to use; if null the default is used
	 * @param file
	 */
	public static void setFile(String file)
	{
		FILE = file;
	}
	
	/**
	 * Get the property for the given key. If it doesn't exist, the program stops with an error.
	 * @param key the wanted property 
	 * @return the property
	 */
	public static String get(String key)
	{
		if(PROPS == null)
		{
			if(FILE == null)
			{
				PROPS = QDUtils.loadClasspathProperties("scraper.properties");
			}
			else
			{
				PROPS = new Properties();
				StringReader reader;
				try {
					reader = new StringReader(QDUtils.readFile(FILE));
					PROPS.load(reader);
				} catch (IOException e) {
					System.err.println("Error when reading properties file " + FILE);
					e.printStackTrace();
					System.exit(14);
				}
				
			}
		}
		String prop = PROPS.getProperty(key);
		if(prop == null)
		{
			System.err.println("Property " + key + " is mandatory");
			System.exit(13);
		}
		return prop;
	}
	
	
}
