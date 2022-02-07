select Z.wagonPriId                                                                                   as wagonId,
       Z.F4601                                                                                        as wagonTypeFromF46,
       V.Wagon_Type                                                                                   as wagonTypeFromHis,
       X.F2201                                                                                        as currentStation,
       X.Next_St                                                                                      as nextStation,
       Z.F4611                                                                                        as originID,
       Z.F4612                                                                                        as destinationID,
       X.F2205                                                                                        as enterTime,
       X.F2207                                                                                        as exitTime,
       IIF(X.F2207 is not null, datediff(second, X.F2205, X.F2207), datediff(second, X.F2205, getdate())) as stopTime,
       Z.F4609                                                                                        as attachedStationID,
       Z.F4610                                                                                        as detachedStationID,
       Z.F4603                                                                                        as trainID,
       Z.F4604                                                                                        as trainFormationTime,
       Z.F4607                                                                                        as barID,
       Z.F4608                                                                                        as barnameh,
       Z.Rec_Id                                                                                       as F46rec_ID,
       X.Rec_Id                                                                                       as F22rec_ID,
       Z.nahi                                                                                         as nahieh
into Traffic.dbo.wagonStatus
from MappingDB.dbo.F46 as Z
         join MappingDB.dbo.F22 as X on X.F15Rec_ID = Z.F15Rec_ID
         join (select S.wagonPriId, max(concat(A.Rec_Id, s.rec_ID)) as latestRecord
               from MappingDB.dbo.F46 as S
                        inner join MappingDB.dbo.F22 as A on A.F15Rec_ID = S.F15Rec_ID
               where S.AddLog_Date > cast('2021-03-21 00:00:00' as datetime)
               group by S.wagonPriId) as D
              on D.latestRecord = concat(X.Rec_Id, Z.rec_ID) and Z.wagonPriId = D.wagonPriId
         full outer join Traffic.dbo.Wagon_History as v on Z.wagonPriId = V.wagonPriId
         inner join Traffic.dbo.wagonList as B on Z.wagonPriId = B.wagonPriId
where Z.AddLog_Date > cast('2021-03-21 00:00:00' as datetime);

insert into Traffic.dbo.wagonStatus
select Z.wagonPriId                                                                                   as wagonId,
       Z.F4601                                                                                        as wagonTypeFromF46,
       V.Wagon_Type                                                                                   as wagonTypeFromHis,
       X.F2201                                                                                        as currentStation,
       X.Next_St                                                                                      as nextStation,
       Z.F4611                                                                                        as originID,
       Z.F4612                                                                                        as destinationID,
       X.F2205                                                                                        as enterTime,
       X.F2207                                                                                        as exitTime,
       IIF(X.F2207 is not null, datediff(second, X.F2205, X.F2207), datediff(second, X.F2205, getdate())) as stopTime,
       Z.F4609                                                                                        as attachedStationID,
       Z.F4610                                                                                        as detachedStationID,
       Z.F4603                                                                                        as trainID,
       Z.F4604                                                                                        as trainFormationTime,
       Z.F4607                                                                                        as barID,
       Z.F4608                                                                                        as barnameh,
       Z.Rec_Id                                                                                       as F46rec_ID,
       X.Rec_Id                                                                                       as F22rec_ID,
       Z.nahi                                                                                         as nahieh
from MappingDB.dbo.F46 as Z
         join MappingDB.dbo.F22 as X on X.F15Rec_ID = Z.F15Rec_ID
         join (select S.wagonPriId, max(concat(A.Rec_Id, s.rec_ID)) as latestRecord
               from MappingDB.dbo.F46 as S
                        inner join MappingDB.dbo.F22 as A on A.F15Rec_ID = S.F15Rec_ID
               where S.AddLog_Date >= getdate() - 0.011
               group by S.wagonPriId) as D
              on D.latestRecord = concat(X.Rec_Id, Z.rec_ID) and Z.wagonPriId = D.wagonPriId
         full outer join Traffic.dbo.Wagon_History as v on Z.wagonPriId = V.wagonPriId
         inner join Traffic.dbo.wagonList as B on Z.wagonPriId = B.wagonPriId
         left join Traffic.dbo.wagonStatus as W on Z.wagonPriId = W.wagonId
