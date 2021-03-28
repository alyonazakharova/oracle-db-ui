--Изменить количество книг, которые выданы заданному клиенту.
UPDATE JOURNAL SET CLIENT_ID=6 WHERE CLIENT_ID=5;

DELETE FROM JOURNAL WHERE CLIENT_ID=5 AND BOOK_ID=13;


--1. В рамках транзакции поменять заданную книгу во всех записях журнала на другую и удалить ее.
DECLARE
    bookId NUMBER;
    newBookId NUMBER;
BEGIN
    bookId := 10;
    newBookId := 13;
    UPDATE JOURNAL SET BOOK_ID=newBookId WHERE BOOK_ID= bookId;
    DELETE FROM BOOKS WHERE ID= bookId;
    COMMIT;
END;

--2. То же, что и п.1, но транзакцию откатить.
DECLARE
    bookId NUMBER;
    newBookId NUMBER;
BEGIN
    SAVEPOINT SP;
    bookId := 10;
    newBookId := 13;
    UPDATE JOURNAL SET BOOK_ID=newBookId WHERE BOOK_ID= bookId;
    DELETE FROM BOOKS WHERE ID= bookId;
    ROLLBACK TO SP;
END;
