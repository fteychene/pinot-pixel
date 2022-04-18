#!/bin/env bash

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)
origin=$(pwd )

cd "$script_dir"
for i in {00..73}
do
    wget https://placedata.reddit.com/data/canvas-history/2022_place_canvas_history-0000000000$i.csv.gzip
    gzip -dc 2022_place_canvas_history-0000000000$i.csv.gzip > dataset_$i.csv
done;
cd "$origin"
