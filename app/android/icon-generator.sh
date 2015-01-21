#!/bin/bash
# Generate icons for all required sizes for Android launcher icons from the 512x512 Carat icon.

if [ -z "$1" ]
then
  echo "Usage: ./icon-generator.sh /path/to/icon/file.png"
  exit 1
fi

convert=$( which convert )
if [ -z ${convert} ]
then
  echo "This script requires imagemagick convert. Please install it, e.g. sudo apt-get install imagemagick"
  exit 1
fi

icon=$( basename "$1" )

sizes=(48 72 96 144 192)
names=(m h xh xxh xxxh)
suff="dpi"
pre="res/drawable"

last=${#names[*]}
let last--

for k in $( seq 0 $last )
do
  s=${sizes[k]}
  d="${pre}-${names[k]}${suff}"
  n="${d}/${icon}"
  if  [ ! -d "$d" ]; then mkdir -p "$d"; fi
  echo convert -scale "${s}x${s}" "$1" "$n"
  convert -scale "${s}x${s}" "$1" "$n"
done


