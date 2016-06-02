# QDScraper
This is a rom scraper for recalbox.  
Complements [sselph's scraper](sslelph) which uses rom hashes to find matches, I use mainly the name of the rom.  
It started as a "quick and dirty" program for me, but I found interest in making it clean and sharing it, so all in all it's not bad.

## What it does
### Steps
1. it scans a directory for roms
2. it tries to get all the info possible for it from the Internet
3. it saves the best matches in your gamelist.xml file (either overwrites it or appends to it depending on the options)
4. it saves all "DUPE" data, ie. other matches, in DUPE_* files/folder

### APIs used
- [TheGamesDB](thegamesdb)
- [ScreenScraper](screenscraper)
- [GiantBomb](giantbomb) => You'll need an api key
- [IGDB](igdb) is considered for futur versions, it also needs an API key

### Platforms supported
I encoded all those platforms (those are the names of the folders in recalbox):  
amstradcpc, atari2600, atari7800, atarist, fba, fba_libretro, fds, gamegear, gb, gba, gbc, gw, lynx, mame, mastersystem, 
megadrive, msx, msx1, msx2, n64, neogeo, nes, ngp, ngpc, o2em, pcengine, pcenginecd, psx, sega32x, segacd, sg1000, snes, 
supergrafx, vectrex, virtualboy, wswan, wswanc, zxspectrum, psp

But there are some that I don't know much about, so I'd say it roughly works for them.
If you give another platform or if an API doesn't know about it, it will try to search without specifying the platform.  

Some platforms do more extensive searches:
- wonderswan color will also look for wonderswan games
- neogeo pocket color will also look for neogeo pocket games
- the **-arcade** option also triggers a wider search
- maybe others, it also depends on the APIs...  
Beware that game boy color won't look for game boy games


### Special features
- Knows about scummvm:  
	=> looks in directories for those and outputs the right xml for them  
	=> it will perform searches using the real name of the game
- Knows about roms consisting of multiple files (typically cue+bin, sub etc) => hides duplicates in recalbox
- Knows about bios files (at least some of them) => hides them in recalbox
- Knows mame rom names => it will perform searches using the real name of the game

## What it does not
- It can't be right 100% of the time, you'll need to check if it found the right games:  
	- Look at the images
	- Check the entry in the xml file there are clues, especially the name of the game on the API side

- It doesn't scan subfolders, except for the particular case of scummvm games


## What do I need ?
- Java 7 installed on your machine 
- A GiantBomb API key if you want the program to also use this API
- Internet, roms, Recalbox ;)  

## Command:
### Main
TODO: main command  

### Options
**-platform**  
This is the platform you want to search, it's the name of the folder you put your roms into, in recalbox. For example snes, segacd...
	
**-dir**  
The directory to scan for roms. It doesn't limit each system for its known extensions like recalbox, it just filters some extensions that might
be there and I'm sure they are not roms:  
xml, txt, jpg, png, gif, htm, html, doc, docx, ini, xls, dat, nv, fs  
If I should filter other things just open an issue.

**-appendToName** 
If you want the program to suffix all your roms by something, use this. I used it to add my patched roms, 
so all of them are now suffixed by (JP patch EN). It will add a space before the suffix.
 
**-useFilename**
Use this if you prefer the name of your rom instead of the name of the game from the API

**-cleanFilename**  
Use this to remove thing between () or [] in the name of your rom when it's used as the name of the game with -useFilename.  
- Remove things between () => -cleanFilename=()  
- Remove things between [] => -cleanFilename=[]  
- Remove things between () and [] => -cleanFilename=()[]
In fact I just look for the opening one so -cleanFilename=[( is perfectly fine

**-romFile**  
You can use this to use a file containing rom names instead of scanning a directory. 
One rom name per line.  
Example: -romFile=C:/recalbox/roms.txt  
Beware that this limits some searches based on md5 hash

**-arcade**  
Use this to look for arcade games, the rom name will be translated to the real name of the game. 
-platform is overridden by this option, you don't need it in this case

**-giantBombApiKey**  
Get your GiantBomb api key here: <http://www.giantbomb.com/api/>
This adds a place to look for games; more APIs = more chances to find that rare game!

**-overwrite**  
By default the new games found by the program are appended to the existing gamelist.xml file.
Use that to override it and look for all found roms.

**-proxyHost**  
If you're behind a proxy, set its host with this

**-proxyPort**  
If you're behind a proxy, set its port with this

**-user**  
If you're behind a proxy that needs authentication, set your username with this

**-password**  
If you're behind a proxy that needs authentication, set your password with this

**-properties**  
The program uses a java .properties file where some advanced options are. 
You can override this with your own if you feel like it, look at src/main/resources/scraper.properties for mine

**-help**  
Should print something similar to this list

### Examples
- todo
- todo

## Issues
### It's super slow
- Maybe it's due to [TheGamesDB](thegamesdb), sometimes it's down or just slow
- Maybe md5 hashing is slow on your machine, only files lighter than 45Mb are processed but maybe that's already too much? 
You can overwrite this with the "-properties" option 
- Maybe I made a mistake ;)

### Some games are wrong
Yes I know, it's performing searches mainly based on the rom name. There are a few things you can do to make the results better:
- Use [sselph's scraper](sselph) first, his results are 100% correct (except for arcade games maybe, I think my descriptions and images are better)
- Use a known romset (NoIntro)
- Name your roms correctly, put every non-name parts in () or [] so it's not used during the search.
	- Good example: "Bishi Bashi Special (E) [SLES-02537].bin" => will search for "Bishi Bashi Special"
	- Bad example: "Bishi Bashi Special - Europe - SLES-02537.bin" => will search for "Bishi Bashi Special Europe SLES 02537"

After all this, I'm sure sometimes there are still games that will be wrong, but hopefully everything is already one cut-and-paste command away.
Look in the file named DUPES/DUPE-<your-rom>.xml, the other matches are there. Find the correct one and replace the entry in gamelist.xml with this one. 
Also move the image it references.

### What about RetroPie ?
I don't know about RetroPie as I never used it, but if I understood it's basically the same thing, 
so maybe this is also suited for RetroPie or any Emulation Station based front-end 

### Other issues
If something goes wrong, you can use the issue function here on GitHub, and if I find some time I'll try to correct it. Always give me all the info so I can reproduce your bug.





[thegamesdb]: http://thegamesdb.net/
[screenscraper]: http://www.screenscraper.fr/
[giantbomb]: http://www.giantbomb.com/
[igdb]: https://www.igdb.com/
[sselph]: https://github.com/sselph/scraper