where W.wagonId is null
  and Z.AddLog_Date >= getdate() - 0.011;

update Traffic.dbo.wagonStatus
set wagonId            = c.wagonId,
    wagonTypeFromF46   = c.wagonTypeFromF46,
    wagonTypeFromHis   = c.wagonTypeFromHis,
    currentStation     = c.currentStation,
    nextStation        = c.nextStation,
    originID           = c.originID,
    destinationID      = c.destinationID,
    enterTime          = c.enterTime,
    exitTime           = c.exitTime,
    stopTime           = c.stopTime,
    attachedStationID  = c.attachedStationID,
    detachedStationID  = c.detachedStationID,
    trainID            = c.trainID,
    trainFormationTime = c.trainFormationTime,
    barID              = c.barID,
    barnameh           = c.barnameh,
    F46rec_ID          = c.F46rec_ID,
    F22rec_ID          = c.F22rec_ID,
    nahieh             = c.nahieh
from Traffic.dbo.wagonStatus as q
         inner join (select Z.wagonPriId                            as wagonId,
                            Z.F4601                                 as wagonTypeFromF46,
                            V.Wagon_Type                            as wagonTypeFromHis,
                            X.F2201                                 as currentStation,
                            X.Next_St                               as nextStation,
                            Z.F4611                                 as originID,
                            Z.F4612                                 as destinationID,
                            X.F2205                                 as enterTime,
                            X.F2207                                 as exitTime,
                            IIF(X.F2207 is not null, datediff(second, X.F2205, X.F2207),
                                datediff(second, X.F2205, getdate())) as stopTime,

                            Z.F4609                                 as attachedStationID,
                            Z.F4610                                 as detachedStationID,
                            Z.F4603                                 as trainID,
                            Z.F4604                                 as trainFormationTime,
                            Z.F4607                                 as barID,
                            Z.F4608                                 as barnameh,
                            Z.Rec_Id                                as F46rec_ID,
                            X.Rec_Id                                as F22rec_ID,
                            Z.nahi                                  as nahieh
                     from MappingDB.dbo.F46 as Z
                              join MappingDB.dbo.F22 as X on X.F15Rec_ID = Z.F15Rec_ID
                              join (select S.wagonPriId, max(concat(A.Rec_Id, s.rec_ID)) as latestRecord
                                    from MappingDB.dbo.F46 as S
                                             inner join MappingDB.dbo.F22 as A on A.F15Rec_ID = S.F15Rec_ID
                                    where S.AddLog_Date >= cast('2021-03-21 00:00:00' as datetime)
                                    group by S.wagonPriId) as D
                                   on D.latestRecord = concat(X.Rec_Id, Z.rec_ID) and Z.wagonPriId = D.wagonPriId
                              full outer join Traffic.dbo.Wagon_History as v on Z.wagonPriId = V.wagonPriId
                              inner join Traffic.dbo.wagonList as B on Z.wagonPriId = B.wagonPriId
                         and Z.AddLog_Date >= cast('2021-03-21 00:00:00' as datetime)) as c
                    on c.wagonId = q.wagonId and (c.currentStation <> q.currentStation or
                                                  q.wagonTypeFromF46 <> c.wagonTypeFromF46 or
                                                  q.wagonTypeFromHis <> c.wagonTypeFromHis or
                                                  q.currentStation <> c.currentStation or
                                                  q.nextStation <> c.nextStation or
                                                  q.originID <> c.originID or
                                                  q.destinationID <> c.destinationID or
                                                  q.enterTime <> c.enterTime or
                                                  q.exitTime <> c.exitTime or
                                                  q.attachedStationID <> c.attachedStationID or
                                                  q.detachedStationID <> c.detachedStationID or
                                                  q.trainID <> c.trainID or
                                                  q.trainFormationTime <> c.trainFormationTime or
                                                  q.barID <> c.barID or
                                                  q.barnameh <> c.barnameh or
                                                  q.F46rec_ID <> c.F46rec_ID or
                                                  q.F22rec_ID <> c.F22rec_ID or
                                                  q.nahieh <> c.nahieh);