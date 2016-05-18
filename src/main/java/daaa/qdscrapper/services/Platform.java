package daaa.qdscrapper.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import daaa.qdscrapper.utils.QDUtils;

/**
 * Converts ES platform names to thegamesdb platform names
 * Loads its content from the platform.properties file
 * 
 * @author daaa
 *
 */
public class Platform
{
	// supported by thegamesdb != supported by giantbomb
	private static final Properties PLATFORMS = QDUtils.loadClasspathProperties("platform.properties");
	private static Set<String> SUPPORTED_PLATFORMS;
	static {
		SUPPORTED_PLATFORMS = new HashSet<>();
		for(Object key: Collections.list(PLATFORMS.keys()))
		{
			SUPPORTED_PLATFORMS.add(key.toString().split("[.]")[0]);
		}
	}
	
	/**
	 * Special platform to process arcade games (key in platform.properties).
	 */
	public static final String ARCADE = "arcade";
	/**
	 * Special platform to process neogeo games (key in platform.properties).
	 */
	public static final String NEOGEO = "neogeo";
	
	/**
	 * Input the ES name, get the GamesDB name
	 * @param esName name of the ES platform (snes, nes, ngpc...)
	 * @return
	 */
	public static String asTheGamesDB(String esName)
	{
		if(esName == null) return null;
		return PLATFORMS.getProperty(esName + ".thegamesdb");
	}
	
	/**
	 * Input the ES name, get the GamesDB name
	 * @param esName name of the ES platform (snes, nes, ngpc...)
	 * @return
	 */
	public static String asGiantBomb(String esName)
	{
		if(esName == null) return null;
		return PLATFORMS.getProperty(esName + ".giantbomb");
	}
	
	/**
	 * Does this app know about the platform?
	 * 
	 * @param platform the platform
	 * @return
	 */
	public static boolean isSupported(String platform) {
		return SUPPORTED_PLATFORMS.contains(platform + ".thegamesdb");
	}
	
	/**
	 * Get the list of all platforms this app knows about, in ES format
	 * @return the list of all platforms this app knows about, in ES format
	 */
	public static Set<String> getSupportedPlatforms() {
		return SUPPORTED_PLATFORMS;
	}
}

