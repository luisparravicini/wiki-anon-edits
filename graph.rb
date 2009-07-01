#!/usr/bin/ruby

require 'fileutils'
require 'rubygems'
require 'RMagick'
require 'geoip_city'

include Magick

IMG = 'images'

$stdout.sync = true

def parse_date(s)
  return if s.nil?

  s = s.strip
  Date.parse(s) if s =~ /^\d{8}$/
end

def image(name)
  File.join(IMG, name)
end

$blur = false  # blur image?

#max = `./max_count e4`.to_i
max=1000

$world = Image.read(image('world.gif')).first

$width = 8192
$height = $width

$gradient = Image.read(image('gradient.jpg')).first

$draw = Draw.new
$draw.font_family = 'arial'
$draw.pointsize = 16
$draw.fill = 'white'
$text_height = $draw.get_type_metrics('height').height

def map_name(date)
  File.join('maps', "#{date.to_s}.png")
end

def gradient(map)
  list = ImageList.new
  list << map
  list << $gradient

  list.fx('v.p{0,u*v.h}')
end

def save_thumb(map, date, fname)
  base = File.basename(fname, '.png')
  thumb = File.join(File.dirname(fname), 'thumbs', "#{base}.jpg")

  d = File.dirname(thumb)
  FileUtils.mkdir d unless File.exist?(d)

  map = map.thumbnail(0.1).negate(false)
  map.crop!(0, 0, map.columns, $world.rows)
  map = gradient(map)
  map = map.blend($world, 0.5, 1, 25, 110).crop!(0, 100, map.columns, map.rows)
  map = map.blur_image if $blur
  $draw.annotate(map, 0, 0, 10, $world.rows - $text_height - 2, date)
  map.write(thumb)
end

def save(map, date)
  fname = map_name(date)
  dir = File.dirname(fname)
  FileUtils.mkdir_p(dir) unless File.exist?(dir)
#  tmp = "#{fname}_tmp.png"
#  map.write(tmp) { self.quality = 95 }
#  FileUtils.mv tmp, fname
  save_thumb(map, date, fname)
end

def map_exist?(date)
  File.exist?(map_name(date))
end

class Float
  def deg2rad
    Math::PI * self / 180
  end
end

def project(lat, lng)
  lat = lat.to_f.deg2rad
  lng = lng.to_f.deg2rad

  radius = $width/(2*Math::PI)

  x = lng * radius
  x += $width/2

  sin = Math.sin(lat)
  y = radius/2 * Math.log( (1 + sin) / (1 - sin) )
  y = -y + $height/2

  [x, y]
end

def blank_map(map=nil)
  if map.nil?
    Image.new($width,$height) { self.background_color = 'white' }
  else
    map.color_reset!('white')
  end
end



#p [$width, $height]
#p project(85,180)
#p project(0,0)
#p project(-85,-180)
#exit

`convert #{image('bolilla.png')} -fill white -colorize #{(100/max).round} bol.png`
dot = ImageList.new('bol.png')
map = blank_map

geoip = GeoIPCity::Database.new('GeoLiteCity.dat')

days = Dir.glob('data/**/*').sort

days.each do |day|
  next if File.directory?(day)

  date = day.split('/')[-3..-1].join
  next if map_exist?(date)

#next unless date =~ /^2008/

  edits = edits_found = 0
  print date
  File.open(day) do |f|
    while (ip = f.read(4))
      raise "corrupt file #{day}" if ip.size != 4

      edits += 1
      ip = ip.unpack('C*').join('.')

      place = geoip.look_up(ip)
      next if place.nil?
      lat = place[:latitude]
      lng = place[:longitude]

      x, y = project(lat, lng)

      map.composite!(dot, x, y, MultiplyCompositeOp)

      edits_found += 1
    end
  end

  save(map, date)
  blank_map(map)

  puts "\t#{edits} edits (#{edits_found} found)"
end
