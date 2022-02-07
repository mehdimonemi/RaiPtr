insert into MappingDB.dbo.F15
select *
from (select A.F1501,
             cast(concat(Format(A.F1502_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F1503, 1, 2), ':',
                         SUBSTRING(A.F1503, 3, 2)) as datetime)                                as F1502,
             A.F1504,
             A.F1505,
             A.F1506,
             A.F1507,
             A.F1508,
             A.F1509,
             A.F1510,
             A.F1511,
             A.F1512,
             A.F1513,
             A.F1514,
             A.Rec_Id,
             A.nahi,
             cast(concat(Format(A.F1507_Date_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F1507_Time, 1, 2), ':',
                         SUBSTRING(A.F1507_Time, 3, 2)) as datetime)                           as F1507_Date,
             case
                 when A.F1507_Ext_Time is not null then
                     cast(concat(Format(A.F1507_Ext_Date_M, 'yyyy/MM/dd'), ' ',
                                 SUBSTRING(A.F1507_Ext_Time, 1, 2), ':',
                                 SUBSTRING(A.F1507_Ext_Time, 3, 2)) as datetime)
                 else null end                                                                 as F1507_Ext_Date,
             A.Ent_Station,
             A.T_No_Pre,
             A.T_Date_Pre,
             A.T_Time_Pre,
             A.Nahi_Pre,
             A.AddLog_User,
             cast(concat(Format(A.AddLog_Date, 'yyyy/MM/dd'), ' ', A.AddLog_Time) as datetime) as AddLog_Date,
             A.AddLog_IP,
             A.EditLog_User,
             case
                 when A.EditLog_Time is not null then
                     cast(concat(Format(A.EditLog_Date, 'yyyy/MM/dd'), ' ', A.EditLog_Time) as datetime)
                 else null end                                                                 as EditLog_Date,
             A.EditLog_IP
      from [172.23.27.208].Nafis_Seir_Proj.dbo.F15 as A
               Left join MappingDB.dbo.F15 as B on B.Rec_ID = A.Rec_ID
      where A.AddLog_Date > getdate() - 1
        and B.Rec_ID is null) as v
where v.AddLog_Date >= getdate() - 0.011;

update MappingDB.dbo.F15
set F1501          = c.F1501,
    F1502          = c.F1502,
    F1504          = c.F1504,
    F1505          = c.F1505,
    F1506          = c.F1506,
    F1507          = c.F1507,
    F1508          = c.F1508,
    F1509          = c.F1509,
    F1510          = c.F1510,
    F1511          = c.F1511,
    F1512          = c.F1512,
    F1513          = c.F1513,
    F1514          = c.F1514,
    Rec_Id         = c.Rec_Id,
    nahi           = c.nahi,
    F1507_Date     = c.F1507_Date,
    F1507_Ext_Date = c.F1507_Ext_Date,
    Ent_Station    = c.Ent_Station,
    T_No_Pre       = c.T_No_Pre,
    T_Date_Pre     = c.T_Date_Pre,
    T_Time_Pre     = c.T_Time_Pre,
    Nahi_Pre       = c.Nahi_Pre,
    AddLog_User    = c.AddLog_User,
    AddLog_Date    = c.AddLog_Date,
    AddLog_IP      = c.AddLog_IP,
    EditLog_User   = c.EditLog_User,
    EditLog_Date   = c.EditLog_Date,
    EditLog_IP     = c.EditLog_IP
from MappingDB.dbo.F15 as d
         inner join (select *
                     from (select A.F1501,
                                  cast(concat(Format(A.F1502_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F1503, 1, 2), ':',
                                              SUBSTRING(A.F1503, 3, 2)) as datetime)                                as F1502,
                                  A.F1504,
                                  A.F1505,
                                  A.F1506,
                                  A.F1507,
                                  A.F1508,
                                  A.F1509,
                                  A.F1510,
                                  A.F1511,
                                  A.F1512,
                                  A.F1513,
                                  A.F1514,
                                  A.Rec_Id,
                                  A.nahi,
                                  cast(concat(Format(A.F1507_Date_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.F1507_Time, 1, 2),
                                              ':',
                                              SUBSTRING(A.F1507_Time, 3, 2)) as datetime)                           as F1507_Date,
                                  case
                                      when A.F1507_Ext_Time is not null then
                                          cast(concat(Format(A.F1507_Ext_Date_M, 'yyyy/MM/dd'), ' ',
                                                      SUBSTRING(A.F1507_Ext_Time, 1, 2), ':',
                                                      SUBSTRING(A.F1507_Ext_Time, 3, 2)) as datetime)
                                      else null end                                                                 as F1507_Ext_Date,
                                  A.Ent_Station,
                                  A.T_No_Pre,
                                  A.T_Date_Pre,
                                  A.T_Time_Pre,
                                  A.Nahi_Pre,
                                  A.AddLog_User,
                                  cast(concat(Format(A.AddLog_Date, 'yyyy/MM/dd'), ' ', A.AddLog_Time) as datetime) as AddLog_Date,
                                  A.AddLog_IP,
                                  A.EditLog_User,
                                  case
                                      when A.EditLog_Time is not null then
                                          cast(concat(Format(A.EditLog_Date, 'yyyy/MM/dd'), ' ', A.EditLog_Time) as datetime)
                                      else null end                                                                 as EditLog_Date,
                                  A.EditLog_IP
                           from [172.23.27.208].Nafis_Seir_Proj.dbo.F15 as A
                           where A.AddLog_Date > getdate() - 10) as v
                     where v.AddLog_Date > getdate() - 10) as c
                    on c.Rec_ID = d.Rec_ID and c.EditLog_Date <> d.EditLog_Date;

select *
from F15
where AddLog_Date > getdate() - 0.011
