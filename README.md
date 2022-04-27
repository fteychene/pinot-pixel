# Pinot pixel canvas

Demo repo for presentation around Apache Pinot introduction.

## Common setup

Start Stack with docker-compose `docker-compose -p pixelcanvas up -d`

## Demos

### Github events

Realtime analytic of Github merge events

[Detailed instructions](githubevents.md)

### Reddit r/place dataset

Ingest the dataset of 2022 Reddit [r/place](https://www.reddit.com/r/place/comments/txvk2d/rplace_datasets_april_fools_2022/)

Place is a social event that allow users of Reddit to update a pixel in a canvas every 5 min (20 min if your account is not verified).
These events are social experiment to create participate art but also interaction between communities.

This dataset is big enough to be challenging but not too big to need a real cluster to work on.
Count roughly 40 min to ingest all the datas once your downloaded and sanitize them.

[Detailed instructions](rplace.md)

### Realtime & Hybrid pixel canvas

To demonstrate Pinot realtime and hybrid capacity this project provide a Reddit place like application.
This application is based on a Command server to receive some pixel update, a Request server with a sample html page to stream the modification in a canvas
and a Bot project that will send some pixel art to the system or generate random update at full speed.

[Detailed instructions](realtime_pixelcanvas.md)

## Misc

 - [Docker-compose stack](docker-compose.yaml)
 - [Pinot table configuration](pinot)
 - [Queries](queries.md)