insert into MappingDB.dbo.F24
select *
from (select A.F2401,
             A.F2402,
             A.F2403,
             cast(concat(Format(A.F2404_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F2405, 1, 2), ':',
                         SUBSTRING(A.F2405, 3, 2)) as datetime)                                as F2404,
             cast(concat(Format(A.F2406_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F2407, 1, 2), ':',
                         SUBSTRING(A.F2407, 3, 2)) as datetime)                                as F2406,
             IIF(A.F2409 != '', cast(concat(Format(A.F2408_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F2409, 1, 2), ':',
                                            SUBSTRING(A.F2409, 3, 2)) as datetime), null)      as F2408,
             A.nahi,
             A.Ent_St,
             A.Ext_St,
             A.Invalid_Access_4_Block,
             A.Rec_Id,
             A.Rec_Id_InServer,
             A.Is_Masdoodi,
             A.AddLog_User,
             cast(concat(Format(A.addlog_Date, 'yyyy/MM/dd'), ' ', A.AddLog_Time) as datetime) as AddLog_Date,
             A.AddLog_IP,
             A.EditLog_User,
             A.Modify_Date                                                                     as EditLog_Date,
             A.EditLog_IP,
             A.Return_2_Block,
             A.StopDate,
             A.StopDate_M,
             A.StopTime,
             A.StartDate,
             A.StartDate_M,
             A.StartTime,
             A.StopKm,
             A.F15EmdadRec_ID,
             A.F15Rec_ID
      from [172.23.27.208].Nafis_Seir_Proj.dbo.F24 as A
               Left join MappingDB.dbo.F24 as B on B.Rec_ID = A.Rec_ID
      where A.AddLog_Date > getdate() - 1
        and B.Rec_ID is null) as v
where v.AddLog_Date >= getdate() - 0.011;

update MappingDB.dbo.F24
set F2401                  = c.F2401,
    F2402                  = c.F2402,
    F2403                  = c.F2403,
    F2404                  = c.F2404,
    F2406                  = c.F2406,
    F2408                  = c.F2408,
    nahi                   = c.nahi,
    Ent_St                 = c.Ent_St,
    Ext_St                 = c.Ext_St,
    Invalid_Access_4_Block = c.Invalid_Access_4_Block,
    Rec_Id                 = c.Rec_Id,
    Rec_Id_InServer        = c.Rec_Id_InServer,
    Is_Masdoodi            = c.Is_Masdoodi,
    AddLog_User            = c.AddLog_User,
    AddLog_Date            = c.AddLog_Date,
    AddLog_IP              = c.AddLog_IP,
    EditLog_User           = c.EditLog_User,
    EditLog_Date           = c.EditLog_Date,
    EditLog_IP             = c.EditLog_IP,
    Return_2_Block         = c.Return_2_Block,
    StopDate               = c.StopDate,
    StopDate_M             = c.StopDate_M,
    StopTime               = c.StopTime,
    StartDate              = c.StartDate,
    StartDate_M            = c.StartDate_M,
    StartTime              = c.StartTime,
    StopKm                 = c.StopKm,
    F15EmdadRec_ID         = c.F15EmdadRec_ID,
    F15Rec_ID              = c.F15Rec_ID
from MappingDB.dbo.F22 as d
         inner join (select *
                     from (select A.F2401,
                                  A.F2402,
                                  A.F2403,
                                  cast(concat(Format(A.F2404_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F2405, 1, 2), ':',
                                              SUBSTRING(A.F2405, 3, 2)) as datetime)                                as F2404,
                                  cast(concat(Format(A.F2406_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F2407, 1, 2), ':',
                                              SUBSTRING(A.F2407, 3, 2)) as datetime)                                as F2406,
                                  IIF(A.F2409 != '',
                                      cast(concat(Format(A.F2408_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F2409, 1, 2), ':',
                                                  SUBSTRING(A.F2409, 3, 2)) as datetime),
                                      null)                                                                         as F2408,
                                  A.nahi,
                                  A.Ent_St,
                                  A.Ext_St,
                                  A.Invalid_Access_4_Block,
                                  A.Rec_Id,
                                  A.Rec_Id_InServer,
                                  A.Is_Masdoodi,
                                  A.AddLog_User,
                                  cast(concat(Format(A.addlog_Date, 'yyyy/MM/dd'), ' ', A.AddLog_Time) as datetime) as AddLog_Date,
                                  A.AddLog_IP,
                                  A.EditLog_User,
                                  A.Modify_Date                                                                     as EditLog_Date,
                                  A.EditLog_IP,
                                  A.Return_2_Block,
                                  A.StopDate,
                                  A.StopDate_M,
                                  A.StopTime,
                                  A.StartDate,
                                  A.StartDate_M,
                                  A.StartTime,
                                  A.StopKm,
                                  A.F15EmdadRec_ID,
                                  A.F15Rec_ID
                           from [172.23.27.208].Nafis_Seir_Proj.dbo.F24 as A
                           where A.AddLog_Date > getdate() - 10) as v
                     where v.AddLog_Date > getdate() - 10) as c
                    on c.Rec_ID = d.Rec_ID and c.EditLog_Date <> d.EditLog_Date;

select *
from MappingDB.dbo.F24
where AddLog_Date > getdate() - 0.011