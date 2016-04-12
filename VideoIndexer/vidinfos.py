import re
import urllib2
import cgi

# (id, title, description, ?caregory?)
# (views, likes, dislikes, favorites)
# [?bad? caption?] ?TODO?

# input (List): [url1,url2,url3]
# output (JSON):
# {items: [{id1,snippet1: {title, ...},statistics1: {viewCount, ...}},{id2,...}]}
def grabURLs(path="video_links.txt", output_path="video_infos.json"):
    with open(path) as urls_file:
        urls = urls_file.read().split("\n")
    request = "https://www.googleapis.com/youtube/v3/videos?part={}&fields={}&id={}&key={}"
    part = "snippet,statistics"
    fields = "items(id,snippet(title,description,categoryId),statistics)"
    ids = ""
    for u in urls:
        buff = re.search('(?<=v=)\w+', u)
        if buff:
            ids += buff.group(0) + ","
    json_result = urllib2.urlopen(request.format(part, fields, ids, open('key', 'r').read()))
    with open(output_path, "w") as outfile:
        outfile.write(json_result.read())
    return json_result


