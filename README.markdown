Wikipedia edits
---------------

These scripts were used to generate a video showing anonymous edits on Wikipedia using the geo location of the ip addresses.

The video is at http://www.flickr.com/photos/xrm0/3654611676/ and the blog post with more info is at http://ktulu.com.ar/blog/2009/06/27/wikipedia-density-map/


Setup
-----

If you want to run the scripts you need a few things to setup first:

 - Download geoip city database from http://geolite.maxmind.com/download/geoip/database/GeoLiteCity.dat.gz and decompress it.

 - Download a Wikipedia database dump (I've used a pages-meta-history.xml.7z dump). See in http://en.wikipedia.org/wiki/Wikipedia_database where to get one. Leave the file compressed.

 - Run extract (you need Ant and a JDK to compile the dump reader). Errors are logged in error.log

 - When extract finishes, a 'data' directory will be created.

 - Run graph.rb (you need Ruby and the rmagick and geoip_city gems). The script will create an image for each day in directory 'maps'

 - Run video.rb (you need Ruby and ffmpeg) to create the video.


Copyright
---------
Copyright &copy; 2009 Luis Parravicini, under the GPL v2 license.
