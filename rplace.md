# Reddit place 2022 dataset

Ingest the dataset of 2022 Reddit [r/place](https://www.reddit.com/r/place/comments/txvk2d/rplace_datasets_april_fools_2022/)

Place is a social event that allow users of Reddit to update a pixel in a canvas every 5 min (20 min if your account is not verified).
These events are social experiment to create participate art but also interaction between communities.

This dataset is big enough to be challenging but not too big to need a real cluster to work on.
Count roughly 40 min to ingest all the datas once your downloaded and sanitize them.

## Preparation step

First we need to download the datasets from reddit. A script is already available to do that.
`./pinot/rplace/download.sh`

This will download all 73 datasets from reddit and decompress them

After we need to sanitize the dataset, since the csv export create various timestamp format we just want to align.
A project is available to sanitize those datasets, you can run it with :
`./gradlew sanitizer:run`


## Demo

### Table creation

Create the table in Pinot with the command
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

### Ingestion

You can now import your datasets using the pinot API. A script is already available to ingest all the datasets
`./pinot/rplace/import.sh`

:warning: This step took around 40 min on my laptop

### Query

Now that we have our dataset with 150M rows available we can do some queries on it.
Feel free to enrich thoses request with your own or event optimise indexing, this demo is for introduction purpose only and is not optimized at all


Number of pixel changed in French flag disputed area
```sql
select count(*) from redditPlace where x > 0 and x <= 250 and y >= 1470 and y <= 1970
```

Number of changes by hours
```sql
select
    ToDateTime("time", 'yyyy-MM-dd') as day,
    hour("time") as hour,
    count(*) as counter
from redditPlace
where x > 0
    and x <= 250
    and y >= 1470
    and y <= 1970
group by day, hour
order by counter desc
```

Most active user of French flag disputed area
```sql
select user_id, count(*) as updated
from redditPlace
where x > 0 and x <= 250 and y >= 1470 and y <= 1970
group by user_id
order by updated desc
```

Most played color
```
select pixel_color, count(*) as counter from redditPlace group by pixel_color order by counter desc limit 10
```

Most active user overall
```sql
select user_id, count(*) as updated
from redditPlace
group by user_id
order by updated desc
```

Get last update for each coordinate
```sql
select x, y, ToDateTime(max("time"), 'yyyy-MM-dd HH:mm:ss.SSS'), LASTWITHTIME(user_id, "time", 'STRING'), LASTWITHTIME(pixel_color, "time", 'STRING')
from redditPlace
where x > 0 and x <= 250 and y >= 1470 and y <= 1970
group by x, y
order by x, y
limit 10
```