//////// THE GAMES DB ///////
/*
<Data>
	<basePlatformUrl>http://thegamesdb.net/platform/</basePlatformUrl>
	<Platforms>
		<Platform>
			<id>25</id>
			<name>3DO</name>
			<alias>3do</alias>
		</Platform>
		<Platform>
			<id>4944</id>
			<name>Acorn Archimedes</name>
			<alias>acorn-archimedes</alias>
		</Platform>
		<Platform>
			<id>4954</id>
			<name>Acorn Electron</name>
			<alias>acorn-electron</alias>
		</Platform>
		<Platform>
			<id>4911</id>
			<name>Amiga</name>
			<alias>amiga</alias>
		</Platform>
		<Platform>
			<id>4947</id>
			<name>Amiga CD32</name>
			<alias>amiga-cd32</alias>
		</Platform>
		<Platform>
			<id>4914</id>
			<name>Amstrad CPC</name>
			<alias>amstrad-cpc</alias>
		</Platform>
		<Platform>
			<id>4916</id>
			<name>Android</name>
			<alias>android</alias>
		</Platform>
		<Platform>
			<id>4942</id>
			<name>Apple II</name>
			<alias>apple2</alias>
		</Platform>
		<Platform>
			<id>23</id>
			<name>Arcade</name>
			<alias>arcade</alias>
		</Platform>
		<Platform>
			<id>22</id>
			<name>Atari 2600</name>
			<alias>atari-2600</alias>
		</Platform>
		<Platform>
			<id>26</id>
			<name>Atari 5200</name>
			<alias>atari-5200</alias>
		</Platform>
		<Platform>
			<id>27</id>
			<name>Atari 7800</name>
			<alias>atari-7800</alias>
		</Platform>
		<Platform>
			<id>4943</id>
			<name>Atari 800</name>
			<alias>atari800</alias>
		</Platform>
		<Platform>
			<id>28</id>
			<name>Atari Jaguar</name>
			<alias>atari-jaguar</alias>
		</Platform>
		<Platform>
			<id>29</id>
			<name>Atari Jaguar CD</name>
			<alias>atari-jaguar-cd</alias>
		</Platform>
		<Platform>
			<id>4924</id>
			<name>Atari Lynx</name>
			<alias>atari-lynx</alias>
		</Platform>
		<Platform>
			<id>4937</id>
			<name>Atari ST</name>
			<alias>atari-st</alias>
		</Platform>
		<Platform>
			<id>30</id>
			<name>Atari XE</name>
			<alias>atari-xe</alias>
		</Platform>
		<Platform>
			<id>31</id>
			<name>Colecovision</name>
			<alias>colecovision</alias>
		</Platform>
		<Platform>
			<id>4946</id>
			<name>Commodore 128</name>
			<alias>c128</alias>
		</Platform>
		<Platform>
			<id>40</id>
			<name>Commodore 64</name>
			<alias>commodore-64</alias>
		</Platform>
		<Platform>
			<id>4945</id>
			<name>Commodore VIC-20</name>
			<alias>commodore-vic20</alias>
		</Platform>
		<Platform>
			<id>4952</id>
			<name>Dragon 32/64</name>
			<alias>dragon32-64</alias>
		</Platform>
		<Platform>
			<id>4928</id>
			<name>Fairchild Channel F</name>
			<alias>fairchild</alias>
		</Platform>
		<Platform>
			<id>4936</id>
			<name>Famicom Disk System</name>
			<alias>fds</alias>
		</Platform>
		<Platform>
			<id>4932</id>
			<name>FM Towns Marty</name>
			<alias>fmtowns</alias>
		</Platform>
		<Platform>
			<id>4950</id>
			<name>Game &amp; Watch</name>
			<alias>game-and-watch</alias>
		</Platform>
		<Platform>
			<id>4940</id>
			<name>Game.com</name>
			<alias>game-com</alias>
		</Platform>
		<Platform>
			<id>4951</id>
			<name>Handheld Electronic Games (LCD)</name>
			<alias>lcd</alias>
		</Platform>
		<Platform>
			<id>32</id>
			<name>Intellivision</name>
			<alias>intellivision</alias>
		</Platform>
		<Platform>
			<id>4915</id>
			<name>iOS</name>
			<alias>ios</alias>
		</Platform>
		<Platform>
			<id>37</id>
			<name>Mac OS</name>
			<alias>mac-os</alias>
		</Platform>
		<Platform>
			<id>4927</id>
			<name>Magnavox Odyssey 2</name>
			<alias>magnavox-odyssey-2</alias>
		</Platform>
		<Platform>
			<id>4948</id>
			<name>Mega Duck</name>
			<alias>megaduck</alias>
		</Platform>
		<Platform>
			<id>14</id>
			<name>Microsoft Xbox</name>
			<alias>microsoft-xbox</alias>
		</Platform>
		<Platform>
			<id>15</id>
			<name>Microsoft Xbox 360</name>
			<alias>microsoft-xbox-360</alias>
		</Platform>
		<Platform>
			<id>4920</id>
			<name>Microsoft Xbox One</name>
			<alias>microsoft-xbox-one</alias>
		</Platform>
		<Platform>
			<id>4929</id>
			<name>MSX</name>
			<alias>msx</alias>
		</Platform>
		<Platform>
			<id>4938</id>
			<name>N-Gage</name>
			<alias>ngage</alias>
		</Platform>
		<Platform>
			<id>4922</id>
			<name>Neo Geo Pocket</name>
			<alias>neo-geo-pocket</alias>
		</Platform>
		<Platform>
			<id>4923</id>
			<name>Neo Geo Pocket Color</name>
			<alias>neo-geo-pocket-color</alias>
		</Platform>
		<Platform>
			<id>24</id>
			<name>NeoGeo</name>
			<alias>neogeo</alias>
		</Platform>
		<Platform>
			<id>4912</id>
			<name>Nintendo 3DS</name>
			<alias>nintendo-3ds</alias>
		</Platform>
		<Platform>
			<id>3</id>
			<name>Nintendo 64</name>
			<alias>nintendo-64</alias>
		</Platform>
		<Platform>
			<id>8</id>
			<name>Nintendo DS</name>
			<alias>nintendo-ds</alias>
		</Platform>
		<Platform>
			<id>7</id>
			<name>Nintendo Entertainment System (NES)</name>
			<alias>nintendo-entertainment-system-nes</alias>
		</Platform>
		<Platform>
			<id>4</id>
			<name>Nintendo Game Boy</name>
			<alias>nintendo-gameboy</alias>
		</Platform>
		<Platform>
			<id>5</id>
			<name>Nintendo Game Boy Advance</name>
			<alias>nintendo-gameboy-advance</alias>
		</Platform>
		<Platform>
			<id>41</id>
			<name>Nintendo Game Boy Color</name>
			<alias>nintendo-gameboy-color</alias>
		</Platform>
		<Platform>
			<id>2</id>
			<name>Nintendo GameCube</name>
			<alias>nintendo-gamecube</alias>
		</Platform>
		<Platform>
			<id>4918</id>
			<name>Nintendo Virtual Boy</name>
			<alias>nintendo-virtual-boy</alias>
		</Platform>
		<Platform>
			<id>9</id>
			<name>Nintendo Wii</name>
			<alias>nintendo-wii</alias>
		</Platform>
		<Platform>
			<id>38</id>
			<name>Nintendo Wii U</name>
			<alias>nintendo-wii-u</alias>
		</Platform>
		<Platform>
			<id>4935</id>
			<name>Nuon</name>
			<alias>nuon</alias>
		</Platform>
		<Platform>
			<id>4921</id>
			<name>Ouya</name>
			<alias>ouya</alias>
		</Platform>
		<Platform>
			<id>1</id>
			<name>PC</name>
			<alias>pc</alias>
		</Platform>
		<Platform>
			<id>4933</id>
			<name>PC-88</name>
			<alias>pc88</alias>
		</Platform>
		<Platform>
			<id>4934</id>
			<name>PC-98</name>
			<alias>pc98</alias>
		</Platform>
		<Platform>
			<id>4930</id>
			<name>PC-FX</name>
			<alias>pcfx</alias>
		</Platform>
		<Platform>
			<id>4917</id>
			<name>Philips CD-i</name>
			<alias>philips-cd-i</alias>
		</Platform>
		<Platform>
			<id>4953</id>
			<name>R DELETE ME WHAT AM I</name>
		</Platform>
		<Platform>
			<id>33</id>
			<name>Sega 32X</name>
			<alias>sega-32x</alias>
		</Platform>
		<Platform>
			<id>21</id>
			<name>Sega CD</name>
			<alias>sega-cd</alias>
		</Platform>
		<Platform>
			<id>16</id>
			<name>Sega Dreamcast</name>
			<alias>sega-dreamcast</alias>
		</Platform>
		<Platform>
			<id>20</id>
			<name>Sega Game Gear</name>
			<alias>sega-game-gear</alias>
		</Platform>
		<Platform>
			<id>18</id>
			<name>Sega Genesis</name>
			<alias>sega-genesis</alias>
		</Platform>
		<Platform>
			<id>35</id>
			<name>Sega Master System</name>
			<alias>sega-master-system</alias>
		</Platform>
		<Platform>
			<id>36</id>
			<name>Sega Mega Drive</name>
		</Platform>
		<Platform>
			<id>17</id>
			<name>Sega Saturn</name>
			<alias>sega-saturn</alias>
		</Platform>
		<Platform>
			<id>4949</id>
			<name>SEGA SG-1000</name>
			<alias>sg1000</alias>
		</Platform>
		<Platform>
			<id>4913</id>
			<name>Sinclair ZX Spectrum</name>
			<alias>sinclair-zx-spectrum</alias>
		</Platform>
		<Platform>
			<id>10</id>
			<name>Sony Playstation</name>
			<alias>sony-playstation</alias>
		</Platform>
		<Platform>
			<id>11</id>
			<name>Sony Playstation 2</name>
			<alias>sony-playstation-2</alias>
		</Platform>
		<Platform>
			<id>12</id>
			<name>Sony Playstation 3</name>
			<alias>sony-playstation-3</alias>
		</Platform>
		<Platform>
			<id>4919</id>
			<name>Sony Playstation 4</name>
			<alias>sony-playstation-4</alias>
		</Platform>
		<Platform>
			<id>39</id>
			<name>Sony Playstation Vita</name>
			<alias>sony-playstation-vita</alias>
		</Platform>
		<Platform>
			<id>13</id>
			<name>Sony PSP</name>
			<alias>sony-psp</alias>
		</Platform>
		<Platform>
			<id>6</id>
			<name>Super Nintendo (SNES)</name>
			<alias>super-nintendo-snes</alias>
		</Platform>
		<Platform>
			<id>4941</id>
			<name>TRS-80 Color Computer</name>
			<alias>trs80-color</alias>
		</Platform>
		<Platform>
			<id>4955</id>
			<name>Turbo Grafx 16 CD</name>
			<alias>turbo-grafx-16-cd</alias>
		</Platform>
		<Platform>
			<id>34</id>
			<name>TurboGrafx 16</name>
			<alias>turbografx-16</alias>
		</Platform>
		<Platform>
			<id>4939</id>
			<name>Vectrex</name>
			<alias>vectrex</alias>
		</Platform>
		<Platform>
			<id>4925</id>
			<name>WonderSwan</name>
			<alias>wonderswan</alias>
		</Platform>
		<Platform>
			<id>4926</id>
			<name>WonderSwan Color</name>
			<alias>wonderswan-color</alias>
		</Platform>
		<Platform>
			<id>4931</id>
			<name>X68000</name>
			<alias>x68000</alias>
		</Platform>
	</Platforms>
</Data>

*/


