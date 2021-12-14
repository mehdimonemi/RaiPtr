--insert new entries every minute
insert into MappingDB.dbo.Dizel_OP
select A.Rec_ID,
       A.Dizel_no,
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
       A.Is_Cold,
       A.KM,
       A.Status_Code,
       A.Block_Code,
       A.Move_Counter,
       A.Dizel_Desc,
       A.F46Rec_ID,
       A.F15Rec_ID,
       A.F22Rec_Id,
       A.AddLog_User,
       A.AddLog_Date,
       A.AddLog_IP,
       A.EditLog_User,
       A.EditLog_Date,
       A.EditLog_IP
from [172.23.27.208].Nafis_Seir_Proj.dbo.Dizel_OP as A
         Left join MappingDB.dbo.Dizel_OP as B on B.Rec_ID = A.Rec_ID
where A.AddLog_Date > getdate() - 0.011
  and B.Rec_ID is null;

--updating the table every minute
update MappingDB.dbo.Dizel_OP
set Rec_ID          = c.Rec_ID,
    Dizel_no        = c.Dizel_no,
    T_NO            = c.T_NO,
    T_Datetime      = c.T_Datetime,
    Status_Datetime = c.Status_Datetime,
    Is_Cold         = c.Is_Cold,
    KM              = c.KM,
    Status_Code     = c.Status_Code,
    Block_Code      = c.Block_Code,
    Move_Counter    = c.Move_Counter,
    Dizel_Desc      = c.Dizel_Desc,
    F46Rec_ID       = c.F46Rec_ID,
    F15Rec_ID       = c.F15Rec_ID,
    F22Rec_Id       = c.F22Rec_Id,
    AddLog_User     = c.AddLog_User,
    AddLog_Date     = c.AddLog_Date,
    AddLog_IP       = c.AddLog_IP,
    EditLog_User    = c.EditLog_User,
    EditLog_Date    = c.EditLog_Date,
    EditLog_IP      = c.EditLog_IP
from MappingDB.dbo.Dizel_OP as d
         inner join (select A.Rec_ID,
                            A.Dizel_no,
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
                            A.Is_Cold,
                            A.KM,
                            A.Status_Code,
                            A.Block_Code,
                            A.Move_Counter,
                            A.Dizel_Desc,
                            A.F46Rec_ID,
                            A.F15Rec_ID,
                            A.F22Rec_Id,
                            A.AddLog_User,
                            A.AddLog_Date,
                            A.AddLog_IP,
                            A.EditLog_User,
                            A.EditLog_Date,
                            A.EditLog_IP
                     from [172.23.27.208].Nafis_Seir_Proj.dbo.Dizel_OP as A
                     where A.AddLog_Date > getdate() - 10) as c
                    on c.Rec_ID = d.Rec_ID and c.EditLog_Date <> d.EditLog_Date;


select * from Dizel_OP where AddLog_Date > getdate()-0.011