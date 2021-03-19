-- Создать хранимую процедуру, выводящую все книги и среднее время, на которое их брали, в днях.
CREATE OR REPLACE PROCEDURE AVG_DAYS AS
    CURSOR booksAndTime IS
        SELECT ID, NAME, AVG(DAYS) AS DAYS FROM
            (SELECT B.ID, NAME, AVG(trunc(DATE_RET)-trunc(DATE_BEG)) AS DAYS
             FROM BOOKS B LEFT JOIN JOURNAL J ON B.ID = J.BOOK_ID
             WHERE DATE_RET IS NOT NULL GROUP BY B.ID, NAME
             UNION
             SELECT B.ID, NAME, AVG(trunc(current_date)-trunc(DATE_BEG)) AS DAYS
             FROM BOOKS B left join JOURNAL J ON B.ID = J.BOOK_ID
             WHERE DATE_RET IS NULL AND DATE_BEG IS NOT NULL GROUP BY B.ID, NAME
             UNION
             -- книги, которые вообще не брали
             SELECT B.ID, NAME, 0 AS DAYS FROM BOOKS B LEFT JOIN JOURNAL J ON B.ID = J.BOOK_ID
             WHERE DATE_BEG IS NULL GROUP BY B.ID, NAME)
        GROUP BY ID, NAME;
    bookId NUMBER;
    bookName VARCHAR2(50);
    daysNum NUMBER;
BEGIN
    OPEN booksAndTime;
    LOOP
        FETCH booksAndTime INTO bookId, bookName, daysNum;
        IF booksAndTime%NOTFOUND THEN
            EXIT;
        ELSE
            DBMS_OUTPUT.PUT_LINE('ID = ' || bookId || ' NAME = ' || bookName || ' DAYS = ' || daysNum);
        END IF;
    END LOOP;
    CLOSE booksAndTime;
END;



-- Создать хранимую процедуру, имеющую два параметра «книга1» и «книга2».
-- Она должна возвращать клиентов, которые вернули «книгу1» быстрее чем «книгу2».
-- Если какой-либо клиент не брал одну из книг – он не рассматривается.
CREATE OR REPLACE PROCEDURE WHO_RETURNED_FIRST(book1 IN NUMBER, book2 IN NUMBER) AS
    CURSOR clientsWhoReturnedFirst IS
        SELECT s1.CLIENT_ID FROM
            (SELECT CLIENT_ID, DATE_RET FROM JOURNAL WHERE BOOK_ID=book1) s1
                JOIN
            (SELECT CLIENT_ID, DATE_RET FROM JOURNAL WHERE BOOK_ID=book2) s2
            ON s1.CLIENT_ID=s2.CLIENT_ID
        WHERE s1.DATE_RET < s2.DATE_RET OR (s1.DATE_RET IS NOT NULL AND s2.DATE_RET IS NULL)
        GROUP BY s1.CLIENT_ID;
    clientId NUMBER;
BEGIN
    OPEN clientsWhoReturnedFirst;
    LOOP
        FETCH clientsWhoReturnedFirst INTO clientId;
        IF clientsWhoReturnedFirst%NOTFOUND THEN
            EXIT;
        ELSE
            DBMS_OUTPUT.PUT_LINE('CLIENT_ID = ' || clientId);
        END IF;
    END LOOP;
    CLOSE clientsWhoReturnedFirst;
END;


-- Создать хранимую процедуру с входным параметром «книга» и двумя выходными параметрами,
-- возвращающими самое большое время, на которое брали книгу, и читателя, поставившего рекорд.
CREATE OR REPLACE PROCEDURE MAX_TIME(bookId IN NUMBER, time OUT NUMBER, clientId OUT NUMBER) IS
BEGIN
    SELECT CLIENT_ID, DAYS INTO clientId, time FROM
        --количество дней для тех, кто уже вернул книгу
        (SELECT CLIENT_ID, (TRUNC(DATE_RET)-TRUNC(DATE_BEG)) AS DAYS
         FROM JOURNAL WHERE BOOK_ID=bookId AND DATE_RET IS NOT NULL
         UNION
         -- + количество дней для тех, кто ее ещё не вернул -> считаем до сегодняшней даты
         SELECT CLIENT_ID, (TRUNC(current_date)-TRUNC(DATE_BEG)) AS DAYS
         FROM JOURNAL WHERE BOOK_ID=bookId AND DATE_RET IS NULL)
    ORDER BY DAYS DESC FETCH FIRST 1 ROW ONLY;
END;
