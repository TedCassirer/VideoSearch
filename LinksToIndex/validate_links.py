__author__ = 'mateusz'

import argparse
import random
import requests
from langdetect import detect
from pytube import YouTube

def main(input, output, limit):

	random.seed(42)

	baseurl = "https://www.youtube.com/oembed?format=json&url="
	validurls = []

	# get links to validate
	with open(input) as f:
		links = f.readlines()

	# shuffle to have videos from various categories
	random.shuffle(links)

	for link in links:
		try:
			# make request
			response = requests.get(baseurl + link)

			# url is valid and video exists if we got 200 in response
			if response.status_code == 200:
				data = response.json()
				title = data['title']

				isEnglish = detect(title) == 'en'
				yt = YouTube(link)
				canDownload = len(yt.filter('mp4')) > 0

				if isEnglish and canDownload:
					print title
					print link
					validurls.append(link.strip())

			# break if we reached given limit
			if len(validurls) >= limit:
				break

		except Exception as e:
			print e

	# write resulting list to the output file
	with open(output, mode="w+") as f:
		for vl in validurls:
			f.write("%s\n" % vl)


if __name__ == "__main__":

	parser = argparse.ArgumentParser()

	parser.add_argument('-i', '--input', type=str, default='links.txt',
						help='Input file with YouTube video links')
	parser.add_argument('-o', '--output', type=str, default='video_links.txt',
						help='Output file with valid and existing YouTube video links')
	parser.add_argument('-l', '--limit', type=int, default=1000,
						help='Limit for the number of links in the output file')

	args = parser.parse_args()
	params = vars(args)

	main(params['input'], params['output'], params['limit'])
