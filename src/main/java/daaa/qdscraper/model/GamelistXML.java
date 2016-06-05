package daaa.qdscraper.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import daaa.qdscraper.Props;
import daaa.qdscraper.services.Console;
import daaa.qdscraper.services.api.ApiService;
import daaa.qdscraper.utils.QDUtils;


/**
 * This class will write the gamelist.xml file based on a list of games
 * 
 * @author daaa
 */
public class GamelistXML
{
	private String path;
	private static final String GAMELIST_ROOT_TAGNAME = "gameList";
	private static final String GAMELIST_GAME_TAGNAME = "game";
	private static final String GAMELIST_FOLDER_TAGNAME = "folder";
	private static final String ROM_PATH = "./"; //must be slashes in recalbox
	private static final String IMAGE_PATH = ROM_PATH + Props.get("images.folder") + "/"; //must be slashes in recalbox
	//private static final String IMAGE_FOLDER = Props.get("images.folder");
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
	private static final String TEMPLATE_HIDDEN =
			  "<path>{path}</path>\n"
			+ "<name>{name}</name>\n"
			+ "<desc>{desc}</desc>\n"
			+ "<hidden>true</hidden>";
	private static final String DESC_BIOS = "This is a bios file";
	private static final String DESC_AUXILIARY = "This is an auxiliary file";
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMdd'T000000'");
	
	private String addToName;
	private List<Game> games = new ArrayList<>();
	
	// flags telling if the root tag is already opened/closed
	private boolean closed = false;
	private boolean open = false;
	
	// the file we're writing
	private BufferedWriter out = null;
	
	// the existing content of the gamelist.xml file if any
	private String existingContent = "";
	// the existing games names, so we can check before searching
	private List<String> existingGames = new ArrayList<String>();

	/**
	 * Constructor
	 * @param path path to the file to create
	 * @param addToName if something needs to be appended to the name of the game
	 * @throws IOException 
	 */
	public GamelistXML(String path, String addToName, boolean overwrite) 
	throws IOException
	{
		super();
		this.path = path;
		this.addToName = addToName == null ? "" : (" " + addToName);
		
		// folder structure
		File file = new File(path);
		if(overwrite) {
			if(file.exists()) {
				//Console.println("Overwriting file " + path);
				Files.deleteIfExists(file.toPath());
			}
			else {
				// Console.println("File " + path + "does not exist");
				String parentFolder = file.getParent();
				Files.createDirectories(Paths.get(parentFolder));
			}
		}
		else { // don't overwrite
			if(file.exists()) {
				Console.println("Reading existing file " + path + " ...");
				parseExisting(path);
			}
			else {
				String parentFolder = file.getParent();
				Files.createDirectories(Paths.get(parentFolder));
			}
		}	
	}

	/**
	 * Add a game to the list
	 * @param game
	 * @throws IOException 
	 */
	public void addGame(Game game) 
	throws IOException
	{
		games.add(game); 
		if(!open)
		{
			// start
			out = Files.newBufferedWriter(Paths.get(path), Charset.forName("UTF-8"));
			out.write(QDUtils.makeTagOpen(GAMELIST_ROOT_TAGNAME) + "\n");
			out.write(existingContent);
			open = true;
		}
		else if(closed)
		{
			throw new IOException("File "+path+"already closed");
		}
		
		// add the game now
		// is it an hidden file ?
		if(game.isBios() || game.isAuxiliary())
		{
			// start game
			out.write(QDUtils.tabulate(makeTagOpen(GAMELIST_GAME_TAGNAME, game), 1) + "\n");
			
			// template 
			String str = QDUtils.tabulate(TEMPLATE_HIDDEN, 2);
		
			// path to the rom in the recalbox
			String romPath = ROM_PATH + game.getFile().replaceAll("\\\\", "/"); //in recalbox they must be slashes
			
			String filename = FilenameUtils.removeExtension(Paths.get(game.getFile()).getFileName().toString());
			
			// add to the template
			str = replaceAllowNull(str, "{path}", romPath);
			str = replaceAllowNull(str, "{name}", filename);
			str = replaceAllowNull(str, "{desc}", game.isBios() ? DESC_BIOS : DESC_AUXILIARY);
			
			out.write(str + "\n");
			
			// end game
			out.write(QDUtils.tabulate(QDUtils.makeTagclosed(GAMELIST_GAME_TAGNAME), 1) + "\n");
		}
		else // a game
		{
			// find out if this is a scummvm type of rom, which absolutely needs the meta on its folder
			Path rom = Paths.get(game.getFile());
			boolean isInAFolder = rom.getNameCount() == 2;
			if(isInAFolder) //this is a scummvm-like rom
			{
				// start folder
				out.write(QDUtils.tabulate(makeTagOpen(GAMELIST_FOLDER_TAGNAME, game), 1) + "\n");
				
				String str = fillTemplate(TEMPLATE_GAME, game, 2);
				out.write(str + "\n");
				
				// end folder
				out.write(QDUtils.tabulate(QDUtils.makeTagclosed(GAMELIST_FOLDER_TAGNAME), 1) + "\n");
			}
			
			// start game
			out.write(QDUtils.tabulate(makeTagOpen(GAMELIST_GAME_TAGNAME, game), 1) + "\n");
			
			String str = TEMPLATE_GAME;
			if(isInAFolder)
			{
				str = replaceAllowNull(str, "{name}", "Start {name}");
			}
			
			str = fillTemplate(str, game, 2);
			out.write(str + "\n");
			
			// end game
			out.write(QDUtils.tabulate(QDUtils.makeTagclosed(GAMELIST_GAME_TAGNAME), 1) + "\n");
		}

		out.flush();
	};
	
