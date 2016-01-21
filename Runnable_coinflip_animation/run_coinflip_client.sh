#!/bin/bash

param=""
# just change of the parameters name.
if [ "$1" = "SERVER" ]
then
	param="START"
fi

java -cp "animated_coinflip.jar:*" AnimatedCoinFlip "$param"
#java -cp "test.jar:*" AnimatedCoinFlip
