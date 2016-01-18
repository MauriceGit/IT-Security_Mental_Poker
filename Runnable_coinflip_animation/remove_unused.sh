#!/bin/bash

#files=$(find -name *.jar | grep amd64 | grep -v android | grep -v solaris | grep -v oculus | grep -v mobile)
#files=$(find -regex .*jogl-all-natives.*)
#files=$(ls jogamp-all-platforms/lib/linux-amd64/*)
#files=$(ls jogamp-all-platforms/jar/*.jar)
#files=$(ls jogamp-all-platforms/jar/atomic/*.jar)
files=$(ls *.jar | grep android)

for file in $files
do
    echo "$file"
    mv "$file" ../
done
