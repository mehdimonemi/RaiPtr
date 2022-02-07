select *
into MappingDB.dbo.Bar
from (select A.NO_VAG_
           , A.Barnameh_ID
           , dbo.JalaliToGregorian(concat(substring(A.BARDATE_, 1, 4), '/',
                                          substring(A.BARDATE_, 5, 2), '/',
                                          substring(A.BARDATE_, 7, 2)), '/') +
             cast(A.AddLog_Time as datetime) as BarDate_
           , A.NOKALA_
           , A.Disc1
           , A.mabda_Code
           , A.mabda_Disc
           , A.Seir_mabda_Code
           , A.Maghsd_Code
           , A.Maghsd_Disc
           , A.Seir_Maghsad_Code
           , A.VAZN_C_
           , A.VAZN_R_
      from [172.23.27.214].Bar.dbo.V_Bar_4_trafik as A) as v
where v.BARDATE_ >= cast('2021-03-21 00:00:00' as datetime);

declare @today int;
set @today = cast(dbo.GregorianToJalali(getdate(), 'yyyyMMdd') as int);
select @today;
insert into MappingDB.dbo.Bar
select *
from (select A.NO_VAG_
           , A.Barnameh_ID
           , dbo.JalaliToGregorian(concat(substring(A.BARDATE_, 1, 4), '/',
                                          substring(A.BARDATE_, 5, 2), '/',
                                          substring(A.BARDATE_, 7, 2)), '/') +
             cast(A.AddLog_Time as datetime) as BarDate_
           , A.NOKALA_
           , A.Disc1
           , A.mabda_Code
           , A.mabda_Disc
           , A.Seir_mabda_Code
           , A.Maghsd_Code
           , A.Maghsd_Disc
           , A.Seir_Maghsad_Code
           , A.VAZN_C_
           , A.VAZN_R_
      from [172.23.27.214].Bar.dbo.V_Bar_4_trafik as A
               Left join MappingDB.dbo.Bar as B on B.Barnameh_ID = A.Barnameh_ID
      where A.BARDATE_ >= @today
        and B.Barnameh_ID is null) as v;