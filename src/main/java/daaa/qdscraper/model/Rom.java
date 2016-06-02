package daaa.qdscraper.model;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import daaa.qdscraper.Props;
import daaa.qdscraper.services.Console;
import daaa.qdscraper.utils.QDUtils;

/**
 * Represents a rom, ie a file on the filesystem with its meta
 * 
 * @author daaa
 */
public class Rom {
	// name of the file
	private String file;
	// path on the filesystem
	private String path;
	// md5 hash if possible
	private String md5;
	// file size in bytes
	private long size = -1L;
	// match against our bios list
	private boolean isBios;
	// if arcade or scummvm we get the match from our DB files
	private String translatedName; 
	// true if it's from the FS, false if it's from a textual list  
	private boolean isRealFile;
	// true if arcade or scummvm and not a bios
	private boolean isTranslated = false;
	// true for .bin files if they have a .cue, or .ccd and .sub
	private boolean isAuxiliary = false;
	
	private static final long MAX_SIZE_FOR_MD5 = Long.valueOf(Props.get("md5.size.max")); //45Mo
	
	/**
	 * @return the file
	 */
	public String getFile() {
		return file;
	}
	/**
	 * @param rom the rom to set (name of the file relative to the romsDir) 
	 */
	public void setFile(String file) {
		this.file = file;
	}
	/**
	 * @return the isAuxiliary
	 */
	public boolean isAuxiliary() {
		return isAuxiliary;
	}
	/**
	 * @param isAuxiliary the isAuxiliary to set
	 */
	public void setIsAuxiliary(boolean isAuxiliary) {
		this.isAuxiliary = isAuxiliary;
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
			if(getSize() > MAX_SIZE_FOR_MD5)
			{
				Console.println("Rom " + file + " is too big to process md5 (" +getSize()+ " bytes)");
			}
			else
			{
				md5 = QDUtils.getMD5(path);
			}
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
	 * @return the size
	 */
	public long getSize() {
		if(isRealFile && size == -1L)
		{
			size = FileUtils.sizeOf(new File(path));
		}
		return size;
	}
	/**
	 * @param size the size to set
	 */
	public void setSize(long size) {
		this.size = size;
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
	
	
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
