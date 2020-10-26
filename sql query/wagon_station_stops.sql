DECLARE @startDate INTEGER;
DECLARE @endDate INTEGER;
SET @endDate = 981032;
SET @startDate = 981000;
SELECT f4602                  AS wagonID
     , ISNULL(Wagon_Type, 24) AS Wagon_Type
     , F4607                  AS fright
     , (
            (
                    (CONVERT(INT, SUBSTRING(F4604, 1, 2)) - 98) * 365 * 24 * 60 + (
                    CASE
                        WHEN CONVERT(INT, SUBSTRING(F4604, 3, 2)) <= 6 THEN (CONVERT(INT, SUBSTRING(F4604, 3, 2)) - 1) *
                                                                            31
                        WHEN CONVERT(INT, SUBSTRING(F4604, 3, 2)) = 12 THEN 6 * 31 + 5 * 30
                        ELSE 6 * 31 + (CONVERT(INT, SUBSTRING(F4604, 3, 2)) - 7) * 30
                        END
                    ) + (CONVERT(INT, SUBSTRING(F4604, 5, 2)) - 1)
                ) * 24 * 60 +
            CONVERT(INT, SUBSTRING(F4605, 1, 2)) * 60 +
            CONVERT(INT, SUBSTRING(F4605, 3, 2))
    )                         AS formationTime
     , (
            (
                    (CONVERT(INT, SUBSTRING(F2205, 1, 2)) - 98) * 365 * 24 * 60 + (
                    CASE
                        WHEN CONVERT(INT, SUBSTRING(F2205, 3, 2)) <= 6 THEN (CONVERT(INT, SUBSTRING(F2205, 3, 2)) - 1) *
                                                                            31
                        WHEN CONVERT(INT, SUBSTRING(F2205, 3, 2)) = 12 THEN 6 * 31 + 5 * 30
                        ELSE 6 * 31 + (CONVERT(INT, SUBSTRING(F2205, 3, 2)) - 7) * 30
                        END
                    ) + (CONVERT(INT, SUBSTRING(F2205, 5, 2)) - 1)
                ) * 24 * 60 +
            CONVERT(INT, SUBSTRING(F2206, 1, 2)) * 60 +
            CONVERT(INT, SUBSTRING(F2206, 3, 2))
    )                         AS EnterTime
     , (
            (
                    (CONVERT(INT, SUBSTRING(F2207, 1, 2)) - 98) * 365 * 24 * 60 + (
                    CASE
                        WHEN CONVERT(INT, SUBSTRING(F2207, 3, 2)) <= 6 THEN (CONVERT(INT, SUBSTRING(F2207, 3, 2)) - 1) *
                                                                            31
                        WHEN CONVERT(INT, SUBSTRING(F2207, 3, 2)) = 12 THEN 6 * 31 + 5 * 30
                        ELSE 6 * 31 + (CONVERT(INT, SUBSTRING(F2207, 3, 2)) - 7) * 30
                        END
                    ) + (CONVERT(INT, SUBSTRING(F2207, 5, 2)) - 1)
                ) * 24 * 60 +
            CONVERT(INT, SUBSTRING(F2208, 1, 2)) * 60 +
            CONVERT(INT, SUBSTRING(F2208, 3, 2))
    )                         AS ExitTime
     , F46.F15Rec_ID
     , code                   AS stationID
     , stations.name          AS StationName
     , f4611                  AS origin
     , f4612                  AS destination
     , F2211                  AS enterBlock
INTO Traffic.dbo.wagon_station_stop
FROM graph.dbo.F46
         /*connect to trains table*/
         RIGHT JOIN graph.dbo.F22 ON F46.F15Rec_ID = F22.F15Rec_ID
    /*connect to stations*/
         LEFT JOIN (
    SELECT p2.[code] AS code
         , p2.[desc] AS NAME
         , nahi
    FROM graph.dbo.P2
    WHERE Is_Active = 1
      AND nahi > 1
      AND (Position_Map = 0 OR (Position_Map = 1 AND tagend = 1))
      And Not (CODE > 758 and CODE < 765)
      AND Not (code = 135 and nahi = 9)
      AND Not (code = 135 and nahi = 13)
      AND Not (code = 767 and [DESC] = N'پالايشگاه بندرعباس')
    GROUP BY nahi, code, [desc]
) AS stations ON stations.code = F22.F2201
    /*connect to acceptable wagon*/
         LEFT JOIN (
    SELECT Mms_Fld_pk_SerialNo
         , Wagon_Type
    FROM graph.dbo.Wagon_History
    WHERE ToDate = 0
       OR ToDate IS NULL
) AS wagonTyps
                   ON CAST(F4602 / 10 AS INT) = Mms_Fld_pk_SerialNo
WHERE F4601 = 10
  AND (F46.f4604 < @endDate AND F46.f4604 > @startDate)
ORDER BY formationTime
       , EnterTime;