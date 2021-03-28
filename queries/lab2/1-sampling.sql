--Вывести все строки из журнала библиотекаря, дата возврата которых меньше некоторой даты.
SELECT * FROM JOURNAL WHERE DATE_RET < to_date('17-03-2021', 'dd-mm-yyyy');


--Посчитать количество книг, которых нет в наличии.
SELECT COUNT(*) AS BOOKS_NOT_IN_STOCK FROM BOOKS WHERE CNT=0;


--Вывести все книги типа «уникальные», которые на руках у читателей.
SELECT DISTINCT B.NAME FROM JOURNAL J
JOIN BOOKS B on B.ID = J.BOOK_ID
JOIN BOOK_TYPES BT on BT.ID = B.TYPE_ID
WHERE BT.ID=3 AND DATE_RET IS NULL;


--Вывести журнал библиотекаря, читателей, включая читателей, которые не брали книг и книги, включая книги, которых не выдавали.
SELECT * FROM JOURNAL J
FULL JOIN BOOKS B on B.ID = J.BOOK_ID
FULL JOIN CLIENTS C2 on C2.ID = J.CLIENT_ID;


--Число книг на руках у заданного клиента.
SELECT COUNT(*) AS N FROM JOURNAL WHERE CLIENT_ID=5 AND DATE_RET IS NULL;


--Размер штрафа заданного клиента.
SELECT SUM(DAYS * FINE) AS TOTAL_FINE FROM
(SELECT TRUNC(DATE_RET) - TRUNC(DATE_END) AS DAYS, FINE
    FROM JOURNAL J JOIN BOOKS B on B.ID = J.BOOK_ID JOIN BOOK_TYPES BT on BT.ID = B.TYPE_ID
    WHERE DATE_RET IS NOT NULL AND J.DATE_RET > J.DATE_END AND CLIENT_ID=5);


--Размер самого большого штрафа.
SELECT MAX(TOTAL_FINE) AS MAX_FINE FROM
(SELECT (TRUNC(DATE_RET) - TRUNC(DATE_END)) * FINE AS TOTAL_FINE
     FROM JOURNAL J JOIN BOOKS B on B.ID = J.BOOK_ID JOIN BOOK_TYPES BT on BT.ID = B.TYPE_ID
     WHERE DATE_RET IS NOT NULL AND J.DATE_RET > J.DATE_END);


--Три самые популярные книги.
SELECT B.ID, B.NAME FROM JOURNAL J JOIN BOOKS B on B.ID = J.BOOK_ID
GROUP BY B.ID, B.NAME ORDER BY COUNT(BOOK_ID) DESC FETCH FIRST 3 ROWS ONLY;
