package daaa.qdscrapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


/**
 * This class will write the gamelist.xml file based on a list of games
 * 
 * @author kerndav
 *
 */
public class GamelistXML
{
	private String path;
	private static final String GAMELIST_ROOT_TAGNAME = "gameList";
	private static final String GAMELIST_GAME_TAGNAME = "game";
	private static final String ROM_PATH = "./";
	private static final String IMAGE_PATH = ROM_PATH + "downloaded_images/";
	private static final String TEMPLATE_GAME = 
			  "<path>{path}</path>\n"
			+ "<name>{name}</name>\n"
			+ "<desc>{desc}</desc>\n"
			+ "<image>{image}</image>\n"
			+ "<rating>{rating}</rating>\n"
			+ "<releasedate>{releasedate}</releasedate>\n"
			+ "<developer>{developer}</developer>\n"
			+ "<publisher>{publisher}</publisher>\n"
			+ "<genre>{genre}</genre>\n"
			+ "<players>{players}</players>\n";
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd'T000000'");
	
	@SuppressWarnings("unused")
	private String platform;
	private String addToName;
	private List<Game> games = new ArrayList<>();
	
	/**
	 * Constructor
	 * @param fileName the gamelist.xml file's name (used for DUPES too)
	 * @param args the app's arguments
	 */
	public GamelistXML(String fileName, Args args) //TODO: put all DUPE files in a single folder
	{
		super();
		this.path = args.romsDir + fileName;
		this.platform = args.platform;
		addToName = args.appendToName == null ? "" : (" " + args.appendToName);
	}

	/**
	 * Add a game to the list
	 * @param game
	 */
	public void addGame(Game game)
	{
		games.add(game);
	};
	
	// TODO: move to an xml utils class
	// Builds an open xml tag
	private String makeTagOpen(String tag) 
	{
		return makeTagOpen(tag, null);
	}
	// Builds an open xml tag with an attribute
	@SuppressWarnings("unused")
	private String makeTagOpen(String tag, String attrName, String attrValue)
	{
		Map<String, String> attrs = new HashMap<String, String>();
		if(attrValue != null)
		{
			attrs.put(attrName, attrValue);
		}
		return makeTagOpen(tag, attrs);
	}
	// Builds an open xml tag with many attributes
	private String makeTagOpen(String tag, Map<String, String> attrs) 
	{
		String ret =  "<" + tag;
		
		if(attrs != null)
		{
			for(Map.Entry<String, String> entry: attrs.entrySet())
			{
				ret = ret + " " + entry.getKey() + "=\"" + entry.getValue() + "\"";
			}
		}
		
		ret += ">";
		
		return ret;
	}
	// Builds a closed xml tag
	private String makeTagclosed(String tag) 
	{
		return "</" + tag + ">";
	}
	
	/**
	 * Opens a &lt;game> tag with an id and the source set as thegamesdb
	 * @param gameId id of the game on thegamesdb
	 * @return the open &lt;game> tag
	 */
	private String makeGameTagOpen(String gameId)
	{
		if(StringUtils.isEmpty(gameId))
		{
			return makeTagOpen(GAMELIST_GAME_TAGNAME);
		}
		// else
		Map<String, String> attrs = new HashMap<String, String>();
		attrs.put("id", gameId);
		attrs.put("source", "TheGamesDB");
		return makeTagOpen(GAMELIST_GAME_TAGNAME, attrs);
	}
	
	/**
	 * Get the number of games in this file
	 * @return the number of games in this file
	 */
	public int getNbGames()
	{
		return this.games.size();
	}
	
	/**
	 * Performs String.replace but allows null values for replacement (will use an empty String)
	 * @param in the String to make the replacement in
	 * @param toReplace the char sequence to look for
	 * @param replacement the replacement string, can be null
	 * @return the string with the replacement made
	 */
	private String replaceAllowNull(String in, String toReplace, String replacement)
	{
		if(replacement == null) replacement = "";
		return in.replace(toReplace, replacement);
	}
	
	/**
	 * Writes the gamelist.xml file, with the name given in the constructor
	 * @return
	 * @throws IOException
	 */
	public String writeFile() throws IOException
	{
		// folder structure
		File file = new File(path);
		Files.deleteIfExists(file.toPath());
		String parentFolder = file.getParent();
		Files.createDirectories(Paths.get(parentFolder));
		
		BufferedWriter out = Files.newBufferedWriter(Paths.get(path), Charset.forName("UTF-8"));
	
		// start
		out.write(makeTagOpen(GAMELIST_ROOT_TAGNAME) + "\n");
		
		for(Game game: games)
		{
			// start game
			out.write(QDUtils.tabulate(makeGameTagOpen(game.getId()), 1) + "\n");
			
			String str = QDUtils.tabulate(TEMPLATE_GAME, 2); 
			
			// format and escape everything first
			String path = ROM_PATH + StringEscapeUtils.escapeXml(game.getRom());
			String name = StringEscapeUtils.escapeXml(game.getName() + addToName);
			String desc = StringEscapeUtils.escapeXml(game.getDesc());
			String image = StringUtils.isEmpty(game.getImage()) ? "" : StringEscapeUtils.escapeXml(IMAGE_PATH + game.getImage());
			String rating = game.getRating() == 0 ? "" : "" + (game.getRating() / 10.0f);
			String releasedate = game.getReleasedate() == null ? "" : SDF.format(game.getReleasedate());
			String developer = StringUtils.isEmpty(game.getDeveloper()) ? "" : StringEscapeUtils.escapeXml(game.getDeveloper());
			String publisher = StringUtils.isEmpty(game.getPublisher()) ? "" : StringEscapeUtils.escapeXml(game.getPublisher());
			String genre = CollectionUtils.isEmpty(game.getGenres()) ? "" : StringUtils.join(game.getGenres(), "/"); // TODO: shorten the genres, remove "Action" if many
			String players = game.getPlayers();
			
			// add to the template
			str = replaceAllowNull(str, "{path}", path);
			str = replaceAllowNull(str, "{name}", name);
			str = replaceAllowNull(str, "{desc}", desc);
			str = replaceAllowNull(str, "{image}", image);
			str = replaceAllowNull(str, "{rating}", rating);
			str = replaceAllowNull(str, "{releasedate}", releasedate);
			str = replaceAllowNull(str, "{developer}", developer);
			str = replaceAllowNull(str, "{publisher}", publisher);
			str = replaceAllowNull(str, "{genre}", genre);
			str = replaceAllowNull(str, "{players}", players);
			
			out.write(str + "\n");
			
			// end game
			out.write(QDUtils.tabulate(makeTagclosed(GAMELIST_GAME_TAGNAME), 1) + "\n");
		}
		
		// end
		out.write(makeTagclosed(GAMELIST_ROOT_TAGNAME) + "\n");
		out.close();
		
		return path;
	}
	
	/**
	 * @return the path
	 */
	public String getPath()
	{
		return path;
	}
	/**
	 * @param path the path to set
	 */
	public void setPath(String path)
	{
		this.path = path;
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
