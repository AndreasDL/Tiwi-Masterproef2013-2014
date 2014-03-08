#!/bin/bash

#scale down using imagemagick

for f30 in icons30/*.png
do f15="icons15/$( basename $f30 )"
    convert $f30  -resize 15x15 $f15
done

