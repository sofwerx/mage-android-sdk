#!/bin/bash

ANDROIDSDKPATH="$HOME/android-sdk"

pushd .
cd `dirname $0`

git checkout "./res/xml/overrides.xml"

if [ ! -z "$1" ] ; then
	configfile="./res/xml/$1.xml"
	if [ -e "$configfile" ] ; then
		cp "$configfile" "./res/xml/overrides.xml"
	fi
fi

# build the *properties files for ant
$ANDROIDSDKPATH/tools/android update project --name MDK --target android-19 -p .

# build the project
ant clean release

popd