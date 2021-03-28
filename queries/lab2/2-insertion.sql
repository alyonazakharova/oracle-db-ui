--Добавить нового клиента.
INSERT INTO CLIENTS (FIRST_NAME, LAST_NAME, FATHER_NAME, PASSPORT_SERIA, PASSPORT_NUM)
VALUES ('Ivan', 'Ivanov', 'Igorevich', '4321', '543298');


--Добавить в рамках транзакции клиента, книгу и запись в журнал библиотекаря о выдачи книги этому клиенту.
DECLARE
    insertedClientId NUMBER;
    insertedBookId NUMBER;
    insertedBookTypeId NUMBER;
    insertedBookDayCount NUMBER;
BEGIN
    INSERT INTO CLIENTS (FIRST_NAME, LAST_NAME, FATHER_NAME, PASSPORT_SERIA, PASSPORT_NUM)
    VALUES ('Иван', 'Петров', 'Иванович', '4010', '458374')
    RETURNING ID INTO insertedClientId;

    INSERT INTO BOOKS (NAME, CNT, TYPE_ID)
    VALUES ('Самоучитель по C++', 5, 1)
    RETURNING ID, TYPE_ID into insertedBookId, insertedBookTypeId;

    SELECT DAY_COUNT INTO insertedBookDayCount
    FROM BOOK_TYPES WHERE ID=insertedBookTypeId;

    INSERT INTO JOURNAL (BOOK_ID, CLIENT_ID, DATE_BEG, DATE_END)
    VALUES (insertedBookId, insertedClientId, current_date, current_date + insertedBookDayCount);

    UPDATE BOOKS SET CNT=CNT-1 WHERE ID=insertedBookId;

    COMMIT;
END;


--Добавить запись в журнал в случае, если книг у данного клиента больше 10, транзакцию откатить.
DECLARE
    clientId NUMBER;
    bookId NUMBER;
    bookDayCount NUMBER;
    booksNumber NUMBER;
BEGIN
    SAVEPOINT sp;

    clientId := 4;
    bookId :=7;

    SELECT DAY_COUNT INTO bookDayCount
    FROM BOOK_TYPES BT JOIN BOOKS B on BT.ID = B.TYPE_ID where B.ID=bookId;

    INSERT INTO JOURNAL (BOOK_ID, CLIENT_ID, DATE_BEG, DATE_END)
    VALUES (bookId, clientId, current_date, current_date + bookDayCount);

    UPDATE BOOKS SET CNT=CNT-1 WHERE ID=bookId;

    SELECT COUNT(*) INTO booksNumber FROM JOURNAL
    WHERE CLIENT_ID = clientId AND DATE_RET IS NULL ;
    IF (booksNumber > 10) THEN
        ROLLBACK TO sp;
    END IF;
    COMMIT;
END;