Select Q.Rec_ID, Q.Tools_No, Q.Nahi, Q.Status_Datetime, Q.Status_St
from MappingDB.dbo.Tools_Status_OP as Q
         join (Select Rec_Id, F1504, a.kind
               from MappingDB.dbo.F15
                        Inner join Traffic.dbo.P8 as a on a.CODE = F1504) as W on W.Rec_Id = Q.F15Rec_ID
         join (select Tools_No, Nahi, max(Status_Datetime) as Status_Datetime
               from MappingDB.dbo.Tools_Status_OP
               group by Tools_No, Nahi) as E
              on concat(E.Tools_No, E.Nahi, E.Status_Datetime) = concat(Q.Tools_No, Q.Nahi, Q.Status_Datetime)
where W.kind <> 3

select W.Tools_No, R.[DESC], E.F2205, E.F2207, T.[DESC], T
from MappingDB.dbo.F15 as Q
         join (select * from MappingDB.dbo.Tools_Status_OP where Tools_No = 2) as W on W.F15Rec_ID = Q.Rec_Id
         join MappingDB.dbo.f22 as E on E.F15Rec_ID = Q.Rec_Id
         join Traffic.dbo.Stations as R on R.CODE = E.F2201
         join Traffic.dbo.P8 as T on T.CODE = Q.F1504
         join MappingDB.dbo.F24 as Y on Y.F15Rec_ID = Q.Rec_Id
where T.kind <> 3
order by E.F2205