//////// GIANT BOMB ////////
/*
results: [
{
id: 1,
name: "Amiga"
},
{
id: 3,
name: "Game Boy"
},
{
id: 4,
name: "Game Boy Advance"
},
{
id: 5,
name: "Game Gear"
},
{
id: 6,
name: "Genesis"
},
{
id: 7,
name: "Atari Lynx"
},
{
id: 8,
name: "Sega Master System"
},
{
id: 9,
name: "Super Nintendo Entertainment System"
},
{
id: 11,
name: "Amstrad CPC"
},
{
id: 12,
name: "Apple II"
},
{
id: 13,
name: "Atari ST"
},
{
id: 14,
name: "Commodore 64"
},
{
id: 15,
name: "MSX"
},
{
id: 16,
name: "ZX Spectrum"
},
{
id: 17,
name: "Mac"
},
{
id: 18,
name: "PlayStation Portable"
},
{
id: 19,
name: "PlayStation 2"
},
{
id: 20,
name: "Xbox 360"
},
{
id: 21,
name: "Nintendo Entertainment System"
},
{
id: 22,
name: "PlayStation"
},
{
id: 23,
name: "GameCube"
},
{
id: 24,
name: "Atari 8-bit"
},
{
id: 25,
name: "Neo Geo"
},
{
id: 26,
name: "3DO"
},
{
id: 27,
name: "CD-i"
},
{
id: 28,
name: "Jaguar"
},
{
id: 29,
name: "Sega CD"
},
{
id: 30,
name: "VIC-20"
},
{
id: 31,
name: "Sega 32X"
},
{
id: 32,
name: "Xbox"
},
{
id: 34,
name: "N-Gage"
},
{
id: 35,
name: "PlayStation 3"
},
{
id: 36,
name: "Wii"
},
{
id: 37,
name: "Dreamcast"
},
{
id: 38,
name: "Apple IIgs"
},
{
id: 39,
name: "Amiga CD32"
},
{
id: 40,
name: "Atari 2600"
},
{
id: 42,
name: "Saturn"
},
{
id: 43,
name: "Nintendo 64"
},
{
id: 47,
name: "ColecoVision"
},
{
id: 48,
name: "TI-99/4A"
},
{
id: 51,
name: "Intellivision"
},
{
id: 52,
name: "Nintendo DS"
},
{
id: 53,
name: "TurboGrafx-CD"
},
{
id: 54,
name: "WonderSwan Color"
},
{
id: 55,
name: "TurboGrafx-16"
},
{
id: 57,
name: "Game Boy Color"
},
{
id: 58,
name: "Commodore 128"
},
{
id: 59,
name: "Neo Geo CD"
},
{
id: 60,
name: "Odyssey 2"
},
{
id: 61,
name: "Dragon 32/64"
},
{
id: 62,
name: "Commodore PET/CBM"
},
{
id: 63,
name: "TRS-80"
},
{
id: 64,
name: "Zodiac"
},
{
id: 65,
name: "WonderSwan"
},
{
id: 66,
name: "Channel F"
},
{
id: 67,
name: "Atari 5200"
},
{
id: 68,
name: "TRS-80 CoCo"
},
{
id: 70,
name: "Atari 7800"
},
{
id: 72,
name: "iPod"
},
{
id: 74,
name: "Odyssey"
},
{
id: 75,
name: "PC-FX"
},
{
id: 76,
name: "Vectrex"
},
{
id: 77,
name: "Game.Com"
},
{
id: 78,
name: "Gizmondo"
},
{
id: 79,
name: "Virtual Boy"
},
{
id: 80,
name: "Neo Geo Pocket"
},
{
id: 81,
name: "Neo Geo Pocket Color"
},
{
id: 82,
name: "V.Smile"
},
{
id: 83,
name: "Pinball"
},
{
id: 84,
name: "Arcade"
},
{
id: 85,
name: "NUON"
},
{
id: 86,
name: "Xbox 360 Games Store"
},
{
id: 87,
name: "Wii Shop"
},
{
id: 88,
name: "PlayStation Network (PS3)"
},
{
id: 89,
name: "Leapster"
},
{
id: 90,
name: "Microvision"
},
{
id: 91,
name: "Famicom Disk System"
},
{
id: 92,
name: "Pioneer LaserActive"
},
{
id: 93,
name: "Adventure Vision"
},
{
id: 94,
name: "PC"
},
{
id: 95,
name: "Sharp X68000"
},
{
id: 96,
name: "iPhone"
},
{
id: 98,
name: "Satellaview"
},
{
id: 99,
name: "Arcadia 2001"
},
{
id: 100,
name: "Aquarius"
},
{
id: 101,
name: "Nintendo 64DD"
},
{
id: 102,
name: "Pippin"
},
{
id: 103,
name: "R-Zone"
},
{
id: 104,
name: "HyperScan"
},
{
id: 105,
name: "Game Wave"
},
{
id: 106,
name: "DSiWare"
},
{
id: 107,
name: "RDI Halcyon"
},
{
id: 108,
name: "FM Towns"
},
{
id: 109,
name: "NEC PC-8801"
},
{
id: 110,
name: "BBC Micro"
},
{
id: 111,
name: "PLATO"
},
{
id: 112,
name: "NEC PC-9801"
},
{
id: 113,
name: "Sharp X1"
},
{
id: 114,
name: "FM-7"
}*/