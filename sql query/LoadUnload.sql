select *
into Traffic.dbo.capacity
         from (
 (select everydayLoading.station
                 , everydayLoading.freight
                 , max(everydayLoading.laodedWagon) as loadCapacity
                 , null as unloadCapacity
            from (
                     select f3102 as day, f3103 as station, f3104 as freight, sum(f3107) as laodedWagon
                     from LoadUnload.dbo.F31
                     group by f3102, f3103, f3104) as everydayLoading
            group by everydayLoading.station, everydayLoading.freight)
      union all
      (select everydayUnLoading.station,
              everydayUnLoading.freight,
              null as loadCapacity,
              max(everydayUnLoading.unlaodedWagon) as unloadCapacity
       from (
                select f3302 as day, f3303 as station, f3304 as freight, sum(f3305) as unlaodedWagon
                from LoadUnload.dbo.F33
                group by f3302, f3303, f3304) as everydayUnLoading
       group by everydayUnLoading.station, everydayUnLoading.freight)) as cap where freight is not null

