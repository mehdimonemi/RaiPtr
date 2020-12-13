DECLARE @startDate INTEGER;
DECLARE @endDate INTEGER;
SET @endDate = 981032;
SET @startDate = 981000;
SELECT F.F2401                AS BlockId
     , upcode                 AS startStationID
     , downcode               AS endStationID
     , [len]                  AS length
     , lengthGIS              AS lengthGIS
     , COUNT(*)               AS train
     , AVG(
            -(
                        (
                                (CONVERT(FLOAT, SUBSTRING(F2406, 1, 2)) - 98) * 365 * 24 * 60 + (
                                CASE
                                    WHEN CONVERT(FLOAT, SUBSTRING(F2406, 3, 2)) <= 6 THEN
                                            (CONVERT(FLOAT, SUBSTRING(F2406, 3, 2)) - 1)
                                            * 31
                                    WHEN CONVERT(FLOAT, SUBSTRING(F2406, 3, 2)) = 12 THEN 6 * 31 + 5 * 30
                                    ELSE 6 * 31 + (CONVERT(FLOAT, SUBSTRING(F2406, 3, 2)) - 7) * 30
                                    END
                                ) + (CONVERT(INT, SUBSTRING(F2406, 5, 2)) - 1)
                            ) * 24 * 60 +
                        CONVERT(FLOAT, SUBSTRING(F2407, 1, 2)) * 60 +
                        CONVERT(FLOAT, SUBSTRING(F2407, 3, 2))
                ) + (
                        (
                                (CONVERT(FLOAT, SUBSTRING(F2408, 1, 2)) - 98) * 365 * 24 * 60 + (
                                CASE
                                    WHEN CONVERT(FLOAT, SUBSTRING(F2408, 3, 2)) <= 6 THEN
                                            (CONVERT(FLOAT, SUBSTRING(F2408, 3, 2)) - 1)
                                            * 31
                                    WHEN CONVERT(FLOAT, SUBSTRING(F2408, 3, 2)) = 12 THEN 6 * 31 + 5 * 30
                                    ELSE 6 * 31 + (CONVERT(FLOAT, SUBSTRING(F2408, 3, 2)) - 7) * 30
                                    END
                                ) + (CONVERT(FLOAT, SUBSTRING(F2408, 5, 2)) - 1)
                            ) * 24 * 60 +
                        CONVERT(FLOAT, SUBSTRING(F2409, 1, 2)) * 60 +
                        CONVERT(FLOAT, SUBSTRING(F2409, 3, 2))
                )
    )                         AS timeTravel
     , MAX(finalWeights.W)    AS trainWeight
     , MAX(finalminweights.W) AS trainMinWeight
     , MAX(finallength.t)     AS trainLength
     , MAX(finalminlengths.t) AS trainMineLength
-- INTO Traffic.dbo.seir_blockTime
FROM graph.dbo.F24 AS F
/*connect to trains*/
         INNER JOIN (SELECT Rec_Id AS TrainId
                          , F1510  AS vaznKhales
                          , F1513  AS tool
                     FROM graph.dbo.F15
                     where (F15.F1504 = 14
                         or F15.F1504 = 16
                         or F15.F1504 = 19)) AS trains ON f.F15Rec_ID = TrainId
/*connect to blocks*/
         LEFT JOIN (SELECT Block.code
                         , MIN(Block.upcode)    AS upcode
                         , MAX(Block.downcode)  AS downcode
                         , MAX(Block.[len])     AS len
                         , MAX(Block.lengthGIS) AS lengthGIS
                    FROM graph.dbo.Block
                    WHERE Visable = 1
                    GROUP BY code) AS blocks ON blocks.code = F.F2401
/*connect to weights in two step:first find maxweights then collect alll of its properies*/
         LEFT JOIN (SELECT allweights.F2401
                         , allweights.w
                    FROM (SELECT F2401
                               , F1510    AS w
                               , COUNT(*) AS c
                          FROM graph.dbo.F24
                                   LEFT JOIN graph.dbo.F15
                                             ON f24.F15Rec_ID = f15.Rec_Id
                          WHERE Is_Masdoodi = 0
                            AND (F15.F1504 = 14
                              or F15.F1504 = 16
                              or F15.F1504 = 19)
                            AND F1510 > 0
                            AND (f2404 < @endDate AND f2404 > @startDate)
                          GROUP BY f2401
                                 , F1510
                         ) AS allweights
                             INNER JOIN (SELECT F2401
                                              , MAX(w) AS w
                                         FROM (SELECT F2401
                                                    , F1510    AS w
                                                    , COUNT(*) AS c
                                               FROM graph.dbo.F24
                                                        LEFT JOIN graph.dbo.F15
                                                                  ON f24.F15Rec_ID = f15.Rec_Id
                                               WHERE Is_Masdoodi = 0
                                                 and (F15.F1504 = 14
                                                   or F15.F1504 = 16
                                                   or F15.F1504 = 19)
                                                 AND F1510 > 0
                                                 AND (f2404 < @endDate AND f2404 > @startDate)
                                               GROUP BY f2401
                                                      , F1510
                                              ) AS weights
                                         GROUP BY F2401) AS maxweights
                                        ON maxweights.w = allweights.w AND
                                           maxweights.F2401 = allweights.F2401) AS finalWeights
                   ON finalWeights.F2401 = F.F2401
