-- Создать представление, отображающее все книги и читателей,
-- о которых найдены записи в журнале с заданной даты по заданную дату.
CREATE OR REPLACE VIEW view_1 AS
SELECT J.ID, B.NAME, C2.LAST_NAME, C2.FIRST_NAME, C2.FATHER_NAME, J.DATE_BEG
FROM JOURNAL J
         JOIN BOOKS B ON B.ID = J.BOOK_ID
         JOIN CLIENTS C2 ON C2.ID = J.CLIENT_ID
WHERE J.DATE_BEG BETWEEN to_date('01-03-2021', 'dd-mm-yyyy') AND to_date('31-03-2021', 'dd-mm-yyyy');


-- Создать представление, отображающее всех читателей и количество книг, находящихся у них на руках.
CREATE OR REPLACE VIEW view_2 AS
SELECT ID, FIRST_NAME, LAST_NAME, s1.BOOKS
FROM CLIENTS JOIN
     (SELECT CLIENT_ID, COUNT(CLIENT_ID) AS BOOKS
      FROM JOURNAL WHERE DATE_RET IS NULL GROUP BY CLIENT_ID) s1
         ON s1.CLIENT_ID=CLIENTS.ID
UNION
SELECT ID, FIRST_NAME, LAST_NAME, 0 AS BOOKS FROM CLIENTS
WHERE ID NOT IN
      (SELECT CLIENT_ID FROM JOURNAL WHERE DATE_RET IS NULL);
