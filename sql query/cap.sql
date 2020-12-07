DECLARE @startDate INTEGER;
DECLARE @endDate INTEGER;
SET @endDate = 981032;
SET @startDate = 981000;
select *,
       MAX(
                       (
                               (CONVERT(INT, SUBSTRING(d.F2205, 1, 2)) - 98) * 365 * 24 * 60 + (
                               CASE
                                   WHEN CONVERT(INT, SUBSTRING(d.F2205, 3, 2)) <= 6 THEN
                                           (CONVERT(INT, SUBSTRING(d.F2205, 3, 2)) - 1)
                                           *
                                           31
                                   WHEN CONVERT(INT, SUBSTRING(d.F2205, 3, 2)) = 12 THEN 6 * 31 + 5 * 30
                                   ELSE 6 * 31 + (CONVERT(INT, SUBSTRING(d.F2205, 3, 2)) - 7) * 30
                                   END
                               ) + (CONVERT(INT, SUBSTRING(d.F2205, 5, 2)) - 1)
                           ) * 24 * 60 +
                       CONVERT(INT, SUBSTRING(d.F2206, 1, 2)) * 60 +
                       CONVERT(INT, SUBSTRING(d.F2206, 3, 2))
           ) AS maxEnterTime
from (
    ;
DECLARE @startDate INTEGER;
DECLARE @endDate INTEGER;
SET @endDate = 981032;
SET @startDate = 981000;
select F4602,
       MAX(((CONVERT(INT, SUBSTRING(F4604, 1, 2)) - 98) * 365 * 24 * 60 + (
           CASE
               WHEN CONVERT(INT, SUBSTRING(F4604, 3, 2)) <= 6 THEN
                       (CONVERT(INT, SUBSTRING(F4604, 3, 2)) - 1)
                       *
                       31
               WHEN CONVERT(INT, SUBSTRING(F4604, 3, 2)) = 12 THEN 6 * 31 + 5 * 30
               ELSE 6 * 31 + (CONVERT(INT, SUBSTRING(F4604, 3, 2)) - 7) * 30
               END
           ) + (CONVERT(INT, SUBSTRING(F4604, 5, 2)) - 1)
               ) * 24 * 60 +
           CONVERT(INT, SUBSTRING(F4605, 1, 2)) * 60 +
           CONVERT(INT, SUBSTRING(F4605, 3, 2))
           ) AS lastTrainTime
from graph.dbo.F46
where (F4604 < @endDate AND F4604 > @startDate) and (f4601 = 10 OR f4601 = 24 OR f4601 = 30)
group by F4602

) as f46

inner join graph.dbo.f24 as f24
on f24.F15Rec_ID=f46.F15Rec_ID
where