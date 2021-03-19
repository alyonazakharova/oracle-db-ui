--Удалить книги, не имеющие ссылок из записей в журнале.
DELETE FROM BOOKS B
WHERE NOT EXISTS(SELECT * FROM JOURNAL J WHERE J.BOOK_ID=B.ID);


--1. Удалить в рамках транзакции книгу и записи о ее выдаче.
DECLARE
    bookId NUMBER;
BEGIN
    bookId := 12;
    DELETE FROM JOURNAL WHERE BOOK_ID=bookId;
    DELETE FROM BOOKS WHERE ID=bookId;
    COMMIT;
END;


--2. То же, что и п.1, но транзакцию откатить.
DECLARE
    bookId NUMBER;
BEGIN
    SAVEPOINT SP;
    bookId := 12;
    DELETE FROM JOURNAL WHERE BOOK_ID=bookId;
    DELETE FROM BOOKS WHERE ID=bookId;
    ROLLBACK TO SP;
END;
