select A.Dizel_No,
       A.Ent_Date,
       A.Cold_Warm,
       A.Total_Kilometer,
       A.Code_Bazdid,
       A.Salem_Kharab,
       A.Code_Depo,
       A.Code_Area,
       A.Train_No,
       A.TrainKind,
       A.Code_Source,
       A.Code_Destination,
       A.Save_Dizel,
       A.Descript,
       A.AreaDes,
       A.TrainDes
into MappingDB.dbo.DizelDaily
from [172.23.27.134].dailyloco.dbo.V_DailyInfo_ForTrafic as A
-- where A.Ent_Date >= cast('2021-03-21 00:00:00' as datetime);

delete
from MappingDB.dbo.DizelDaily
where Ent_Date = format(getdate(), 'yyyy-MM-dd');
go
insert into MappingDB.dbo.DizelDaily
select A.Dizel_No,
       A.Ent_Date,
       A.Cold_Warm,
       A.Total_Kilometer,
       A.Code_Bazdid,
       A.Salem_Kharab,
       A.Code_Depo,
       A.Code_Area,
       A.Train_No,
       A.TrainKind,
       A.Code_Source,
       A.Code_Destination,
       A.Save_Dizel,
       A.Descript,
       A.AreaDes,
       A.TrainDes
from [172.23.27.134].dailyloco.dbo.V_DailyInfo_ForTrafic as A
where A.Ent_Date = format(getdate(), 'yyyy-MM-dd');
go

select *
from MappingDB.dbo.DizelDaily
where Ent_Date >= getdate() - 1