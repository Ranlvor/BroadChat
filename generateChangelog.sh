#!/bin/sh
if [ -z "$1" ]; then
	out="/dev/stdout"
else
	out="$1"
fi
git log --pretty=format:"%ad - %h - %s" --date=short --reverse > $out
echo >> $out