select *
-- into Traffic.dbo.Stations
from [172.23.27.208].Nafis_Seir_Proj.dbo.p2
where is_active = 'true'
  and nahi >= 7
  and 'true' = case
                   WHEN (code = 135 and nahi <> 8) then 'false'--Special Condition for 'تهران'
                   when (position_map = 1 and tagend = 'true') then 'true'
                   when (position_map = 1 and tagend = 'false') then 'false'
                   else 'true'
    end


select * from Stations where code =336;