	/**
	 * Closes the file; ie the root tag and the writer
	 * @throws IOException
	 */
	public void close() 
	throws IOException
	{
		if(closed)
		{
			Console.printErr("File " + path + " already closed");
			return;
		}
		if(open /*&& !closed*/)
		{
			// end
			out.write(QDUtils.makeTagclosed(GAMELIST_ROOT_TAGNAME) + "\n");
		}
		
		if(out != null)
		{
			out.flush();
			out.close();
		}
		closed = true;
	}
	
	/**
	 * Opens a tag with an id, source, score, api-title
	 * @param game the result game
	 * @return the open tag
	 */
	private String makeTagOpen(String tag, Game game)
	{
		String api = game.getApi();
		String gameId = game.getId();
		String apiGameTitle = StringEscapeUtils.escapeXml10(game.getTitle());
		String scraper = game.getScraper();
		if(StringUtils.isEmpty(scraper)) {
			scraper = "?"; // other scrapers should add their id
		}
		
		Map<String, String> attrs = new HashMap<String, String>();
		
		attrs.put("id", gameId);
		attrs.put("api-title", apiGameTitle);
		attrs.put("scraper", game.getScraper());
		
		
		if(!StringUtils.isEmpty(api)) {
			attrs.put("source", api);
		}
		if(game.getScore() != 0) {
			attrs.put("score", game.getScoreInPercent());
		}
		if(game.getDistance() != Integer.MAX_VALUE && game.getDistance() != 0) {
			attrs.put("distance", ""+game.getDistance());
		}
		if(game.getMatchingType() != null) {
			attrs.put("matching-type", game.getMatchingType().name().toLowerCase());
		}
		return QDUtils.makeTagOpen(tag, attrs);
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
		//return in.replaceFirst(toReplace, replacement);
		return StringUtils.replaceOnce(in, toReplace, replacement);
	}
	
	/**
	 * Replaces all placeholders '{xxx}' in a template with its value from the game
	 * @param template the template to use
	 * @param game the game to process
	 * @param nbTabulations number of tabulations to apply on each xml element
	 * @return
	 */
	private String fillTemplate(String template, Game game, int nbTabulations)
	{
		// path to the rom in the recalbox
		String romPath = ROM_PATH + game.getFile().replaceAll("[\\\\]", "/"); //in recalbox they must be slashes
		
		// format and escape everything
		String name = StringEscapeUtils.escapeXml10(game.getName() + addToName);
		String desc = StringEscapeUtils.escapeXml10(game.getDesc()); //add the legal text? let's see if igdb says something
		String image = StringUtils.isEmpty(game.getImage()) ? "" : StringEscapeUtils.escapeXml10(IMAGE_PATH + game.getImage()); //must be slashes in recalbox
		String rating = game.getRating() == 0 ? "" : "" + game.getRating();
		String releasedate = game.getReleasedate() == null ? "" : SDF.format(game.getReleasedate());
		String developer = StringUtils.isEmpty(game.getDeveloper()) ? "" : StringEscapeUtils.escapeXml10(game.getDeveloper());
		String publisher = StringUtils.isEmpty(game.getPublisher()) ? "" : StringEscapeUtils.escapeXml10(game.getPublisher());
		List<String> genres = ApiService.cleanGenres(game.getGenres());
		String genre = CollectionUtils.isEmpty(genres) ? "" : StringUtils.join(genres, "/"); 
		genre = StringEscapeUtils.escapeXml10(genre);
		String players = game.getPlayers();
		
		// template
		String str = QDUtils.tabulate(template, nbTabulations);
		
		// add to the template
		str = replaceAllowNull(str, "{path}", romPath);
		str = replaceAllowNull(str, "{name}", name);
		str = replaceAllowNull(str, "{desc}", desc);
		str = replaceAllowNull(str, "{image}", image);
		str = replaceAllowNull(str, "{rating}", rating);
		str = replaceAllowNull(str, "{releasedate}", releasedate);
		str = replaceAllowNull(str, "{developer}", developer);
		str = replaceAllowNull(str, "{publisher}", publisher);
		str = replaceAllowNull(str, "{genre}", genre);
		str = replaceAllowNull(str, "{players}", players);
		
		return str;
	}
	
