#!/usr/bin/env bash

docker run \
  --rm \
  --network=pixelcanvas_default \
  --name pinot-streaming-table-creation \
  -v $(pwd):/tmp/demo \
  apachepinot/pinot:latest AddTable \
  -schemaFile /tmp/demo/pinot/pixel_realtime_schema.json \
  -tableConfigFile /tmp/demo/pinot/pixel_realtime_table.json \
  -controllerHost pinot-controller \
  -controllerPort 9000 \
  -exec


docker run \
  --rm \
  --network=pixelcanvas_default \
  --name pinot-streaming-table-creation \
  -v $(pwd):/tmp/demo \
  apachepinot/pinot:latest AddTable \
  -schemaFile /tmp/demo/pinot/reddit_place_schema.json \
  -tableConfigFile /tmp/demo/pinot/reddit_place_table.json \
  -controllerHost pinot-controller \
  -controllerPort 9000 \
  -exec