#!/bin/bash

year=2023

cd "$(dirname "$0")"

if [ ! -f "session.txt" ]; then
    echo "Save your session ID into a file called 'session.txt' in the 'input' folder"
    echo "before using this script."
    echo ""
    echo "To get your session ID, inspect a request to https://adventofcode.com/ in your"
    echo "browser after logging in with your account, and find the 'session=...' part of"
    echo "the cookies among request headers."
    exit 1
fi

session=$(cat "session.txt")

if [ $(TZ=EST date "+%Y") -eq $year ] && [ $(TZ=EST date "+%d") -leq 25 ]; then
    max=$(TZ=EST date "+%d")
else
    max=25
fi

for d in $(seq 1 $max); do
    if [ $d -lt 10 ]; then
        file="day0$d.txt"
    else
        file="day$d.txt"
    fi
    if [ -f $file ]; then
        echo "$file: already exists"
    else
        curl -s -f -b "session=$session" "https://adventofcode.com/$year/day/$d/input" -o $file \
            && echo "$file: DOWNLOADED" \
            || echo "$file: ERROR"
    fi
done