	/**
	 * Saves the existing content of the gameList tag in a gamelist.xml file; this will be output in the resulting file.
	 * @param filepath the gamelist.xml file to parse
	 */
	public void parseExisting(String filepath)
	{
		File file = new File(filepath);
		if(!file.exists()) {
			Console.println("The gamelist.xml file does not exist: " + filepath);
			return;
		}
		
		try
		{
			InputStream in = new FileInputStream(file);
			String xml = IOUtils.toString(in);
			Document doc = QDUtils.parseXML(xml);
			XPath xpath = QDUtils.getXPath();
			
			// saving the xml as is
			xml = xml.replace("<gameList>", "");
			xml = xml.replace("</gameList>", "");
			if(xml.startsWith("\n")) {
				xml = xml.substring(1);
			}
//			if(xml.endsWith("\n")) {
//				xml = xml.substring(0, xml.length() - 1);
//			}
			existingContent = xml;
			
			// parsing just the names of the files
			int i;
			for(i=1; ; i++)
			{
				Object gameO = xpath.evaluate("gameList/game["+i+"]", doc, XPathConstants.NODE);
				if(gameO == null) break;
				
				Element gameE = (Element)gameO;
				NodeList paths = gameE.getElementsByTagName("path");
				Node pathO = paths.item(0);
				String game = pathO.getTextContent();
				game = FilenameUtils.getName(game);  // does work on / or \ whatever the system it runs on
				existingGames.add(game);
			}
			Console.println("The existing gamelist.xml file already contains " + (i-1) + " games");
		}
		catch(IOException | ParserConfigurationException | SAXException | XPathExpressionException e)
		{
			Console.printErr("Exception when parsing file " + filepath);
			Console.printErr(e);
		}
	}
	
	/**
	 * Tells if this gamelist already contains a file.
	 * This has the limitation has it doesn't care if the rom is in a folder or not
	 * @param rom
	 * @return
	 */
	public boolean contains(Rom rom) {
		String name = FilenameUtils.getName(rom.getFile());
		return existingGames.contains(name);
	}
	
//	public GameCollection parse(String filepath)
//	{
//		File file = new File(filepath);
//		if(!file.exists()) {
//			Console.println("The gamelist.xml file does not exist: " + filepath);
//			return null;
//		}
//		
//		try
//		{
//			InputStream in = new FileInputStream(file);
//			String xml = IOUtils.toString(in);
//			Document doc = QDUtils.parseXML(xml);
//			XPath xpath = QDUtils.getXPath();
//			
//			GameCollection games = new GameCollection();
//			
//			for(int i=1; ; i++)
//			{
//				Object gameO = xpath.evaluate("gameList/game["+i+"]", doc, XPathConstants.NODE);
//				if(gameO == null) break;
//				
//				Element gameE = (Element)gameO;
//				
//				// attributes
//				String id = gameE.getAttribute("id");
//				String scraper = gameE.getAttribute("scraper");
//				String source = gameE.getAttribute("source");
//				String score = gameE.getAttribute("score");
//				String apiTitle = StringEscapeUtils.unescapeXml(gameE.getAttribute("api-title"));
//				String matchingType = gameE.getAttribute("matching-type");
//				String distance = gameE.getAttribute("distance");
//				
//				// nodes
//				String path = null;
//				String name = null;
//				String desc = null;
//				String image = null;
//				String rating = null;
//				String releasedate = null;
//				String developer = null;
//				String publisher = null;
//				String genre = null;
//				String players = null;
//				String hidden = null;
//				
//				NodeList nodes = gameE.getChildNodes();
//				for(int n = 0; n<nodes.getLength(); n++)
//				{
//					Node node = nodes.item(n);
//					switch(node.getNodeName()) {
//						case "path": {
//							path = node.getNodeValue();
//							break;
//						}
//						case "name": {
//							name = node.getNodeValue();
//							break;
//						}
//						case "desc": {
//							desc = node.getNodeValue();
//							break;
//						}
//						case "image": {
//							image = node.getNodeValue();
//							break;
//						}
//						case "rating": {
//							rating = node.getNodeValue();
//							break;
//						}
//						case "releasedate": {
//							releasedate = node.getNodeValue();
//							break;
//						}
//						case "developer": {
//							developer = node.getNodeValue();
//							break;
//						}
//						case "publisher": {
//							publisher = node.getNodeValue();
//							break;
//						}
//						case "genre": {
//							genre = node.getNodeValue();
//							break;
//						}
//						case "players": {
//							players = node.getNodeValue();
//							break;
//						}
//						case "hidden": {
//							hidden = node.getNodeValue();
//							break;
//						}
//						default: {
//							Console.println("Unknown tag " + node.getNodeName());
//						}
//					}
//				}
//				
//				// now set everything
//				Game game = new Game(source, scraper);
//				game.set
//			}
//			
//			return games;
//		}
//		catch(IOException | ParserConfigurationException | SAXException | XPathExpressionException e)
//		{
//			Console.printErr("Exception when parsing file " + filepath);
//			Console.printErr(e);
//		}
//		
//		return null;
//	}
	
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
	 * @return the closed
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * @return the open
	 */
	public boolean isOpen() {
		return open;
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
