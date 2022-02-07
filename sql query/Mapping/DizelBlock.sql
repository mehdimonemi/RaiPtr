select *
into MappingDB.dbo.DizelBlock
from (select A.Dizel_No,
             cast(concat(Format(A.Ent_Date, 'yyyy/MM/dd'), ' ',
                         replace(left(A.COLDTIME, CHARINDEX(':', A.COLDTIME) - 1), '.', ''), ':',
                         replace(right(A.COLDTIME, LEN(A.COLDTIME) - CHARINDEX(':', A.COLDTIME)), '.',
                                 '')) as datetime)             as Ent_Date,
             case
                 when A.STOPTIME is not null then
                     cast(concat(Format(iif(A.stoptime >= A.coldtime,
                                            A.Ent_Date, A.Ent_Date + 1), 'yyyy/MM/dd'), ' ',
                                 replace(left(A.STOPTIME, CHARINDEX(':', A.STOPTIME) - 1), '.', ''), ':',
                                 replace(right(A.STOPTIME, LEN(A.STOPTIME) - CHARINDEX(':', A.STOPTIME)), '.',
                                         '')) as datetime) end as STOPTIME,
             Code_Area,
             Code_DEPO,
             Code_BLOCK,
             SOS_Dizel,
             EMDAD_DizelNo1,
             EMDAD_DizelNo2,
             Code_Train_Kind,
             Train_No,
             No_Moaser,
             AreaDes,
             DepoDes,
             TrainKindDes
      from [172.23.27.134].dailyloco.dbo.V_DizelBlock_ForTrafic as A
      where A.Ent_Date >= cast('2010-03-21 00:00:00' as datetime)) as v;

delete
from MappingDB.dbo.DizelBlock
where Ent_Date >= format(getdate() - 10, 'yyyy-MM-dd');

insert into MappingDB.dbo.DizelBlock
select A.Dizel_No,
       cast(concat(Format(A.Ent_Date, 'yyyy/MM/dd'), ' ',
                   replace(left(A.COLDTIME, CHARINDEX(':', A.COLDTIME) - 1), '.', ''), ':',
                   replace(right(A.COLDTIME, LEN(A.COLDTIME) - CHARINDEX(':', A.COLDTIME)), '.',
                           '')) as datetime)             as Ent_Date,
       case
           when A.STOPTIME is not null then
               cast(concat(Format(iif(A.stoptime >= A.coldtime,
                                      A.Ent_Date, A.Ent_Date + 1), 'yyyy/MM/dd'), ' ',
                           replace(left(A.STOPTIME, CHARINDEX(':', A.STOPTIME) - 1), '.', ''), ':',
                           replace(right(A.STOPTIME, LEN(A.STOPTIME) - CHARINDEX(':', A.STOPTIME)), '.',
                                   '')) as datetime) end as STOPTIME,
       Code_Area,
       Code_DEPO,
       Code_BLOCK,
       SOS_Dizel,
       EMDAD_DizelNo1,
       EMDAD_DizelNo2,
       Code_Train_Kind,
       Train_No,
       No_Moaser,
       AreaDes,
       DepoDes,
       TrainKindDes
from [172.23.27.134].dailyloco.dbo.V_DizelBlock_ForTrafic as A
where A.Ent_Date >= format(getdate() - 10, 'yyyy-MM-dd');


select *
from MappingDB.dbo.DizelBlock
where Ent_Date >= getdate() - 1
