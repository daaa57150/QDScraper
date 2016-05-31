package daaa.qdscraper.model;

/**
 * Type of matching used to find a game
 * 
 * @author daaa
 */
public enum MatchingType {
	/** The name of the file was cleaned, and a search performed on the API */
	SEARCH, 
	/** The md5 hash was sent to the API */
	MD5, 
	/** The complete raw file name of the rom was sent to the API */
	FILENAME
}
