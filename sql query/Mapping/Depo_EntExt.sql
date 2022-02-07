select A.Dizel_No,
       cast(concat(Format(A.Ent_Date, 'yyyy/MM/dd'), ' ',
                   replace(left(Ent_Time, CHARINDEX(':', Ent_Time) - 1), '.', ''), ':',
                   replace(right(Ent_Time, LEN(Ent_Time) - CHARINDEX(':', Ent_Time)), '.',
                           '')) as datetime)                       as Ent_Date,
       case
           when A.Ext_Time is not null then
               cast(concat(Format(A.Ext_Date, 'yyyy/MM/dd'), ' ',
                           replace(left(Ext_Time, CHARINDEX(':', Ext_Time) - 1), '.', ''), ':',
                           replace(right(Ext_Time, LEN(Ext_Time) - CHARINDEX(':', Ext_Time)), '.',
                                   '')) as datetime) end           as Ext_Date,
       A.Code_Depo,
       A.Code_Area,
       replace(cast(A.Descript as varchar(4000)), N'&#1740', N'ی') as Descript,
       A.AreaDes,
       A.DepoDes
into MappingDB.dbo.Depo_EntExt
from [172.23.27.134].dailyloco.dbo.V_dizel_entext_forTrafic as A
-- where A.Ent_Date >= cast('2021-03-21 00:00:00' as datetime);

insert into MappingDB.dbo.Depo_EntExt
select *
from (select A.Dizel_No,
             cast(concat(Format(A.Ent_Date, 'yyyy/MM/dd'), ' ',
                         replace(left(Ent_Time, CHARINDEX(':', Ent_Time) - 1), '.', ''), ':',
                         replace(right(Ent_Time, LEN(Ent_Time) - CHARINDEX(':', Ent_Time)), '.',
                                 '')) as datetime)                       as Ent_Date,
             case
                 when A.Ext_Time is not null then
                     cast(concat(Format(A.Ext_Date, 'yyyy/MM/dd'), ' ',
                                 replace(left(Ext_Time, CHARINDEX(':', Ext_Time) - 1), '.', ''), ':',
                                 replace(right(Ext_Time, LEN(Ext_Time) - CHARINDEX(':', Ext_Time)), '.',
                                         '')) as datetime) end           as Ext_Date,
             A.Code_Depo,
             A.Code_Area,
             replace(cast(A.Descript as varchar(4000)), N'&#1740', N'ی') as Descript,
             A.AreaDes,
             A.DepoDes
      from [172.23.27.134].dailyloco.dbo.V_dizel_entext_forTrafic as A
      where A.Ent_Date >= getdate() - 1) as v
where v.Ent_Date >= getdate() - 0.011;

select *
from MappingDB.dbo.Depo_EntExt
where Ent_Date >= getdate() - 1
