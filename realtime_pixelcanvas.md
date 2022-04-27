# Realtime pixel canvas

To demonstrate Pinot realtime and hybrid capacity this project provide a Reddit place like application.
This application is based on a Command server to receive some pixel update, a Request server with a sample html page to stream the modification in a canvas
and a Bot project that will send some pixel art to the system or generate random update at full speed.

## Preparation

Create the table for realtime ingestion :
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

Create the table for offline ingestion :
```
docker run \
    --rm \
    --network=pixelcanvas_default \
    --name pinot-streaming-hybrid-loading \
    -v (pwd):/tmp/demo \
    apachepinot/pinot:latest AddTable \
    -schemaFile /tmp/demo/pinot/pixel_realtime_schema.json \
    -tableConfigFile /tmp/demo/pinot/pixel_offline_table.json \
    -controllerHost pinot-controller \
    -controllerPort 9000 \
    -exec
```

:warning: This part is not yet runnable through cli only and is ATM ran through an IDE.

 - Start the command server (`Main.kt` in `command` project)
 - Start the request server (`Main.kt` in `request` project)


Open http://localhost:8081/index.html and click on the Start button.
You should see connection to kafka from request logs

## Demo

## Pixel art

Now start the pixel art bot (`PixelArt.kt` in `bot` project) to send some images to be displayed.
They should be displayed on your front end

## Bot

Now start the pixel art bot (`Bot.kt` in `bot` project) to send some random pixel update with "high throughput"

## Query

While the bots are running your can open pinot controller ui to do some request in realtime.

Most active player :
```sql
select user, count(*) as counter from pixelEvent group by user order by counter
```

Color sent by player :
```sql
select user, color, count(*) as counter from pixelEvent group by user, color order by counter
```

Get last update for each coordinate
```sql
select x, y, ToDateTime(max("time"), 'yyyy-MM-dd HH:mm:ss.SSS'), LASTWITHTIME(user, "time", 'STRING'), LASTWITHTIME(color, "time", 'STRING')
from pixelEvent
group by x, y
order by x, y
limit 250
```

## Application integration

Open http://localhost:8081/users to see the request for the most active player exposed through the request application