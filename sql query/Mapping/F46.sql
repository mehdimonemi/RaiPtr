select *
into MappingDB.dbo.F46
from (select A.F4601,
             A.F4602,
             (case
                  when (dbo.to6WagonID(A.F4602) is not null or dbo.to6WagonID(A.F4602) <> 0)
                      then dbo.to6WagonID(A.F4602)
                  else F.Mms_Fld_pk_SerialNo end)                                              as wagonPriId,
             A.F4603,
             cast(concat(Format(A.F4604_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F4605, 1, 2), ':',
                         SUBSTRING(A.F4605, 3, 2)) as datetime)                                as F4604,
             A.F4606,
             A.F4607,
             A.F4608,
             A.F4609,
             A.F4610,
             A.F4611,
             A.F4612,
             A.F4617,
             A.F4618,
             A.Rec_Id,
             A.Rec_Id_InServer,
             A.nahi,
             A.F4608_D_M,
             A.IsInvalid_WagonNo,
             A.AddLog_User,
             cast(concat(Format(A.addlog_Date, 'yyyy/MM/dd'), ' ', A.AddLog_Time) as datetime) as AddLog_Date,
             A.AddLog_IP,
             A.EditLog_User,
             A.Modify_Date                                                                     as EditLog_Date,
             A.EditLog_IP,
             A.Barnameh_ID,
             A.F15Rec_ID,
             A.F46TransRec_ID,
             A.Bar_Real_Weight,
             A.Bar_Kind,
             A.Kargozar
      from [172.23.27.208].Nafis_Seir_Proj.dbo.F46 as A
               left join Traffic.dbo.Wagon_History as F on dbo.to6WagonID(A.F4602) = F.Mms_Fld_pk_SerialNo
      where A.AddLog_Date >= cast('2021-03-21 00:00:00' as datetime)) as v;

insert into MappingDB.dbo.F46
select *
from (select A.F4601,
             A.F4602,
             (case
                  when (dbo.to6WagonID(A.F4602) is not null or dbo.to6WagonID(A.F4602) <> 0)
                      then dbo.to6WagonID(A.F4602)
                  else F.Mms_Fld_pk_SerialNo end)               as wagonPriId,
             A.F4603,
             cast(concat(Format(A.F4604_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F4605, 1, 2), ':',
                         SUBSTRING(A.F4605, 3, 2)) as datetime)                                as F4604,
             A.F4606,
             A.F4607,
             A.F4608,
             A.F4609,
             A.F4610,
             A.F4611,
             A.F4612,
             A.F4617,
             A.F4618,
             A.Rec_Id,
             A.Rec_Id_InServer,
             A.nahi,
             A.F4608_D_M,
             A.IsInvalid_WagonNo,
             A.AddLog_User,
             cast(concat(Format(A.addlog_Date, 'yyyy/MM/dd'), ' ', A.AddLog_Time) as datetime) as AddLog_Date,
             A.AddLog_IP,
             A.EditLog_User,
             A.Modify_Date                                                                     as EditLog_Date,
             A.EditLog_IP,
             A.Barnameh_ID,
             A.F15Rec_ID,
             A.F46TransRec_ID,
             A.Bar_Real_Weight,
             A.Bar_Kind,
             A.Kargozar
      from [172.23.27.208].Nafis_Seir_Proj.dbo.F46 as A
               left join Traffic.dbo.Wagon_History as F on dbo.to6WagonID(A.F4602) = F.Mms_Fld_pk_SerialNo
               Left join MappingDB.dbo.F46 as B on B.Rec_ID = A.Rec_ID
      where A.AddLog_Date > getdate() - 1
        and B.Rec_ID is null) as v
where v.AddLog_Date >= getdate() - 0.011;

update MappingDB.dbo.F46
set F4601             = c.F4601,
    F4602             = c.F4602,
    wagonPriId        = c.wagonPriId,
    F4603             = c.F4603,
    F4604             = c.F4604,
    F4606             = c.F4606,
    F4607             = c.F4607,
    F4608             = c.F4608,
    F4609             = c.F4609,
    F4610             = c.F4610,
    F4611             = c.F4611,
    F4612             = c.F4612,
    F4617             = c.F4617,
    F4618             = c.F4618,
    Rec_Id            = c.Rec_Id,
    Rec_Id_InServer   = c.Rec_Id_InServer,
    nahi              = c.nahi,
    F4608_D_M         = c.F4608_D_M,
    IsInvalid_WagonNo = c.IsInvalid_WagonNo,
    AddLog_User       = c.AddLog_User,
    AddLog_Date       = c.AddLog_Date,
    AddLog_IP         = c.AddLog_IP,
    EditLog_User      = c.EditLog_User,
    EditLog_Date      = c.EditLog_Date,
    EditLog_IP        = c.EditLog_IP,
    Barnameh_ID       = c.Barnameh_ID,
    F15Rec_ID         = c.F15Rec_ID,
    F46TransRec_ID    = c.F46TransRec_ID,
    Bar_Real_Weight   = c.Bar_Real_Weight,
    Bar_Kind          = c.Bar_Kind,
    Kargozar          = c.Kargozar
from MappingDB.dbo.F46 as d
         inner join (select *
                     from (select A.F4601,
                                  A.F4602,
                                  (case
                                       when (dbo.to6WagonID(A.F4602) is not null or dbo.to6WagonID(A.F4602) <> 0)
                                           then dbo.to6WagonID(A.F4602)
                                       else F.Mms_Fld_pk_SerialNo end)               as wagonPriId,
                                  A.F4603,
                                  cast(concat(Format(A.F4604_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F4605, 1, 2), ':',
                                              SUBSTRING(A.F4605, 3, 2)) as datetime)                                as F4604,
                                  A.F4606,
                                  A.F4607,
                                  A.F4608,
                                  A.F4609,
                                  A.F4610,
                                  A.F4611,
                                  A.F4612,
                                  A.F4617,
                                  A.F4618,
                                  A.Rec_Id,
                                  A.Rec_Id_InServer,
                                  A.nahi,
                                  A.F4608_D_M,
                                  A.IsInvalid_WagonNo,
                                  A.AddLog_User,
                                  cast(concat(Format(A.addlog_Date, 'yyyy/MM/dd'), ' ', A.AddLog_Time) as datetime) as AddLog_Date,
                                  A.AddLog_IP,
                                  A.EditLog_User,
                                  A.Modify_Date                                                                     as EditLog_Date,
                                  A.EditLog_IP,
                                  A.Barnameh_ID,
                                  A.F15Rec_ID,
                                  A.F46TransRec_ID,
                                  A.Bar_Real_Weight,
                                  A.Bar_Kind,
                                  A.Kargozar
                           from [172.23.27.208].Nafis_Seir_Proj.dbo.F46 as A
                                    left join Traffic.dbo.Wagon_History as F
                                              on dbo.to6WagonID(A.F4602) = F.Mms_Fld_pk_SerialNo
                           where A.AddLog_Date > getdate() - 10) as v
                     where v.AddLog_Date > getdate() - 10) as c
                    on c.Rec_ID = d.Rec_ID and c.EditLog_Date <> d.EditLog_Date;

select *
from MappingDB.dbo.F46
where AddLog_Date > getdate() - 0.011