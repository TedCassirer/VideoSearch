# Links to index #

This folder contains YouTube video links to index.

Dataset source is http://netsg.cs.sfu.ca/youtubedata/ from section `2. Datasets of Updating Crawl` with date `Jul. 9th, 2008`.
It can be downloaded from here: http://netsg.cs.sfu.ca/youtubedata/080709u.zip.

## Content ##

`update.txt` - original crawling file that is downloaded from http://netsg.cs.sfu.ca/youtubedata/.

`prefixed.txt` - video IDs in `update.txt` are replaced with the YouTube links.

`links.txt` - selected the first column from `prefixed.txt`.

`video_links.txt` - final list of YouTube video links to be indexed.

## Instructions ##

Steps to obtain final list of valid YouTube video links from the original crawling file:

1. `awk '$0="https://www.youtube.com/watch?v="$0' update.txt > prefixed.txt`
2. `awk -F '\t' '{print $1}' prefixed.txt > links.txt`
3. `python validate_links.py`

## Links validation script ##

The `validate_links.py` script validates YouTube video links. It takes optional arguments:

* `-i`, `--input`, `default='links.txt'` - Input file with YouTube video links
* `-o`, `--output`, `default='video_links.txt'` - Output file with valid and existing YouTube video links
* `-l`, `--limit`, `default=1000`, - Limit for the number of links in the output file
