from pytube import YouTube
import os
import json
from vidinfos import grabURLs
import re
import subprocess

videos = {}
YOUTUBELINK = "https://www.youtube.com/watch?v="
MODEL = "googlenet_places"
VIDEO_INDEX_PATH = "video_index"
VIDEO_RESOLUTION = 0  # 0 -> Lowest available resolution, -1 -> Highest available resolution
SAMPLING_RATE = 0.01  # frames in the video for each sample. 1 = 1 sample per second


# The bash script ended up being kinda pointless in the end. Doesn't really do much for us at this moment.
# Will probably change this later.
def index_video(id):
    link = YOUTUBELINK + id
    video_path = download(link) + ".mp4"
    subprocess.call("./tag_video.sh -p {} -s {}".format(video_path, SAMPLING_RATE), shell=True)


def download(link, path="video_index/"):
    yt = YouTube(link)
    file_name = re.sub(r'\W+', '',
                       yt.filename.replace(" ", "_"))  # Ugly ass solution to fix encoding errors later on...
    yt.set_filename(file_name)
    video_path = os.path.join(path, yt.filename)

    print "Downloading video: " + yt.filename
    # Below downloads the video in the lowest resolution available.
    # Change the '0' to '-1' to get the highest instead
    video = yt.filter('mp4')[VIDEO_RESOLUTION]
    video.download(path)
    return video_path


def parse_json(path="video_infos.json"):
    if not os.path.isfile(VIDEO_INDEX_PATH + "_" + MODEL + ".json"):
        with open(path) as data_file:
            data = json.load(data_file)
        for video_json in data["items"]:
            id = video_json["id"]
            index_video(id)


grabURLs()
parse_json()
