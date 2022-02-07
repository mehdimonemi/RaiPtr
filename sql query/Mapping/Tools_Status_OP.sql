select A.Rec_ID,
       A.Tools_Kind,
       A.Tools_No,
       A.T_NO,
       cast(concat(Format(A.T_Date_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.T_Time, 1, 2), ':',
                   SUBSTRING(A.T_Time, 3, 2)) as datetime) as T_Datetime,
       A.Nahi,
       case
           when A.STATUS_Time != '' then
               cast(concat(Format(A.Status_Date_M, 'yyyy/MM/dd'), ' ',
                           SUBSTRING(A.STATUS_Time, 1, 2), ':', SUBSTRING(A.STATUS_Time, 3, 2)) as datetime)
           else null end                                   as Status_Datetime,
       A.Status_St,
       A.Status_Code,
       A.Block_Code,
       A.KM,
       A.Move_Counter,
       A.Tools_Desc,
       A.F46Rec_ID,
       A.F15Rec_ID,
       A.F22Rec_Id,
       A.AddLog_User,
       A.AddLog_Date,
       A.AddLog_IP,
       A.EditLog_User,
       A.EditLog_Date,
       A.EditLog_IP
into MappingDB.dbo.Tools_Status_OP
from [172.23.27.208].Nafis_Seir_Proj.dbo.Tools_Status_OP as A
where A.AddLog_Date >= cast('2021-03-21 00:00:00' as datetime)

--insert new entries every minute
insert into MappingDB.dbo.Tools_Status_OP
select A.Rec_ID,
       A.Tools_Kind,
       A.Tools_No,
       A.T_NO,
       cast(concat(Format(A.T_Date_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.T_Time, 1, 2), ':',
                   SUBSTRING(A.T_Time, 3, 2)) as datetime) as T_Datetime,
       A.Nahi,
       case
           when A.STATUS_Time != '' then
               cast(concat(Format(A.Status_Date_M, 'yyyy/MM/dd'), ' ',
                           SUBSTRING(A.STATUS_Time, 1, 2), ':', SUBSTRING(A.STATUS_Time, 3, 2)) as datetime)
           else null end                                   as Status_Datetime,
       A.Status_St,
       A.Status_Code,
       A.Block_Code,
       A.KM,
       A.Move_Counter,
       A.Tools_Desc,
       A.F46Rec_ID,
       A.F15Rec_ID,
       A.F22Rec_Id,
       A.AddLog_User,
       A.AddLog_Date,
       A.AddLog_IP,
       A.EditLog_User,
       A.EditLog_Date,
       A.EditLog_IP
from [172.23.27.208].Nafis_Seir_Proj.dbo.Tools_Status_OP as A
         Left join MappingDB.dbo.Tools_Status_OP as B on B.Rec_ID = A.Rec_ID
where A.AddLog_Date >= getdate() - 0.011
  and B.Rec_ID is null;

--updating the table every minute
update MappingDB.dbo.Tools_Status_OP
set Rec_ID          = c.Rec_ID,
    Tools_Kind      = c.Tools_Kind,
    Tools_No        = c.Tools_No,
    T_NO            = c.T_NO,
    T_Datetime      = c.T_Datetime,
    Nahi            = c.Nahi,
    Status_Datetime = c.Status_Datetime,
    Status_St       = c.Status_St,
    Status_Code     = c.Status_Code,
    Block_Code      = c.Block_Code,
    KM              = c.KM,
    Move_Counter    = c.Move_Counter,
    Tools_Desc      = c.Tools_Desc,
    F46Rec_ID       = c.F46Rec_ID,
    F15Rec_ID       = c.F15Rec_ID,
    F22Rec_Id       = c.F22Rec_Id,
    AddLog_User     = c.AddLog_User,
    AddLog_Date     = c.AddLog_Date,
    AddLog_IP       = c.AddLog_IP,
    EditLog_User    = c.EditLog_User,
    EditLog_Date    = c.EditLog_Date,
    EditLog_IP      = c.EditLog_IP
from MappingDB.dbo.Tools_Status_OP as d
         inner join (select A.Rec_ID,
                            A.Tools_Kind,
                            A.Tools_No,
                            A.T_NO,
                            cast(concat(Format(A.T_Date_M, 'yyyy/MM/dd'), ' ', SUBSTRING(A.T_Time, 1, 2), ':',
                                        SUBSTRING(A.T_Time, 3, 2)) as datetime) as T_Datetime,
                            A.Nahi,
                            case
                                when A.STATUS_Time != '' then
                                    cast(concat(Format(A.Status_Date_M, 'yyyy/MM/dd'), ' ',
                                                SUBSTRING(A.STATUS_Time, 1, 2), ':',
                                                SUBSTRING(A.STATUS_Time, 3, 2)) as datetime)
                                else null end                                   as Status_Datetime,
                            A.Status_St,
                            A.Status_Code,
                            A.Block_Code,
                            A.KM,
                            A.Move_Counter,
                            A.Tools_Desc,
                            A.F46Rec_ID,
                            A.F15Rec_ID,
                            A.F22Rec_Id,
                            A.AddLog_User,
                            A.AddLog_Date,
                            A.AddLog_IP,
                            A.EditLog_User,
                            A.EditLog_Date,
                            A.EditLog_IP
                     from [172.23.27.208].Nafis_Seir_Proj.dbo.Tools_Status_OP as A
                     where A.AddLog_Date > getdate() - 10) as c
                    on c.Rec_ID = d.Rec_ID and c.EditLog_Date <> d.EditLog_Date;

select *
from [172.23.27.208].Nafis_Seir_Proj.dbo.Tools_Status_OP
where      Rec_ID = 6711456

update MappingDB.dbo.Tools_Status_OP
set Status_Datetime = cast('2021-11-06 00:15:00.000' as datetime)
where Rec_ID=6711456;