DECLARE @endDate BIGINT;
SET @endDate = 9810311200;
DECLARE @startDate BIGINT;
SET @startDate = 9810010000;

declare @counter smallint;
set @counter = 0;

while @counter < 30 Begin
    insert into graph.dbo.cap
    SELECT allTimes.F4602
         , allTimes.f4607
         , allTimes.F2201
         , @counter
        /*all wagon location records*/
    FROM (SELECT s.F4601
               , s.f4602
               , s.f4607
               , f.F2201
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
            ) AS EnterTime
          FROM (SELECT F4601
                     , F4602
                     , f4607
                     , f2205
                     , f2206
                     , t.F15Rec_ID
                FROM graph.dbo.F22 AS x
                         INNER JOIN (SELECT F4601
                                          , F4602
                                          , F4607
                                          , f4610
                                          , F46.F15Rec_ID
                                          , F1506
                                     FROM graph.dbo.F46
                                              INNER JOIN graph.dbo.F15 ON F46.F15Rec_ID = F15.Rec_Id) AS t
                                    ON t.F15Rec_ID = x.F15Rec_ID AND (x.f2201 = t.F4610 OR t.F1506 <> x.f2201)
                WHERE CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) < @endDate
                  AND CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) > @startDate) AS s
                   INNER JOIN graph.dbo.F22 AS f
                              ON s.F15Rec_ID = f.F15Rec_ID AND f.F2205 <= s.F2205 AND f.F2206 <= s.F2206
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
                         FROM (SELECT F4601
                                    , F4602
                                    , F4610
                                    , f2205
                                    , f2206
                                    , t.F15Rec_ID
                               FROM graph.dbo.F22 AS x
                                        INNER JOIN (SELECT F4601
                                                         , F4602
                                                         , F4610
                                                         , F46.F15Rec_ID
                                                         , F1506
                                                    FROM graph.dbo.F46
                                                             INNER JOIN graph.dbo.F15 ON F46.F15Rec_ID = F15.Rec_Id) AS t
                                                   ON t.F15Rec_ID = x.F15Rec_ID AND
                                                      (x.f2201 = t.F4610 OR t.F1506 <> x.f2201)
                               WHERE CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) < @endDate
                                 AND CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) >
                                     @startDate) AS d
                                  INNER JOIN graph.dbo.F22 AS f
                                             ON d.F15Rec_ID = f.F15Rec_ID AND f.F2205 <= d.F2205 AND f.F2206 <= d.F2206
                         GROUP BY d.F4602) AS maxs
                        ON maxs.F4602 = allTimes.F4602 AND maxs.maxEnterTime = allTimes.EnterTime

    WHERE (allTimes.f4601 = 10
        OR allTimes.f4601 = 24
        OR allTimes.f4601 = 30);
    set @startDate = @startDate - 10000;
    set @endDate = @endDate - 10000;
    set @counter = @counter + 1;
end;