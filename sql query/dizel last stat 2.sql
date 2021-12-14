DECLARE @endDate char(10);
SET @endDate = cast(concat(dbo.GregorianToJalali(getdate(), 'yyMMdd'), format(getdate(), 'HHmm')) as char(10));
DECLARE @startDate char(10);
SET @startDate = cast(concat(dbo.GregorianToJalali(getdate() - 1, 'yyMMdd'), format(getdate(), 'HHmm')) as char(10));

SELECT allTimes.F4602                           AS fleetId,
       allTimes.f4601                           AS fleetKind,
       allTimes.f4612                           AS Destination,
       allTimes.F2201                           AS lastStation,
       allTimes.f4610                           AS detachStation,
       allTimes.F1506                           AS trainDestination,
       allTimes.STATUS                          AS STATUS,
       allTimes.F15Rec_ID                       AS trainRecId,
       ISNULL(LTRIM(RTRIM(allTimes.F2205)), '') AS lastStationEnterYear,
       ISNULL(LTRIM(RTRIM(allTimes.F2206)), '') AS lastStationEnterTime,
       ISNULL(LTRIM(RTRIM(allTimes.F2207)), '') AS lastStationExitYear,
       ISNULL(LTRIM(RTRIM(allTimes.F2208)), '') AS lastStationExitTime,
       maxs.maxEnterTime                        AS lastTimeCalculate,
       ISNULL(LTRIM(RTRIM(allTimes.F1502)), '') AS trainFormationYear,
       ISNULL(LTRIM(RTRIM(allTimes.F1503)), '') AS trainFormationTime
INTO Traffic.dbo.dizel_last_stat
FROM (SELECT s.F4602,
             s.f4601,
             s.f4610,
             s.f4607,
             s.f4612,
             s.F15Rec_ID,
             f.F2201,
             f.F2205,
             f.F2206,
             f.F2207,
             f.F2208,
             0                                        AS STATUS,
             dbo.seirDateToDatetime(f.F2205, f.F2206) AS EnterTime,
             F15.F1506,
             F15.F1502,
             F15.F1503
      FROM (SELECT F4602,
                   F4610,
                   F4601,
                   f4607,
                   f4612,
                   f2205,
                   f2206,
                   t.F15Rec_ID
            FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F22 AS x
                     INNER JOIN (
                SELECT F4602,
                       F4610,
                       F4601,
                       f4607,
                       f4612,
                       F46.F15Rec_ID,
                       F1506
                FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F46
                         INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F15 ON F46.F15Rec_ID = F15.Rec_Id) AS t
                                ON t.F15Rec_ID = x.F15Rec_ID AND (x.f2201 = t.F4610 OR t.F1506 <> x.f2201)
            where CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) < @endDate
              AND CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) > @startDate
           ) AS s
               INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F22 AS f
                          ON s.F15Rec_ID = f.F15Rec_ID AND f.F2205 <= s.F2205 AND f.F2206 <= s.F2206
               INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F15 ON s.F15Rec_ID = F15.Rec_Id) AS allTimes
         INNER JOIN (SELECT d.F4602, MAX(dbo.seirDateToDatetime(f.F2205, f.F2206)) AS maxEnterTime
                     FROM (SELECT F4602,
                                  F4610,
                                  F4601,
                                  a.f2205,
                                  a.f2206,
                                  t.F15Rec_ID
                           FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F22 AS a
                                    INNER JOIN (SELECT F4602,
                                                       F4610,
                                                       F4601,
                                                       F46.F15Rec_ID,
                                                       F1506
                                                FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F46
                                                         INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F15
                                                                    ON F46.F15Rec_ID = F15.Rec_Id) AS s
                                               ON t.F15Rec_ID = x.F15Rec_ID AND
                                                  (x.f2201 = t.F4610 OR t.F1506 <> x.f2201)
                           where CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) < @endDate
                             AND CAST(CONCAT(RTRIM(LTRIM(F2205)), RTRIM(LTRIM(F2206))) AS FLOAT) > @startDate) AS d
                              INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.F22 AS f
                                         ON d.F15Rec_ID = f.F15Rec_ID
                                             AND f.F2205 <= d.F2205
                                             AND f.F2206 <= d.F2206
                     GROUP BY d.F4602) AS maxs ON maxs.F4602 = allTimes.F4602 AND maxs.maxEnterTime = allTimes.EnterTime
         INNER JOIN (SELECT tso.Tools_No
                     FROM [172.23.27.208].Nafis_Seir_Proj.dbo.Tools_Status_Op AS tso
                              INNER JOIN (SELECT z.Rec_Id, x.kind AS TrainKind, COUNT(c.f2401) AS blocks
                                          FROM [172.23.27.208].Nafis_Seir_Proj.dbo.F15 as z
                                                   INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.P8 AS x ON x.CODE = z.f1504
                                                   INNER JOIN [172.23.27.208].Nafis_Seir_Proj.dbo.f24 as c
                                                              ON c.F15Rec_ID = z.Rec_Id
                                          GROUP BY z.Rec_Id, x.kind) AS v ON tso.F15Rec_ID = v.Rec_Id
                     WHERE blocks > 4
                       AND (v.TrainKind = 1 OR v.TrainKind = 2)
                     GROUP BY tso.Tools_No) AS dizelList ON allTimes.F4602 = dizelList.Tools_No
