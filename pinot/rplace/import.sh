#!/bin/env bash

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)

for i in {00..73}
do
    echo "Import dataset_sanitize_$i.csv"
    curl -X POST -F file=@${script_dir}/dataset_sanitize_$i.csv \
              -H "Content-Type: multipart/form-data" \
              "http://localhost:9000/ingestFromFile?tableNameWithType=redditPlace_OFFLINE&batchConfigMapStr=%7B%22inputFormat%22%3A%22csv%22%2C%20%22multiValueDelimiter%22%3A%20%22%7C%22%7D"
    echo
done;

