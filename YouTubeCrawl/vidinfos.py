from __future__ import print_function
import re
import urllib2

# (id, title, description, ?caregory?)
# (views, likes, dislikes, favorites)
# [?bad? caption?] ?TODO?

#input (List): [url]
#output (JSON): 
# {items: [{id,snippet: {title, ...},statistics: {viewCount, ...}}]}
def grabURL(url):
	request = "https://www.googleapis.com/youtube/v3/videos?part={}&fields={}&id={}&key={}"
	part = "snippet,statistics"
	fields = "items(id,snippet(title,description,categoryId),statistics)"
	id = ""
	json_result = ""
	print(url)
	buffer = re.search('(?<=v=)\S*', url)
	id = buffer.group(0)
	print(id)
	response = urllib2.urlopen(request.format(part, fields,id,open('key', 'r').read())).read()
	log = open("grabs/"+id+"_m", "w+")
	print(response, file = log)
	log.close()

#for testing; input = url1 url2 url3
#input = raw_input("Enter URLs\n");
#grabURLs(input.split())
#with open("video_links.txt") as f:
#	grabURLs(f.readlines())

start_from = 0

try:
	with open('last_line.txt', 'r') as llf: start_from = int(llf.read())
except:
	pass

with open('video_links.txt') as file:
	for i, line in enumerate(file):
		if i < start_from: continue
		grabURL(line.rstrip('\n'))
		with open('last_line.txt', 'w') as outfile:
			outfile.write(str(i))