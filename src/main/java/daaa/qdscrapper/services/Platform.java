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
	
	// TODO: one ES platform = many API platforms
	/**
	 * Special platform to process arcade games (key in platform.properties).
	 */
	public static final String ARCADE = "arcade";
	/**
	 * Special platform to process neogeo games (key in platform.properties).
	 */
	public static final String NEOGEO = "neogeo"; //remove, it belongs to the arcade list
	
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

/////// IGDB ///////
/*
 	{
      "id": 50,
      "name": "3DO Interactive Multiplayer",
      "slug": "3do"
    },
    {
      "id": 116,
      "name": "Acorn Archimedes",
      "slug": "acorn-archimedes"
    },
    {
      "id": 16,
      "name": "Amiga",
      "slug": "amiga"
    },
    {
      "id": 114,
      "name": "Amiga CD32",
      "slug": "amiga-cd32"
    },
    {
      "id": 25,
      "name": "Amstrad CPC",
      "slug": "acpc"
    },
    {
      "id": 100,
      "name": "Analogue electronics",
      "slug": "analogueelectronics"
    },
    {
      "id": 34,
      "name": "Android",
      "slug": "android"
    },
    {
      "id": 75,
      "name": "Apple II",
      "slug": "appleii"
    },
    {
      "id": 115,
      "name": "Apple IIGS",
      "slug": "apple-iigs"
    },
    {
      "id": 52,
      "name": "Arcade",
      "slug": "arcade"
    },
    {
      "id": 59,
      "name": "Atari 2600",
      "slug": "atari2600"
    },
    {
      "id": 66,
      "name": "Atari 5200",
      "slug": "atari5200"
    },
    {
      "id": 60,
      "name": "Atari 7800",
      "slug": "atari7800"
    },
    {
      "id": 65,
      "name": "Atari 8-bit",
      "slug": "atari8bit"
    },
    {
      "id": 62,
      "name": "Atari Jaguar",
      "slug": "jaguar"
    },
    {
      "id": 61,
      "name": "Atari Lynx",
      "slug": "lynx"
    },
    {
      "id": 63,
      "name": "Atari ST/STE",
      "slug": "atari-st"
    },
    {
      "id": 91,
      "name": "Bally Astrocade",
      "slug": "astrocade"
    },
    {
      "id": 69,
      "name": "BBC Microcomputer System",
      "slug": "bbcmicro"
    },
    {
      "id": 73,
      "name": "BlackBerry OS",
      "slug": "blackberry"
    },
    {
      "id": 107,
      "name": "Call-A-Computer time-shared mainframe computer system",
      "slug": "call-a-computer"
    },
    {
      "id": 109,
      "name": "CDC Cyber 70",
      "slug": "cdccyber70"
    },
    {
      "id": 68,
      "name": "ColecoVision",
      "slug": "colecovision"
    },
    {
      "id": 93,
      "name": "Commodore 16",
      "slug": "c16"
    },
    {
      "id": 15,
      "name": "Commodore C64/128",
      "slug": "c64"
    }
    {
      "id": 94,
      "name": "Commodore Plus/4",
      "slug": "c-plus-4"
    },
    {
      "id": 71,
      "name": "Commodore VIC-20",
      "slug": "vic-20"
    },
    {
      "id": 98,
      "name": "DEC GT40",
      "slug": "gt40"
    },
    {
      "id": 85,
      "name": "Donner Model 30",
      "slug": "donner30"
    },
    {
      "id": 23,
      "name": "Dreamcast",
      "slug": "dc"
    },
    {
      "id": 102,
      "name": "EDSAC",
      "slug": "edsac--1"
    },
    {
      "id": 127,
      "name": "Fairchild Channel F",
      "slug": "fairchild-channel-f"
    },
    {
      "id": 99,
      "name": "Family Computer",
      "slug": "famicom"
    },
    {
      "id": 51,
      "name": "Family Computer Disk System",
      "slug": "fds"
    },
    {
      "id": 101,
      "name": "Ferranti Nimrod Computer",
      "slug": "nimrod"
    },
    {
      "id": 118,
      "name": "FM Towns",
      "slug": "fm-towns"
    },
    {
      "id": 33,
      "name": "Game Boy",
      "slug": "gb"
    },
    {
      "id": 24,
      "name": "Game Boy Advance",
      "slug": "gba"
    },
    {
      "id": 22,
      "name": "Game Boy Color",
      "slug": "gbc"
    },
    {
      "id": 104,
      "name": "HP 2100",
      "slug": "hp2100"
    },
    {
      "id": 105,
      "name": "HP 3000",
      "slug": "hp3000"
    },
    {
      "id": 111,
      "name": "Imlac PDS-1",
      "slug": "imlac-pds1"
    },
    {
      "id": 67,
      "name": "Intellivision",
      "slug": "intellivision"
    },
    {
      "id": 39,
      "name": "iOS",
      "slug": "ios"
    },
    {
      "id": 3,
      "name": "Linux",
      "slug": "linux"
    },
    {
      "id": 14,
      "name": "Mac",
      "slug": "mac"
    },
    {
      "id": 112,
      "name": "Microcomputer",
      "slug": "microcomputer--1"
    },
    {
      "id": 6,
      "name": "Microsoft Windows",
      "slug": "win"
    },
    {
      "id": 89,
      "name": "Microvision",
      "slug": "microvision--1"
    },
    {
      "id": 55,
      "name": "Mobile",
      "slug": "mobile"
    }
    {
      "id": 27,
      "name": "MSX",
      "slug": "msx"
    },
    {
      "id": 53,
      "name": "MSX2",
      "slug": "msx2"
    },
    {
      "id": 80,
      "name": "Neo Geo AES",
      "slug": "neogeoaes"
    },
    {
      "id": 79,
      "name": "Neo Geo MVS",
      "slug": "neogeomvs"
    },
    {
      "id": 119,
      "name": "Neo Geo Pocket",
      "slug": "neo-geo-pocket"
    },
    {
      "id": 120,
      "name": "Neo Geo Pocket Color",
      "slug": "neo-geo-pocket-color"
    },
    {
      "id": 42,
      "name": "N-Gage",
      "slug": "ngage"
    },
    {
      "id": 37,
      "name": "Nintendo 3DS",
      "slug": "3ds"
    },
    {
      "id": 4,
      "name": "Nintendo 64",
      "slug": "n64"
    },
    {
      "id": 20,
      "name": "Nintendo DS",
      "slug": "nds"
    },
    {
      "id": 18,
      "name": "Nintendo Entertainment System (NES)",
      "slug": "nes"
    },
    {
      "id": 21,
      "name": "Nintendo GameCube",
      "slug": "ngc"
    },
    {
      "id": 122,
      "name": "Nuon",
      "slug": "nuon"
    },
    {
      "id": 88,
      "name": "Odyssey",
      "slug": "odyssey--1"
    },
    {
      "id": 113,
      "name": "OnLive Game System",
      "slug": "onlive-game-system"
    },
    {
      "id": 72,
      "name": "Ouya",
      "slug": "ouya"
    },
    {
      "id": 125,
      "name": "PC-8801",
      "slug": "pc-8801"
    },
    {
      "id": 13,
      "name": "PC DOS",
      "slug": "dos"
    },
    {
      "id": 128,
      "name": "PC Engine SuperGrafx",
      "slug": "supergrafx"
    },
    {
      "id": 95,
      "name": "PDP-1",
      "slug": "pdp1"
    },
    {
      "id": 96,
      "name": "PDP-10",
      "slug": "pdp10"
    },
    {
      "id": 108,
      "name": "PDP-11",
      "slug": "pdp11"
    },
    {
      "id": 103,
      "name": "PDP-7",
      "slug": "pdp-7--1"
    },
    {
      "id": 97,
      "name": "PDP-8",
      "slug": "pdp-8--1"
    },
    {
      "id": 117,
      "name": "Philips CD-i",
      "slug": "philips-cd-i"
    }
     {
      "id": 110,
      "name": "PLATO",
      "slug": "plato--1"
    },
    {
      "id": 7,
      "name": "PlayStation",
      "slug": "ps"
    },
    {
      "id": 8,
      "name": "PlayStation 2",
      "slug": "ps2"
    },
    {
      "id": 9,
      "name": "PlayStation 3",
      "slug": "ps3"
    },
    {
      "id": 48,
      "name": "PlayStation 4",
      "slug": "ps4--1"
    },
    {
      "id": 45,
      "name": "PlayStation Network",
      "slug": "psn"
    },
    {
      "id": 38,
      "name": "PlayStation Portable",
      "slug": "psp"
    },
    {
      "id": 46,
      "name": "PlayStation Vita",
      "slug": "psvita"
    },
    {
      "id": 106,
      "name": "SDS Sigma 7",
      "slug": "sdssigma7"
    },
    {
      "id": 30,
      "name": "Sega 32X",
      "slug": "sega32"
    },
    {
      "id": 78,
      "name": "Sega CD",
      "slug": "segacd"
    },
    {
      "id": 35,
      "name": "Sega Game Gear",
      "slug": "gamegear"
    },
    {
      "id": 64,
      "name": "Sega Master System",
      "slug": "sms"
    },
    {
      "id": 29,
      "name": "Sega Mega Drive/Genesis",
      "slug": "smd"
    },
    {
      "id": 32,
      "name": "Sega Saturn",
      "slug": "saturn"
    },
    {
      "id": 84,
      "name": "SG-1000",
      "slug": "sg1000"
    },
    {
      "id": 77,
      "name": "Sharp X1",
      "slug": "x1"
    },
    {
      "id": 121,
      "name": "Sharp X68000",
      "slug": "sharp-x68000"
    },
    {
      "id": 92,
      "name": "SteamOS",
      "slug": "steam--1"
    },
    {
      "id": 58,
      "name": "Super Famicom",
      "slug": "sfam"
    },
    {
      "id": 19,
      "name": "Super Nintendo Entertainment System (SNES)",
      "slug": "snes--1"
    },
    {
      "id": 124,
      "name": "SwanCrystal",
      "slug": "swancrystal"
    },
    {
      "id": 44,
      "name": "Tapwave Zodiac",
      "slug": "zod"
    },
    {
      "id": 129,
      "name": "Texas Instruments TI-99",
      "slug": "ti-99"
    },
    {
      "id": 126,
      "name": "TRS-80",
      "slug": "trs-80"
    }
    {
      "id": 86,
      "name": "TurboGrafx-16/PC Engine",
      "slug": "turbografx16--1"
    },
    {
      "id": 70,
      "name": "Vectrex",
      "slug": "vectrex"
    },
    {
      "id": 87,
      "name": "Virtual Boy",
      "slug": "virtualboy"
    },
    {
      "id": 47,
      "name": "Virtual Console (Nintendo)",
      "slug": "vc"
    },
    {
      "id": 82,
      "name": "Web browser",
      "slug": "browser"
    },
    {
      "id": 5,
      "name": "Wii",
      "slug": "wii"
    },
    {
      "id": 41,
      "name": "Wii U",
      "slug": "wiiu"
    },
    {
      "id": 56,
      "name": "WiiWare",
      "slug": "wiiware"
    },
    {
      "id": 74,
      "name": "Windows Phone",
      "slug": "winphone"
    },
    {
      "id": 57,
      "name": "WonderSwan",
      "slug": "wonderswan"
    },
    {
      "id": 123,
      "name": "WonderSwan Color",
      "slug": "wonderswan-color"
    },
    {
      "id": 11,
      "name": "Xbox",
      "slug": "xbox"
    },
    {
      "id": 12,
      "name": "Xbox 360",
      "slug": "xbox360"
    },
    {
      "id": 36,
      "name": "Xbox Live Arcade",
      "slug": "xla"
    },
    {
      "id": 49,
      "name": "Xbox One",
      "slug": "xboxone"
    },
    {
      "id": 26,
      "name": "ZX Spectrum",
      "slug": "zxs"
    }
 */




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