create Function dbo.seirDateToDatetime(@day1 char(10), @time1 char(10)) returns datetime as
begin return (CONVERT(datetime, concat(convert(varchar, dbo.JalaliToGregorian(concat(SUBSTRING(@day1, 1, 2), '-',
                                                                                  SUBSTRING(@day1, 3, 2), '-',
                                                                                  SUBSTRING(@day1, 5, 2)), '-'),
                                                     1), concat(' ', SUBSTRING(@time1, 1, 2), ':',
                                                                SUBSTRING(@time1, 3, 2))))) end;