DECLARE @startDate INTEGER;
DECLARE @endDate INTEGER;
SET @endDate = 980932;
SET @startDate = 980900;
select f.Rec_Id         as                 TrainID,
       f.f1509          as                 VazneKhales,
       f1510            as                 vazneKol,
       f.f1513          as                 toolTrain,
       stuff((SELECT '; ' + cast(d.Tools_No as varchar)
              from graph.dbo.Tools_Status_OP as D
              WHERE f.Rec_Id = D.F15Rec_ID
                and (D.Status_Code = 1
                  OR D.Status_Code = 7
                  OR D.Status_Code = 8
                  OR D.Status_Code = 9
                  OR D.Status_Code = 11
                  OR D.Status_Code = 12)
              FOR XML PATH('')), 1, 1, '') [dizels]
        ,
       count(f46.F4602) as                 wagon,
       stuff((SELECT '; ' + cast(f24.F2401 as varchar)
              from graph.dbo.f24 as f24
              WHERE f.Rec_Id = f24.F15Rec_ID
              FOR XML PATH('')), 1, 1, '') [blocks]
from graph.dbo.f15 as f
         LEFT JOIN graph.dbo.P8 ON f.F1504 = P8.CODE
         left join graph.dbo.f46 on f.Rec_Id = f46.F15Rec_ID
where p8.kind = 1
  and ((f1502 < @endDate AND f1502 > @startDate))
  and (f46.f4601 = 10
    OR f46.f4601 = 24
    OR f46.f4601 = 30)

group by f.Rec_Id,
         f.f1509,
         f1510,
         f.f1513