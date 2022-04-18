#!/bin/env bash

script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" &>/dev/null && pwd -P)

for i in {00..43}
do
    echo "Import pixel_updated_$i.json"
    curl -X POST -F file=@${script_dir}/pixel_updated_$i.json \
              -H "Content-Type: multipart/form-data" \
              "http://localhost:9000/ingestFromFile?tableNameWithType=pixelEvent_OFFLINE&batchConfigMapStr=%7B%22inputFormat%22%3A%22json%22%7D"
    echo
done;

