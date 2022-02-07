select A.Dizel_No,
       A.Ent_Date,
       A.Ext_Date,
       A.Code_Asasi,
       A.Expr1,
       A.EntAsasiDes,
       A.ExtAsasiDes
into MappingDB.dbo.DizelAsasi
from [172.23.27.134].dailyloco.dbo.V_Asasi_ForTrafic as A
-- where A.Ent_Date >= cast('2021-03-21 00:00:00' as datetime);

delete
from MappingDB.dbo.DizelAsasi
where Ent_Date = format(getdate(), 'yyyy-MM-dd');
go
insert into MappingDB.dbo.DizelAsasi
select A.Dizel_No,
       A.Ent_Date,
       A.Ext_Date,
       A.Code_Asasi,
       A.Expr1,
       A.EntAsasiDes,
       A.ExtAsasiDes
from [172.23.27.134].dailyloco.dbo.V_Asasi_ForTrafic as A
where A.Ent_Date = format(getdate(), 'yyyy-MM-dd');
go

select *
from MappingDB.dbo.DizelAsasi
where Ent_Date >= getdate() - 1