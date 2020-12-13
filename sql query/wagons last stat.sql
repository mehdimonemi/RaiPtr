DECLARE @endDate BIGINT;
SET @endDate = 9810311200;
DECLARE @startDate BIGINT;
SET @startDate = 9810010000;
SELECT allTimes.F4602                               AS fleetId
     , allTimes.f4601                               AS fleetKind
     , allTimes.f4607                               AS frieght
     , allTimes.f4612                               AS Destination
     , allTimes.F2201                               AS lastStation
     , allTimes.f4610                               AS detachStation
     , allTimes.F1506                               AS trainDestination
     , allTimes.STATUS                              AS STATUS
     , allTimes.F15Rec_ID                           AS trainRecId
     , ISNULL(LTRIM(RTRIM(allTimes.F2205)), '')     AS lastStationEnterYear
     , ISNULL(LTRIM(RTRIM(allTimes.F2206)), '')     AS lastStationEnterTime
     , ISNULL(LTRIM(RTRIM(allTimes.F2207)), '')     AS lastStationExitYear
     , ISNULL(LTRIM(RTRIM(allTimes.F2208)), '')     AS lastStationExitTime
     , maxs.maxEnterTime                            AS lastTimeCalculate
     , ISNULL(LTRIM(RTRIM(allTimes.F1502)), '')     AS trainFormationYear
     , ISNULL(LTRIM(RTRIM(allTimes.F1503)), '')     AS trainFormationTime
     , ISNULL(wagonTyps.Wagon_Type, allTimes.f4601) AS wagonType
     , ISNULL(wagonTyps.WagonLength, 16)            AS WagonLength
     , ISNULL(wagonTyps.vazn_wagon_khali, 20)       AS emptyWeight
     , ISNULL(wagonTyps.vazn_wagon_bardar, 70)      AS FullWeight
