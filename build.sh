#!/bin/bash

./gradlew build
OUT="./build/libs/"
FILENAME="RushmoreT.jar"

pushd  $OUT
	unzip RushmoreTweaks.jar
	rm RushmoreTweaks*
popd

cp ./lib/* $OUT

pushd $OUT
	zip $FILENAME *
popd

cp $OUT$FILENAME .


