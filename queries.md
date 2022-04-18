# Pinot Queries

## Reddit place

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

## Realtime pixel canvas

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