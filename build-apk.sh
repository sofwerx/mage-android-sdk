#!/bin/bash

ANDROIDSDKPATH="$HOME/android-sdk"

pushd .
cd `dirname $0`

# build the *properties files for ant
$ANDROIDSDKPATH/tools/android update project --name MDK --target android-19 -p .

# build the project
ant clean release

popd