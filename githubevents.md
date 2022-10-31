# Github merge events

_This is adapted based of the official documentation [recipe](https://docs.pinot.apache.org/basics/recipes/github-events-stream)__

Pinot already include a streaming source to be used for demo.
This source will call Github API regularly (10s) and send events to a kafka when detecting merged pull request.

To use this demo, you should create a [github personal token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token)

## Preparation step

Create the kafka topic
```
docker exec \
        -t kafka \
        /bin/kafka-topics \
        --bootstrap-server kafka:9092 \
        --partitions=1 --replication-factor=1 \
        --create --topic pullRequestMergedEvents
```

Now let's start a docker container to watch Github events (Please notice the `$GH_TOKEN` to configure your token by env var or changing the command line)
```
docker run --rm -ti \
    --network=pixelcanvas_default \
    --name pinot-github-events-into-kafka \
    -v (pwd):/tmp/demo \
    -d apachepinot/pinot:0.10.0 StreamGitHubEvents \
    -schemaFile /tmp/demo/pinot/gh_event_schema.json \
    -topic pullRequestMergedEvents \
    -personalAccessToken $GH_TOKEN \
    -kafkaBrokerList kafka:9092
```

If you look at the container logs you should see events pushed into kafka at some point when merge event are detected.

## Demo

### Table ingestion

Create the [table](pinot/gh_event_table.json) and [schema](pinot/gh_event_schema.json)
```
docker run \
    --rm \
    --network=pixelcanvas_default \
    --name pinot-streaming-table-creation \
    -v (pwd):/tmp/demo \
    apachepinot/pinot:0.10.0 AddTable \
    -schemaFile /tmp/demo/pinot/gh_event_schema.json \
    -tableConfigFile /tmp/demo/pinot/gh_event_table.json \
    -controllerHost pinot-controller \
    -controllerPort 9000 \
    -exec
```

### Queries

Now you can access to you [controller ui](http://localhost:9000) to query the `pullRequestMergedEvents` table.

An example of query could be
```sql
SELECT organization, count(*) as merged, SUM(numLinesAdded), SUM(numLinesDeleted), SUM(numFilesChanged)
FROM pullRequestMergedEvents
GROUP BY organization
ORDER BY merged DESC
```

How many events are we in ?
```sql
select count(*) from pullRequestMergedEvents limit 10
```