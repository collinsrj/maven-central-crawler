#!/bin/sh
for f in $1
do
	pom=`echo $f | sed 's/.\{4\}$//'`
	# gpg --keyserver eu-pool.sks-keyservers.net --verify "$f" "$pom" 
	gpg --verify "$f" "$pom" 
	if [ $? -ne 0 ] 
		then
    	echo "Signature did not verify for $f"
	fi
done
