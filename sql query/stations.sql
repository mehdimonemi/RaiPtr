SELECT p2.[code] AS code
     , p2.[desc] AS NAME
     , nahi
INTO Traffic.dbo.stations
FROM graph.dbo.P2
WHERE Is_Active = 1
  AND nahi > 1
  AND (Position_Map = 0 OR (Position_Map = 1 AND tagend = 1))
  And Not (CODE > 758 and CODE < 765)
  AND Not (code=135 and nahi=9)
  AND Not (code=135 and nahi=13)
  AND Not (code=767 and [DESC]=N'پالايشگاه بندرعباس')
GROUP BY nahi
       , code
       , [desc]