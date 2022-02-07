select A.Dizel_No,
       A.Ent_Date,
       A.Ext_Date,
       A.Code_Block_Location,
       A.Dizel_MP,
       A.BazsaziLocation,
       A.Code_Bazsazi,
       A.EntReason,
       A.ExtReason,
       A.Descript
into MappingDB.dbo.DizelBazsazi
from [172.23.27.134].dailyloco.dbo.V_Bazsazi_ForTrafic as A
-- where A.Ent_Date >= cast('2021-03-21 00:00:00' as datetime);

delete
from MappingDB.dbo.DizelBazsazi
where Ent_Date = format(getdate(), 'yyyy-MM-dd');
go
insert into MappingDB.dbo.DizelBazsazi
select A.Dizel_No,
       A.Ent_Date,
       A.Ext_Date,
       A.Code_Block_Location,
       A.Dizel_MP,
       A.BazsaziLocation,
       A.Code_Bazsazi,
       A.EntReason,
       A.ExtReason,
       A.Descript
from [172.23.27.134].dailyloco.dbo.V_Bazsazi_ForTrafic as A
where A.Ent_Date = format(getdate(), 'yyyy-MM-dd');
go

select *
from MappingDB.dbo.DizelBazsazi
where Ent_Date >= getdate() - 1