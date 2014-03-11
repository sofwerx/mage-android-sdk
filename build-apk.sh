#!/bin/bash

ANDROIDSDKPATH="$HOME/android-sdk"

# build the *properties files for ant
$ANDROIDSDKPATH/tools/android update project --name MDK --target android-18 -p .

# build the project
ant clean release