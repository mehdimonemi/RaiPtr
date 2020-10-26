/************************************************************
 * Code formatted by SoftTree SQL Assistant © v11.0.35
 * Time: 02/04/1399 14:52:18
 ************************************************************/

SELECT b.tag
      ,b.regin
      ,b.code
      ,b.upcode
      ,b.Position_Map
      ,b.downcode
      ,b.[type]
      ,b.[len]
      ,b.name
      ,b.no
      ,b.total_len
      ,b.nahie_code
      ,b.TrnLen
      ,b.Visable
      ,b.Line_Count
      ,b.Line_Count_Upper
      ,ROW_NUMBER() OVER(
           PARTITION tag
          ,regin
          ,code
          ,upcode
          ,Position_Map
          ,downcode
          ,[type]
          ,[len]
          ,name
          ,no
          ,total_len
          ,nahie_code
          ,TrnLen
          ,Visable
          ,Line_Count
          ,Line_Count_Upper
       )                   row_num
FROM   graph.dbo.Block  AS b

WITH CTE(tag
          ,regin
          ,code
          ,upcode
          ,Position_Map
          ,downcode
          ,[type]
          ,[len]
          ,name
          ,no
          ,total_len
          ,nahie_code
          ,TrnLen
          ,Visable
          ,Line_Count
          ,Line_Count_Upper)
AS (SELECT tag
          ,regin
          ,code
          ,upcode
          ,Position_Map
          ,downcode
          ,[type]
          ,[len]
          ,name
          ,no
          ,total_len
          ,nahie_code
          ,TrnLen
          ,Visable
          ,Line_Count
          ,Line_Count_Upper 
           ROW_NUMBER() OVER(PARTITION BY tag
          ,regin
          ,code
          ,upcode
          ,Position_Map
          ,downcode
          ,[type]
          ,[len]
          ,name
          ,no
          ,total_len
          ,nahie_code
          ,TrnLen
          ,Visable
          ,Line_Count
          ,Line_Count_Upper
           ORDER BY code) AS DuplicateCount
    FROM graph)
SELECT *
FROM CTE;