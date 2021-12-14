SELECT [Dizel_No]
      ,[Ent_Date]
      ,[Ent_Date_Shamsi]
      ,[Ext_Date]
      ,[Ext_Date_Shamsi]
      ,[Code_Block_Location]
      ,[Dizel_MP]
      ,[BazsaziLocation]
      ,[Code_Bazsazi]
      ,[EntReason]
      ,[ExtReason]
      ,[Descript]
  FROM [DailyLoco].[dbo].[V_Bazsazi_ForTrafic] where [Ent_Date]='2021-11-08 00:00:00'
GO