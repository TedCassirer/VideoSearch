import re
import urllib2

# (id, title, description, ?caregory?)
# (views, likes, dislikes, favorites)
# [?bad? caption?] ?TODO?

#input (List): [url1,url2,url3]
#output (JSON): 
# {items: [{id1,snippet1: {title, ...},statistics1: {viewCount, ...}},{id2,...}]}
def grabURLs(urls):
	request = "https://www.googleapis.com/youtube/v3/videos?part={}&fields={}&id={}&key={}"
	part = "snippet,statistics"
	fields = "items(id,snippet(title,description,categoryId),statistics)"
	ids = ""
	json_result = ""
	for u in urls:
		buffer = re.search('(?<=v=)\w+', u)
		ids += buffer.group(0) + ","
	response = urllib2.urlopen(request.format(part, fields,ids,open('key', 'r').read())).read()
	json_result += response + "\n"
	return json_result


#for testing; input = url1 url2 url3
input = raw_input("Enter URLs\n");
print grabURLs(input.split())