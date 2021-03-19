-- Хранимая процедура для расчета суммы штрафов библиотеки
CREATE OR REPLACE PROCEDURE fine_sum(date1 IN DATE, date2 IN DATE, res OUT NUMBER) AS
    CURSOR finesInPeriod IS
        SELECT ((TRUNC(DATE_RET) - TRUNC(DATE_END)) * FINE) FROM
        (SELECT BOOK_ID, DATE_END, DATE_RET
        FROM JOURNAL WHERE TRUNC(DATE_RET) BETWEEN date1 AND date2) s1
        JOIN BOOKS B ON B.ID=s1.BOOK_ID JOIN BOOK_TYPES BT ON BT.ID = B.TYPE_ID
        WHERE DATE_RET > DATE_END;
    currentFine NUMBER;
BEGIN
    OPEN finesInPeriod;
    res := 0;
    LOOP
        FETCH finesInPeriod INTO currentFine;
        IF finesInPeriod%NOTFOUND THEN
            EXIT ;
        ELSE
            res := res + currentFine;
        END IF;
    END LOOP;
    CLOSE finesInPeriod;
END;


-- -- Хранимая процедура для расчета трех самых популярных книг
CREATE OR REPLACE PROCEDURE TOP_3_BOOKS(date1 IN DATE, date2 IN DATE) AS
    CURSOR topBooks IS
        SELECT B.NAME FROM JOURNAL J JOIN BOOKS B ON B.ID = J.BOOK_ID
        WHERE TRUNC(DATE_BEG) BETWEEN date1 AND date2
        GROUP BY B.NAME ORDER BY COUNT(J.BOOK_ID) DESC FETCH FIRST 3 ROWS ONLY;
    bookName VARCHAR2(50);
BEGIN
    OPEN topBooks;
    LOOP
        FETCH topBooks INTO bookName;
        IF topBooks%NOTFOUND THEN
            EXIT;
        ELSE
            DBMS_OUTPUT.PUT_LINE(bookName);
        END IF;
    END LOOP;
    CLOSE topBooks;
END;



CREATE OR REPLACE PROCEDURE TOP_3_BOOKS_IN_PERIOD(date1 IN DATE, date2 IN DATE, res OUT SYS_REFCURSOR) AS
BEGIN
    OPEN res FOR
    SELECT B.NAME FROM JOURNAL J JOIN BOOKS B ON B.ID = J.BOOK_ID
    WHERE TRUNC(DATE_BEG) BETWEEN date1 AND date2
    GROUP BY B.NAME ORDER BY COUNT(J.BOOK_ID) DESC FETCH FIRST 3 ROWS ONLY;
END;
