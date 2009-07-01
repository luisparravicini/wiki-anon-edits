#!/usr/bin/ruby

require 'fileutils'

include FileUtils

files = Dir.glob('maps/thumbs/*.jpg')

# create links to the images in the order needed
links = []
files.sort.each_with_index do |fname, i|
  link_name = File.join(File.dirname(fname), "#{i}.jpg")
  ln_s File.expand_path(fname), link_name, :force => true

  links.push link_name
end

quality = ARGV.shift || 1

`ffmpeg -qscale #{quality} -i maps/thumbs/%d.jpg wiki-edits.mpeg`

links.each { |link| File.delete link }
