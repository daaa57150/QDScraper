package daaa.qdscraper.model;

import daaa.qdscraper.utils.QDUtils;

/**
 * Represents a rom, ie a file on the filesystem with its meta
 * 
 * @author daaa
 */
public class Rom {
	// name of the file
	private String rom;
	// path on the filesystem
	private String path;
	// md5 hash if possible
	private String md5;
	// match against our bios list
	private boolean isBios;
	// if arcade or scummvm we get the match from our DB files
	private String translatedName; 
	// true if it's from the FS, false if it's from a textual list  
	private boolean isRealFile;
	// true if arcade or scummvm and not a bios
	private boolean isTranslated = false;
	
	
	/**
	 * @return the rom
	 */
	public String getRom() {
		return rom;
	}
	/**
	 * @param rom the rom to set (name of the file relative to the romsDir) 
	 */
	public void setRom(String rom) {
		this.rom = rom;
	}
	/**
	 * @return the isTranslated
	 */
	public boolean isTranslated() {
		return isTranslated;
	}
	/**
	 * @param isTranslated the isTranslated to set
	 */
	public void setIsTranslated(boolean isTranslated) {
		this.isTranslated = isTranslated;
	}
	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	/**
	 * @param path the complete path to the file
	 */
	public void setPath(String path) {
		this.path = path;
	}
	/**
	 * @return the md5, lazy calculated
	 */
	public String getMd5() {
		if(isRealFile && md5 == null)
		{
			md5 = QDUtils.getMD5(path);
		}
		return md5;
	}
	/**
	 * @param md5 the md5 to set
	 */
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	/**
	 * @return the isBios
	 */
	public boolean isBios() {
		return isBios;
	}
	/**
	 * @param isBios the isBios to set
	 */
	public void setIsBios(boolean isBios) {
		this.isBios = isBios;
	}
	/**
	 * @return the translatedName
	 */
	public String getTranslatedName() {
		return translatedName;
	}
	/**
	 * @param translatedName the translatedName to set
	 */
	public void setTranslatedName(String translatedName) {
		this.translatedName = translatedName;
	}
	/**
	 * @return the isRealFile
	 */
	public boolean isRealFile() {
		return isRealFile;
	}
	/**
	 * @param isRealFile the isRealFile to set
	 */
	public void setIsRealFile(boolean isRealFile) {
		this.isRealFile = isRealFile;
	}
	
	
	
	
	
	
}
