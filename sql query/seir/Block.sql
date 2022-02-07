select *
into Traffic.dbo.Blocks
from (
         select a.code,
                a.upcode,
                a.[Desc]  as upName,
                a.downcode,
                w2.[Desc] as downName,
                a.len,
                a.Visable,
                a.Line_Count,
                a.Line_Count_Upper
         from (
                  select b.code,
                         b.upcode,
                         w.[Desc],
                         b.downcode,
                         b.len,
                         b.Visable,
                         b.Line_Count,
                         b.Line_Count_Upper
                  from [172.23.27.208].Nafis_Seir_Proj.dbo.Block as b
                           left join (select * from Traffic.dbo.stations) as w on b.upcode = w.code
                  where b.Visable = 'true') as a
                  left join (select * from Traffic.dbo.stations) as w2 on a.downcode = w2.code
         group by a.code, a.upcode, a.Line_Count_Upper, a.Line_Count, w2.[Desc], a.[Desc], a.downcode, a.len,
                  a.Visable) as aw2 where upName is not null and downName is not null

select * from Traffic.dbo.Blocks where code = 1034
