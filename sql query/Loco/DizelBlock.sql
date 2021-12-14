SELECT [Dizel_No]
      ,[Ent_Date]
      ,[Ent_Date_Shamsi]
      ,[Code_Area]
      ,[Code_DEPO]
      ,[Code_BLOCK]
      ,[COLDTIME]
      ,[STOPTIME]
      ,[SOS_Dizel]
      ,[EMDAD_DizelNo1]
      ,[EMDAD_DizelNo2]
      ,[Code_Train_Kind]
      ,[Train_No]
      ,[No_Moaser]
      ,[AreaDes]
      ,[DepoDes]
      ,[TrainKindDes]
      ,[BlockDes]
  FROM [DailyLoco].[dbo].[V_DizelBlock_ForTrafic] where [Ent_Date]='2021-11-08 00:00:00'
GO


