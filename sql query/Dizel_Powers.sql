DECLARE @startDate INTEGER;
DECLARE @endDate INTEGER;
SET @endDate=981032;
SET @startDate=981000;
SELECT tso.Tools_No                 AS fleetId
      ,f24.F2401                    AS BlockId
      ,f24.Ent_St
      ,f24.Ext_St
      ,MAX(F15.F1510/dizels.dizel)  AS dizelPower
-- INTO   Traffic.dbo.dizel_powers
FROM   graph.dbo.Tools_Status_Op AS tso
       /*conncet to block time table*/
       INNER JOIN graph.dbo.F24 AS f24
            ON  tso.F15Rec_ID = f24.F15Rec_ID
       /*conncet to trains table*/
       INNER JOIN graph.dbo.F15 AS F15
            ON  tso.F15Rec_ID = F15.Rec_Id
       /*understand number of dizels on any train*/
       INNER JOIN (
                SELECT f15.Rec_Id
                      ,COUNT(*)      AS dizel
                FROM   graph.dbo.f15  AS f15
                       INNER JOIN graph.dbo.Tools_Status_Op AS tso2
                            ON  tso2.F15Rec_ID = f15.Rec_Id
                WHERE  tso2.Status_Code = 1
                       OR tso2.Status_Code = 7
                       OR tso2.Status_Code = 8
                       OR tso2.Status_Code = 9
                       OR tso2.Status_Code = 11
                       OR tso2.Status_Code = 12
                GROUP BY
                       f15.Rec_Id
            ) AS dizels
            ON  dizels.Rec_Id = F15.Rec_Id
       /*know how many block a train went and what kind is it*/
       INNER JOIN (
                SELECT f15.Rec_Id
                      ,p.kind            AS TrainKind
                      ,COUNT(f24.f2401)  AS blocks
                FROM   graph.dbo.F15      AS F15
                       INNER JOIN graph.dbo.P8 AS p
                            ON  p.CODE = F15.f1504
                       INNER JOIN graph.dbo.f24
                            ON  f24.F15Rec_ID = F15.Rec_Id
                GROUP BY
                       f15.Rec_Id
                      ,p.kind
            ) AS x
            ON  tso.F15Rec_ID = x.Rec_Id
WHERE  tso.Tools_No>8
       AND (x.TrainKind=1) AND (tso.T_Date<@endDate AND tso.T_Date>@startDate)
       AND (f24.f2404<@endDate AND f24.f2404>@startDate) AND (F15.F1502<@endDate AND F15.F1502 >@startDate)
GROUP BY
       tso.Tools_No
      ,f24.F2401
      ,f24.Ent_St
      ,f24.Ext_St