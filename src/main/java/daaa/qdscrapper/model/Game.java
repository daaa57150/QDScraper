package daaa.qdscrapper.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a game
 * @author daaa
 */
public class Game
{
	private String rom; //name of the rom (file)
	private String name; //name we want in the gamelist.xml
	private String title; //name from theGamesDB
	private String id; //id from theGamesDB or other api
	private String desc;
	private String image;
	private float rating;
	private Date releasedate;
	private String developer;
	private String publisher;
	private List<String> genres = new ArrayList<>();
	private String players;
	private boolean bios = false;
	private boolean perfectMatch = false;

	// the api that retrieved this result
	private String api;
	private String legalText; //some apis request showing a legal text to the user
	
	/* *
	 * Default constructor
	 * / 
	public Game(){}
	*/
	
	/**
	 * Constructor with api, use this!
	 * @param api
	 */
	public Game(String api){
		this.api = api;
	}
	
	/**
	 * is bios?
	 * @return true if it is a bios
	 */
	public boolean isBios() 
	{
		return bios;
	}
	/**
	 * set is bios
	 * @param bios the bios to set
	 */
	public void setBios(boolean bios) 
	{
		this.bios = bios;
	}
	/**
	 * @return the api
	 */
	public String getApi() {
		return api;
	}
	/**
	 * @param api the api to set
	 */
	public void setApi(String api) {
		this.api = api;
	}
	/**
	 * @return the rom
	 */
	public String getRom()
	{
		return rom;
	}
	/**
	 * @param rom the rom to set
	 */
	public void setRom(String rom)
	{
		this.rom = rom;
	}
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	/**
	 * @return the desc
	 */
	public String getDesc()
	{
		return desc;
	}
	/**
	 * @param desc the desc to set
	 */
	public void setDesc(String desc)
	{
		this.desc = desc;
	}
	/**
	 * @return the image
	 */
	public String getImage()
	{
		return image;
	}
	/**
	 * @param image the image to set
	 */
	public void setImage(String image)
	{
		this.image = image;
	}
	/**
	 * @return the rating
	 */
	public float getRating()
	{
		return rating;
	}
	/**
	 * @param rating the rating to set
	 */
	public void setRating(float rating)
	{
		this.rating = rating;
	}
	/**
	 * @return the releasedate
	 */
	public Date getReleasedate()
	{
		return releasedate;
	}
	/**
	 * @param releasedate the releasedate to set
	 */
	public void setReleasedate(Date releasedate)
	{
		this.releasedate = releasedate;
	}
	/**
	 * @return the developer
	 */
	public String getDeveloper()
	{
		return developer;
	}
	/**
	 * @param developer the developer to set
	 */
	public void setDeveloper(String developer)
	{
		this.developer = developer;
	}
	/**
	 * @return the publisher
	 */
	public String getPublisher()
	{
		return publisher;
	}
	/**
	 * @param publisher the publisher to set
	 */
	public void setPublisher(String publisher)
	{
		this.publisher = publisher;
	}
	/**
	 * @return the genres
	 */
	public List<String> getGenres()
	{
		return genres;
	}
	/**
	 * @param genres the genres to set
	 */
	public void setGenres(List<String> genres)
	{
		this.genres = genres;
	}
	public void addGenre(String genre)
	{
		this.genres.add(genre);
	}
	/**
	 * @return the players
	 */
	public String getPlayers()
	{
		return players;
	}
	/**
	 * @param players the players to set
	 */
	public void setPlayers(String players)
	{
		this.players = players;
	}
	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}
	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	/**
	 * @return the perfectMatch
	 */
	public boolean isPerfectMatch() {
		return perfectMatch;
	}
	/**
	 * @param perfectMatch the perfectMatch to set
	 */
	public void setPerfectMatch(boolean perfectMatch) {
		this.perfectMatch = perfectMatch;
	}
	/**
	 * @return the legalText
	 */
	public String getLegalText() {
		return legalText;
	}

	/**
	 * @param legalText the legalText to set
	 */
	public void setLegalText(String legalText) {
		this.legalText = legalText;
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
