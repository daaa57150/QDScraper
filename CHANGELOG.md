## Version 0.1 
Date: 2016-05-03


## Version 0.2
Date: 2016-07-14
- Bugfix NullPointer when all games already in gamelist
- Better ScreenScraper cleaning
- Some messages fixed
- Bugfix: ScreenScraper genres escaped (fixes Run & gun)
- Apply score even on match by filename/md5 (prevents bad rom association on ScreenScraper)
- Added png compression
- Moved ScreenScraper to last (big png covers, truncated descriptions, game screenshot instead of boxart)

## Version 0.3
- Bugfix with roms where the file contains a '&'
- TurboGrafx and PC Engine now also look for PC Engine CD games and vice versa
- The rom text file is now also searched in the "." and "-dir" folders so it doesn't need to be an absolute path anymore

## Version 0.4
- Don't stop if an image can't be resized, just print a message and use the base image
- Bugfix for ScreenScraper who likes to put "N/A" on ratings making it unparseable as a number
- Better exception catching, now if something bad happens with an API it doesn't stop

## Unreleased
- Increased http timeout to 90s
- Better exception management when TheGamesDB is down
- The games db changed the turbografx cd alias


