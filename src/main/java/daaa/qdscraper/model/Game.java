package daaa.qdscraper.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Represents a game
 * @author daaa
 */
public class Game implements Comparable<Game>
{
	private String file; //name of the rom (file, as a relative path to romsDir)
	private String name; //name we want in the gamelist.xml
	private String title; //name from apî
	private String id; //id from theGamesDB or other api
	private String desc;
	private String image;
	private float rating = 0;
	private Date releasedate;
	private String developer;
	private String publisher;
	private List<String> genres = new ArrayList<>();
	private String players;
	private boolean bios = false;
	//private boolean perfectMatch = false;
	
	// 1 = perfect match, 0 = totally different
	private double score = 0; // jarod-winkler
	// levenstein
	private int distance = Integer.MAX_VALUE; // levenshtein
	// the type of matching that was used to retrieve this game
	private MatchingType matchingType;
	// set to true if the game was matched using a hash, ie 100% sure it's the same rom
	//private boolean md5Match = false;
	
	// the api that retrieved this result
	private String api;
	//some apis request showing a legal text to the user
	private String legalText; 
	
	private static DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	static {
		//df.setMaximumFractionDigits(340); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS
		df.setMaximumFractionDigits(2);
	}
	
	
	/* *
	 * Default constructor
	 * / 
	public Game(){}
	*/
	

	/**
	 * Compares the quality of this match with another game.
	 * Most important thing is the score, then the description and the image, then the other metas that were filled.
	 */
	@Override
	public int compareTo(Game o2) {
		
		// 1) score is important
		if(getScore() != o2.getScore()) {
			return ((Double)getScore()).compareTo(o2.getScore()) *-1; // *-1 because high scores are better
		}
		
		if(getDistance() != o2.getDistance()) {  
			return ((Integer)getDistance()).compareTo(o2.getDistance()); // small distance is best here
		}
		
		// 2) number of criterions (desc is super important, image is super important, the rest not really)
		return getCriterionsScore().compareTo(o2.getCriterionsScore()) *-1; // *-1 because high scores are first
		
		// we could check the quality of the image
	}
	
	/**
	 * Calculates a score based on the criterions that are filled
	 * @return
	 */
	private Integer getCriterionsScore()
	{
		int cs = 0;
		if(!StringUtils.isEmpty(desc)) {
			if(desc.length() >= 200) cs += 5;
			else cs += 3;
		}
		if(!StringUtils.isEmpty(image)) {
			cs += 6;
		}
		
		if(rating > 0) cs ++;
		if(releasedate != null) cs += 2;
		if(!StringUtils.isEmpty(developer)) cs += 2;
		if(!StringUtils.isEmpty(publisher)) cs ++;
		if(genres.size() > 0) cs ++;
		if(!StringUtils.isEmpty(players)) cs += 2;
		
		return cs;
	}
	
	
	/**
	 * Constructor with api, use this!
	 * @param api
	 */
	public Game(String api){
		this.api = api;
	}
	
	/* *
	 * @return the md5Match
	 * /
	public boolean isMd5Match() {
		return md5Match;
	}

	/* *
	 * @param md5Match the md5Match to set
	 * /
	public void setMd5Match(boolean md5Match) {
		this.md5Match = md5Match;
	}*/
	
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
	public String getFile()
	{
		return file;
	}
	/**
	 * @param rom the rom to set
	 */
	public void setFile(String file)
	{
		this.file = file;
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
	 * @return the distance
	 */
	public int getDistance() {
		return distance;
	}

	/**
	 * @param distance the distance to set
	 */
	public void setDistance(int distance) {
		this.distance = distance;
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
	 * The score in percentage with 2 decimals
	 * @return
	 */
	public String getScoreInPercent()
	{
		return df.format(getScore() * 100) + "%";
	}
	
	/**
	 * The score between [0,1] with 2 decimals
	 * @return
	 */
	public String getScoreString()
	{
		return df.format(getScore());
	}
	
	/**
	 * @return the score; always 1 if matched by md5 of filename
	 */
	public double getScore() {
		if(matchingType == MatchingType.MD5) return 1.;
		if(matchingType == MatchingType.FILENAME) return 1.; // not sure if we should do that? works well with mame roms at least
		return score;
	}
	/**
	 * @param match the match to set
	 */
	public void setScore(double score) {
		this.score = score;
	}
	
	/**
	 * @return true if perfect match
	 */
	public boolean isPerfectMatch() {
		return score == 1;
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
	 * @return the matchingType
	 */
	public MatchingType getMatchingType() {
		return matchingType;
	}

	/**
	 * @param matchingType the matchingType to set
	 */
	public void setMatchingType(MatchingType matchingType) {
		this.matchingType = matchingType;
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
