--Удалить книги, не имеющие ссылок из записей в журнале.
delete from BOOKS B
where not exists
    (select * from JOURNAL J where J.BOOK_ID=B.ID);


--1. Удалить в рамках транзакции книгу и записи о ее выдаче.
declare
    book number;
begin
    book := 12;
    delete from JOURNAL where BOOK_ID=book;
    delete from BOOKS where ID=book;
    commit;
end;


--2. То же, что и п.1, но транзакцию откатить.
declare
    book number;
begin
    savepoint sp;
    book := 12;
    delete from JOURNAL where BOOK_ID=book;
    delete from BOOKS where ID=book;
    rollback to sp;
end;