-- INTO Traffic.dbo.wagon_last_stat
/*all wagon location records*/
FROM (SELECT s.F4602
           , s.f4601
           , s.f4610
           , s.f4607
           , s.f4612
           , s.F15Rec_ID
           , f.F2201
           , f.F2205
           , f.F2206
           , f.F2207
           , f.F2208
           , CASE
                 WHEN f.F2201 = s.f4612 THEN 0/*reach destination*/
        /*some stations are actually as one*/
                 WHEN s.f4612 = 381 AND f.F2201 = 408 THEN 0
                 WHEN s.f4612 = 384 AND f.F2201 = 411 THEN 0
                 WHEN s.f4612 = 414 AND
                      (f.F2201 = 415 OR f.F2201 = 416 OR f.F2201 = 769 OR f.F2201 = 768 OR f.F2201 = 767
                          OR f.F2201 = 642) THEN 0
                 WHEN s.f4612 = 352 AND f.F2201 = 605 THEN 0
                 WHEN (s.f4612 = 499 OR s.f4612 = 708) AND f.F2201 = 604 THEN 0
                 WHEN s.f4612 = 602 AND (f.F2201 = 750 OR f.F2201 = 603) THEN 0
                 WHEN s.f4612 = 116 AND f.F2201 = 465 THEN 0
                 WHEN s.f4612 = 424 AND f.F2201 = 425 THEN 0
                 WHEN s.f4612 = 625 AND f.F2201 = 636 THEN 0
                 WHEN s.f4612 = 165 AND f.F2201 = 474 THEN 0
                 WHEN s.f4612 = 273 AND f.F2201 = 388 THEN 0
                 WHEN s.f4612 = 298 AND f.F2201 = 426 THEN 0
                 WHEN s.f4612 = 393 AND f.F2201 = 392 THEN 0
                 WHEN s.f4612 = 341 AND f.F2201 = 391 THEN 0
                 WHEN s.f4612 = 363 AND f.F2201 = 390 THEN 0
                 WHEN s.f4612 = 352 AND f.F2201 = 605 THEN 0
                 WHEN s.f4612 = 459 AND (f.F2201 = 390 OR f.F2201 = 605) THEN 0
                 WHEN s.f4612 = 585 AND f.F2201 = 635 THEN 0
                 WHEN s.f4612 = 586 AND f.F2201 = 634 THEN 0
                 WHEN s.f4612 = 580 AND f.F2201 = 586 THEN 0
                 WHEN f.F2201 = F15.F1506 THEN 1/*reach train destination*/
                 WHEN f.F2201 = s.f4610 THEN 2/*reach detach Station*/
                 ELSE 3/*Moving*/
        END AS STATUS
           , (((CONVERT(INT, SUBSTRING(f.F2205, 1, 2)) - 98) * 365 * 24 * 60 + (
        CASE
            WHEN CONVERT(INT, SUBSTRING(f.F2205, 3, 2)) <= 6 THEN
                (CONVERT(INT, SUBSTRING(f.F2205, 3, 2)) - 1) * 31
            WHEN CONVERT(INT, SUBSTRING(f.F2205, 3, 2)) = 12 THEN 6 * 31 + 5 * 30
            ELSE 6 * 31 + (CONVERT(INT, SUBSTRING(f.F2205, 3, 2)) - 7) * 30
            END
        ) + (CONVERT(INT, SUBSTRING(f.F2205, 5, 2)) - 1)) * 24 * 60 +
              CONVERT(INT, SUBSTRING(f.F2206, 1, 2)) * 60 +
              CONVERT(INT, SUBSTRING(f.F2206, 3, 2))
        )   AS EnterTime
           , F15.F1506
           , F15.F1502
           , F15.F1503
      FROM (SELECT F4602
                 , F4610
                 , F4601
                 , f4607
                 , f4612
                 , f2205
                 , f2206
                 , t.F15Rec_ID
            FROM graph.dbo.F22 AS x
                     INNER JOIN (SELECT F4602
                                      , F4610
                                      , F4601
                                      , f4607
                                      , f4612
                                      , F46.F15Rec_ID
                                      , F1506
                                 FROM graph.dbo.F46
                                          INNER JOIN graph.dbo.F15 ON F46.F15Rec_ID = F15.Rec_Id) AS t
                                ON t.F15Rec_ID = x.F15Rec_ID AND (x.f2201 = t.F4610 OR t.F1506 <> x.f2201)
            WHERE CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) < @endDate
              AND CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) > @startDate) AS s
               INNER JOIN graph.dbo.F22 AS f ON s.F15Rec_ID = f.F15Rec_ID AND f.F2205 <= s.F2205 AND f.F2206 <= s.F2206
               INNER JOIN graph.dbo.F15 ON s.F15Rec_ID = F15.Rec_Id) AS allTimes
         /*find the latest recorded wagon location*/
         INNER JOIN (SELECT d.F4602,
                            MAX(((CONVERT(INT, SUBSTRING(d.F2205, 1, 2)) - 98) * 365 * 24 * 60 + (
                                CASE
                                    WHEN CONVERT(INT, SUBSTRING(d.F2205, 3, 2)) <= 6 THEN
                                        (CONVERT(INT, SUBSTRING(d.F2205, 3, 2)) - 1) * 31
                                    WHEN CONVERT(INT, SUBSTRING(d.F2205, 3, 2)) = 12
                                        THEN 6 * 31 + 5 * 30
                                    ELSE 6 * 31 + (CONVERT(INT, SUBSTRING(d.F2205, 3, 2)) - 7) * 30
                                    END
                                ) + (CONVERT(INT, SUBSTRING(d.F2205, 5, 2)) - 1)) * 24 * 60 +
                                CONVERT(INT, SUBSTRING(d.F2206, 1, 2)) * 60 +
                                CONVERT(INT, SUBSTRING(d.F2206, 3, 2))) AS maxEnterTime
                     FROM (SELECT F4602
                                , F4610
                                , F4601
                                , f2205
                                , f2206
                                , t.F15Rec_ID
                           FROM graph.dbo.F22 AS x
                                    INNER JOIN (SELECT F4602
                                                     , F4610
                                                     , F4601
                                                     , F46.F15Rec_ID
                                                     , F1506
                                                FROM graph.dbo.F46
                                                         INNER JOIN graph.dbo.F15 ON F46.F15Rec_ID = F15.Rec_Id) AS t
                                               ON t.F15Rec_ID = x.F15Rec_ID AND
                                                  (x.f2201 = t.F4610 OR t.F1506 <> x.f2201)
                           WHERE CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) < @endDate
                             AND CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) > @startDate) AS d
                              INNER JOIN graph.dbo.F22 AS f
                                         ON d.F15Rec_ID = f.F15Rec_ID AND f.F2205 <= d.F2205 AND f.F2206 <= d.F2206
                     GROUP BY d.F4602) AS maxs ON maxs.F4602 = allTimes.F4602 AND maxs.maxEnterTime = allTimes.EnterTime
         LEFT JOIN (SELECT Mms_Fld_pk_SerialNo
                         , Wagon_Type
                         , WagonLength
                         , vazn_wagon_khali
                         , vazn_wagon_bardar
                    FROM graph.dbo.Wagon_History
                    WHERE ToDate = 0
                       OR ToDate IS NULL) AS wagonTyps
                   ON CAST(allTimes.F4602 / 10 AS BIGINT) = wagonTyps.Mms_Fld_pk_SerialNo
WHERE (allTimes.f4601 = 10 OR allTimes.f4601 = 24 OR allTimes.f4601 = 30)