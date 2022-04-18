
## Pinot

### GH Events

```
docker exec \
        -t kafka \
        /bin/kafka-topics \
        --bootstrap-server kafka:9092 \
        --partitions=1 --replication-factor=1 \
        --create --topic pullRequestMergedEvents

docker run \
    --rm \
    --network=pixelcanvas_default \
    --name pinot-streaming-table-creation \
    -v (pwd):/tmp/gh \
    apachepinot/pinot:0.10.0 AddTable \
    -schemaFile /tmp/gh/schema.json \
    -tableConfigFile /tmp/gh/table.json \
    -controllerHost pinot-controller \
    -controllerPort 9000 \
    -exec

docker run --rm -ti \
    --network=pixelcanvas_default \
    --name pinot-github-events-into-kafka \
    -v (pwd):/tmp/gh \
    -d apachepinot/pinot:0.10.0 StreamGitHubEvents \
    -schemaFile /tmp/gh/schema.json \
    -topic pullRequestMergedEvents \
    -personalAccessToken $GH_TOKEN \
    -kafkaBrokerList kafka:9092
```

### R/Place

Configure Reddit r/place table and schema
```
docker run \
    --rm \
    --network=pixelcanvas_default \
    --name pinot-streaming-table-creation \
    -v (pwd):/tmp/demo \
    apachepinot/pinot:latest AddTable \
    -schemaFile /tmp/demo/pinot/reddit_place_schema.json \
    -tableConfigFile /tmp/demo/pinot/reddit_place_table.json \
    -controllerHost pinot-controller \
    -controllerPort 9000 \
    -exec
```

### PixelCanvas

Configure Pixel runtime
```
docker run \
    --rm \
    --network=pixelcanvas_default \
    --name pinot-streaming-table-creation \
    -v (pwd):/tmp/demo \
    apachepinot/pinot:latest AddTable \
    -schemaFile /tmp/demo/pinot/pixel_realtime_schema.json \
    -tableConfigFile /tmp/demo/pinot/pixel_realtime_table.json \
    -controllerHost pinot-controller \
    -controllerPort 9000 \
    -exec
```


Configure Pixel offline
```
docker run \
    --rm \
    --network=pixelcanvas_default \
    --name pinot-streaming-table-creation \
    -v (pwd):/tmp/demo \
    apachepinot/pinot:latest AddTable \
    -schemaFile /tmp/demo/pinot/pixel_realtime_schema.json \
    -tableConfigFile /tmp/demo/pinot/pixel_offline_table.json \
    -controllerHost pinot-controller \
    -controllerPort 9000 \
    -exec
```