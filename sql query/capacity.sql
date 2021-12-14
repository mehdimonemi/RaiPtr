-- noinspection SqlInsertValuesForFile
DECLARE @endDate char(10);
SET @endDate = cast(dbo.GregorianToJalali(getdate(), 'yyMMdd') as char(10));
DECLARE @startDate char(10);
SET @startDate = cast(dbo.GregorianToJalali(getdate() - 1, 'yyMMdd') as char(10));

insert into Traffic.dbo.Cap
SELECT allTimes.F4602    --wagonID
     , allTimes.f4607    --FreightID
     , allTimes.F2201    --stationID
     , maxs.maxEnterTime --enterDate
     , @endDate          --reportingDate
    /*all wagon location records*/
FROM (SELECT s.F4601
           , s.f4602
           , s.f4607
           , f.F2201
           , dbo.seirDateToDatetime(f.F2205, f.F2206) AS EnterTime
      FROM (SELECT F4601
                 , F4602
                 , f4607
                 , f2205
                 , f2206
                 , t.F15Rec_ID
            FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F22 AS x
                     INNER JOIN (SELECT F4601
                                      , F4602
                                      , F4607
                                      , f4610
                                      , F46.F15Rec_ID
                                      , F1506
                                 FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F46
                                          INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F15
                                                     ON F46.F15Rec_ID = F15.Rec_Id
                                 WHERE F1507_Date <= @endDate
                                   AND F1507_Date >= @startDate) AS t
                                ON t.F15Rec_ID = x.F15Rec_ID AND (x.f2201 = t.F4610 OR t.F1506 <> x.f2201)
            WHERE X.F2205 <= @endDate
              AND X.F2205 >= @startDate) AS s
               INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F22 AS f
                          ON s.F15Rec_ID = f.F15Rec_ID AND f.F2205 <= s.F2205 AND f.F2206 <= s.F2206
               INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F15 ON s.F15Rec_ID = F15.Rec_Id) AS allTimes
         /*find the latest recorded wagon location*/
         INNER JOIN (SELECT d.F4602,
                            MAX(dbo.seirDateToDatetime(f.F2205, f.F2206)) AS maxEnterTime
                     FROM (SELECT F4601
                                , F4602
                                , F4610
                                , f2205
                                , f2206
                                , t.F15Rec_ID
                           FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F22 AS x
                                    INNER JOIN (SELECT F4601
                                                     , F4602
                                                     , F4610
                                                     , F46.F15Rec_ID
                                                     , F1506
                                                FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F46
                                                         INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F15
                                                                    ON F46.F15Rec_ID = F15.Rec_Id
                                                WHERE F1507_Date <= @endDate
                                                  AND F1507_Date >= @startDate) AS t
                                               ON t.F15Rec_ID = x.F15Rec_ID AND
                                                  (x.f2201 = t.F4610 OR t.F1506 <> x.f2201)
                           WHERE X.F2205 <= @endDate
                             AND X.F2205 >= @startDate) AS d
                              INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F22 AS f
                                         ON d.F15Rec_ID = f.F15Rec_ID AND f.F2205 <= d.F2205 AND f.F2206 <= d.F2206
                     GROUP BY d.F4602) AS maxs
                    ON maxs.F4602 = allTimes.F4602 AND maxs.maxEnterTime = allTimes.EnterTime

WHERE (allTimes.f4601 = 10
    OR allTimes.f4601 = 24
    OR allTimes.f4601 = 30);

drop table Traffic.dbo.capacity;
create table Traffic.dbo.capacity
(
    station  int,
    freight  Bigint,
    capacity int
);

insert into Traffic.dbo.capacity

select cap.station, cap.freight, max(cap.wagons) as capacity
from (select station, freight, day, count(wagon) as wagons
      from [172.23.27.208].Nafis_Seir_Proj.dbo.cap
      group by station, freight, day) as cap
group by station, freight;