# Links to index #

This folder contains YouTube video links to index.

Dataset source is http://netsg.cs.sfu.ca/youtubedata/ from section `1. Datasets of Normal Crawl` with date `Jul. 27th, 2008`.
It can be downloaded from here: http://netsg.cs.sfu.ca/youtubedata/080727.zip.

## Content ##

`0.txt`, `1.txt`, `2.txt`, `3.txt` - original crawling files that are downloaded from http://netsg.cs.sfu.ca/youtubedata/ storing the video data of depth 0, 1, 2 and 3 respectively.

`prefixed.txt` - video IDs from `3.txt` are replaced with the YouTube links.

`links.txt` - selected the first column from `prefixed.txt`.

`video_links.txt` - final list of YouTube video links to be indexed.

## Dependencies ##

`pip install langdetect`

## Instructions ##

Steps to obtain final list of valid YouTube video links from the original crawling file:

1. `awk '$0="https://www.youtube.com/watch?v="$0' 3.txt > prefixed.txt`
2. `awk -F '\t' '{print $1}' prefixed.txt > links.txt`
3. `python validate_links.py`

## Links validation script ##

The `validate_links.py` script validates YouTube video links. It takes optional arguments:

* `-i`, `--input`, `default='links.txt'` - Input file with YouTube video links
* `-o`, `--output`, `default='video_links.txt'` - Output file with valid and existing YouTube video links
* `-l`, `--limit`, `default=1000`, - Limit for the number of links in the output file