/*connect to length in two step:first find maxlength then collect all of its properties*/
         LEFT JOIN (SELECT alllength.F2401
                         , alllength.t
                    FROM (SELECT F2401
                               , F1513    AS t
                               , COUNT(*) AS c
                          FROM graph.dbo.F24
                                   LEFT JOIN graph.dbo.F15 ON f24.F15Rec_ID = f15.Rec_Id
                          WHERE Is_Masdoodi = 0
                            AND (F15.F1504 = 14
                              or F15.F1504 = 16
                              or F15.F1504 = 19)
                            AND F1513 > 0
                            AND (f2404 < @endDate AND f2404 > @startDate)
                          GROUP BY f2401
                                 , F1513
                         ) AS alllength
                             INNER JOIN (SELECT F2401
                                              , MAX(t) AS t
                                         FROM (SELECT F2401
                                                    , F1513    AS t
                                                    , COUNT(*) AS c
                                               FROM graph.dbo.F24
                                                        LEFT JOIN graph.dbo.F15
                                                                  ON f24.F15Rec_ID = f15.Rec_Id
                                               WHERE Is_Masdoodi = 0
                                                 AND (F15.F1504 = 14
                                                   or F15.F1504 = 16
                                                   or F15.F1504 = 19)
                                                 AND (f2404 < @endDate AND f2404 > @startDate)
                                                 AND F1513 > 0
                                               GROUP BY f2401
                                                      , F1513
                                              ) AS lengths
                                         GROUP BY F2401) AS maxlengths
                                        ON maxlengths.t = alllength.t
                                            AND maxlengths.F2401 = alllength.F2401) AS finallength
                   ON finallength.F2401 = F.F2401

/*connect to min weights in two step:first find minweights then collect all of its properies*/
         LEFT JOIN (SELECT allweights.F2401
                         , allweights.w
                    FROM (SELECT F2401
                               , F1510    AS w
                               , COUNT(*) AS c
                          FROM graph.dbo.F24
                                   LEFT JOIN graph.dbo.F15 ON f24.F15Rec_ID = f15.Rec_Id
                          WHERE Is_Masdoodi = 0
                            AND (F15.F1504 = 14
                              or F15.F1504 = 16
                              or F15.F1504 = 19)
                            AND (f2404 < @endDate AND f2404 > @startDate)
                            AND F1510 >= 0
                          GROUP BY f2401
                                 , F1510) AS allweights
                             INNER JOIN (SELECT F2401
                                              , min(w) AS w
                                         FROM (SELECT F2401
                                                    , F1510    AS w
                                                    , COUNT(*) AS c
                                               FROM graph.dbo.F24
                                                        LEFT JOIN graph.dbo.F15
                                                                  ON f24.F15Rec_ID = f15.Rec_Id
                                               WHERE Is_Masdoodi = 0
                                                 AND (F15.F1504 = 14
                                                   or F15.F1504 = 16
                                                   or F15.F1504 = 19)
                                                 AND F1510 > 0
                                                 AND (f2404 < @endDate AND f2404 > @startDate)
                                               GROUP BY f2401
                                                      , F1510) AS mweights
                                         where mweights.c > 5
                                         GROUP BY F2401) AS minweights
                                        ON minweights.w = allweights.w AND minweights.F2401 = allweights.F2401
) AS finalminweights ON finalminweights.F2401 = F.F2401

/*connect to min length in two step:first find minlength then collect all of its properties*/
         LEFT JOIN (SELECT alllengths.F2401
                         , alllengths.t
                    FROM (SELECT F2401
                               , F1513    AS t
                               , COUNT(*) AS c
                          FROM graph.dbo.F24
                                   LEFT JOIN graph.dbo.F15
                                             ON f24.F15Rec_ID = f15.Rec_Id
                          WHERE Is_Masdoodi = 0
                            AND (F15.F1504 = 14
                              or F15.F1504 = 16
                              or F15.F1504 = 19)
                            AND (f2404 < @endDate AND f2404 > @startDate)
                            AND F1513 >= 0
                          GROUP BY f2401
                                 , F1513) AS alllengths
                             INNER JOIN (SELECT F2401
                                              , min(t) AS t
                                         FROM (SELECT F2401
                                                    , F1513    AS t
                                                    , COUNT(*) AS c
                                               FROM graph.dbo.F24
                                                        LEFT JOIN graph.dbo.F15
                                                                  ON f24.F15Rec_ID = f15.Rec_Id
                                               WHERE Is_Masdoodi = 0
                                                 AND (F15.F1504 = 14
                                                   or F15.F1504 = 16
                                                   or F15.F1504 = 19)
                                                 AND (f2404 < @endDate AND f2404 > @startDate)
                                                 AND F1513 > 0
                                               GROUP BY f2401
                                                      , F1513) AS mlengths
                                         where mlengths.c > 5
                                         GROUP BY F2401) AS minlengths
                                        ON minlengths.t = alllengths.t AND minlengths.F2401 = alllengths.F2401
) AS finalminlengths ON finalminlengths.F2401 = F.F2401

WHERE Is_Masdoodi = 0
  AND (f2404 < @endDate AND f2404 > @startDate)
GROUP BY F.F2401
       , upcode
       , downcode
       , [len]
       , lengthGIS
ORDER BY F.F2401