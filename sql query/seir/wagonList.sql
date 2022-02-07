select case when Z.wagonPriId is null then F.wagonPriId else Z.wagonPriId end as wagonPriId,
       Z.F4601                                                                as wagonTypeFromF46,
       F.Wagon_Type                                                           as wagonTypeFromHis,
       Z.F4602                                                                as wagonIdF46,
       concat(F.Mms_Fld_pk_SerialNo, F.Mms_Fld_AutoControl)                   as wagonIdHis
into Traffic.dbo.wagonList
from MappingDB.dbo.F46 as Z
         join MappingDB.dbo.F22 as X on X.F15Rec_ID = Z.F15Rec_ID
         join (select S.wagonPriId, max(concat(A.Rec_Id, s.rec_ID)) as latestRecord
               from MappingDB.dbo.F46 as S
                        inner join MappingDB.dbo.F22 as A on A.F15Rec_ID = S.F15Rec_ID
               where S.AddLog_Date > cast('2021-03-21 00:00:00' as datetime)
               group by S.wagonPriId) as D
              on D.latestRecord = concat(X.Rec_Id, Z.rec_ID) and Z.wagonPriId = D.wagonPriId
         full outer join Traffic.dbo.Wagon_History as F on Z.wagonPriId = F.wagonPriId
where Z.F4602 > 9999999
   or ((Z.F4602 >= 1400000 and Z.F4602 <= 9999999)
    and (Z.F4601 <> 20 and
         Z.F4601 <> 25 and
         Z.F4601 <> 27))
   or Z.F4602 is null
   or (Z.F4602 < 1400000 and Z.F4601 = 30);

insert into Traffic.dbo.wagonList
select case when Z.wagonPriId is null then F.wagonPriId else Z.wagonPriId end as wagonPriId,
       Z.F4601                                                                as wagonTypeFromF46,
       F.Wagon_Type                                                           as wagonTypeFromHis,
       Z.F4602                                                                as wagonIdF46,
       concat(F.Mms_Fld_pk_SerialNo, F.Mms_Fld_AutoControl)                   as wagonIdHis
from MappingDB.dbo.F46 as Z
         join MappingDB.dbo.F22 as X on X.F15Rec_ID = Z.F15Rec_ID
         join (select S.wagonPriId, max(concat(A.Rec_Id, s.rec_ID)) as latestRecord
               from MappingDB.dbo.F46 as S
                        inner join MappingDB.dbo.F22 as A on A.F15Rec_ID = S.F15Rec_ID
               where S.AddLog_Date > getdate() - 10
               group by S.wagonPriId) as D
              on D.latestRecord = concat(X.Rec_Id, Z.rec_ID) and Z.wagonPriId = D.wagonPriId
         full outer join Traffic.dbo.Wagon_History as F on Z.wagonPriId = F.wagonPriId
         left join Traffic.dbo.wagonList as W on W.wagonPriId = Z.wagonPriId
where (Z.AddLog_Date > getdate() - 1)
  and (Z.F4602 > 9999999
    or ((Z.F4602 >= 1400000 and Z.F4602 <= 9999999)
        and (Z.F4601 <> 20 and
             Z.F4601 <> 25 and
             Z.F4601 <> 27))
    or Z.F4602 is null
    or (Z.F4602 < 1400000 and Z.F4601 = 30))
  and W.wagonPriId is null;