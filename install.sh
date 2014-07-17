#! /bin/sh

if [ -z "$*" ]; then
	ant -f build.xml
	echo "Please supply the FULL path to sblock"
else
	ant -f build.xml
	cp dist/Sblock.jar $1/plugins/
	mkdir $1/plugins/lib
	cp lib/j* $1/plugins/lib/
	cp lib/gson* $1/plugins/lib/gson.jar
fi