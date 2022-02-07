select *
into Traffic.dbo.Dizel_Owner
from [172.23.27.208].Nafis_Seir_Proj.dbo.Dizel_Owner;

select *
into Traffic.dbo.Kala
from [172.23.27.208].Nafis_Seir_Proj.dbo.Kala
where is_active = 'true';

select *
into Traffic.dbo.kargozaran
from [172.23.27.208].Nafis_Seir_Proj.dbo.kargozaran
where is_active = 'true';

select *
into Traffic.dbo.Nahi
from [172.23.27.208].Nafis_Seir_Proj.dbo.Nahi;

select *
into Traffic.dbo.OWNNAME
from [172.23.27.208].Nafis_Seir_Proj.dbo.OWNNAME;

--wagon types
select *
into Traffic.dbo.P5
from [172.23.27.208].Nafis_Seir_Proj.dbo.P5
where is_active = 'true';

--train types
select *
into Traffic.dbo.P8
from [172.23.27.208].Nafis_Seir_Proj.dbo.P8
where isactive = 'true';

--stop reasons
select *
into Traffic.dbo.P31
from [172.23.27.208].Nafis_Seir_Proj.dbo.P31
where is_active = 'true';

--stop reason groups
select *
into Traffic.dbo.Status_Main_Group
from [172.23.27.208].Nafis_Seir_Proj.dbo.Status_Main_Group;

--wagon types
select *
into Traffic.dbo.Wagon_Type
from [172.23.27.208].Nafis_Seir_Proj.dbo.Wagon_Type
where isActive = 'true';

--wagon owners
select *
into Traffic.dbo.Wagon_Owner
from [172.23.27.208].Nafis_Seir_Proj.dbo.Wagon_Owner;

--Locomotive status
select *
into Traffic.dbo.Tools_Status
from [172.23.27.208].Nafis_Seir_Proj.dbo.Tools_Status
where is_active = 'true';

--wagon history
select (case
            when (A.sixId is not null or A.sixId <> 0) then A.sixId
            else F.Mms_Fld_pk_SerialNo end)                                                       as wagonPriId,
       F.*,
       case
           when ((Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 1, 1) * 2 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 2, 1) * 7 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 3, 1) * 6 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 4, 1) * 5 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 5, 1) * 4 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 6, 1) * 3) % 11) = 0 then 1
           when ((Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 1, 1) * 2 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 2, 1) * 7 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 3, 1) * 6 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 4, 1) * 5 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 5, 1) * 4 +
                  Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 6, 1) * 3) % 11) = 1 then 0
           else 11 - ((Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 1, 1) * 2 +
                       Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 2, 1) * 7 +
                       Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 3, 1) * 6 +
                       Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 4, 1) * 5 +
                       Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 5, 1) * 4 +
                       Substring(cast(F.Mms_Fld_pk_SerialNo as varchar(50)), 6, 1) * 3) % 11) end as Mms_Fld_AutoControl
into Traffic.dbo.Wagon_History
from [172.23.27.208].Nafis_Seir_Proj.dbo.Wagon_History as F
         left join (select B.sixId
                    from (select dbo.to6WagonID(F4602) as sixId
                          from [172.23.27.208].Nafis_Seir_Proj.dbo.F46
                          where AddLog_Date >= cast('2021-03-21 00:00:00' as datetime)
                         ) As B
                    group by sixId) as A
                   on A.sixId = F.Mms_Fld_pk_SerialNo
where (F.toDate is null
    or F.toDate = '')
  and F.Wagon_Type <> 20
