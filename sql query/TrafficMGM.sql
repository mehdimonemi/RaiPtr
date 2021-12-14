CREATE TABLE TrafficMGM.dbo.capacity
(
    ID       SMALLINT identity (1,1) PRIMARY KEY,
    station  SMALLINT,
    capacity SMALLINT
)

CREATE Table TrafficMGM.dbo.dizel_last_stat
(
    ID SMALLINT identity (1,1) PRIMARY KEY,
    fleetId smallint,
    fleetKind tinyint,
    destination smallint,
    lastStation smallint,
    detachStation smallint,
    trainDestination smallint,
    status tinyint,
    trainRecId mediumint,
    las

)