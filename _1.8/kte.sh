#!/bin/bash

clear
echo "\e[36m  _  __ _____  _____ \n / |/ //__ __\/  __/ \n |   /   / \  |  \ \n |   \   | |  |  /_ \n \_|\_\  \_/  \____\ \n \e[0m"

echo "\e[36m Downloading server \e[0m"
wget https://share.alkanife.fr/kte/kte.zip >/dev/null 2>&1
echo "\e[36m Done, setting up \e[0m"

unzip -qq kte.zip

rm kte.zip

mv kte server/kte

echo "\e[36m Ready\n Starting server\n -----------------------------------\n \e[0m"

cd server/

java -Xms2G -Xmx2G -jar taco.jar

echo "\e[36m \n \n -----------------------------------\n Server down, cleaning folders \e[0m"

cd ../

mv server/logs/latest.log latest.log

rm -R server/
echo "\e[36m Done, bye! \n \e[0m"
