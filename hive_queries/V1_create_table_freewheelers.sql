create external table test_tableau
(
    bikes_available INT,
    docks_available INT,
    is_renting BOOLEAN,
    is_returning BOOLEAN,
    last_updated timestamp,
    station_id STRING,
    name STRING,
    latitude DOUBLE,
    longitude DOUBLE
)
    row format delimited
        fields terminated by ','
    location '/free2wheelers/stationMart/data/'
    tblproperties ("skip.header.line.count"="1");
ALTER TABLE test_tableau SET SERDEPROPERTIES ("timestamp.formats"="yyyy-MM-dd'T'HH:mm:ss");
