--Изменить количество книг, которые выданы заданному клиенту.
update JOURNAL
set CLIENT_ID=6
where CLIENT_ID=5;


--1. В рамках транзакции поменять заданную книгу во всех записях журнала на другую и удалить ее.
declare
    book number;
    new_book number;
begin
    book := 10;
    new_book := 13;
    update JOURNAL J
    set BOOK_ID=new_book
    where BOOK_ID=book;
    delete from BOOKS B where B.ID=book;
    commit;
end;

--2. То же, что и п.1, но транзакцию откатить.
declare
    book number;
    new_book number;
begin
    savepoint sp;
    book := 10;
    new_book := 13;
    update JOURNAL J
    set BOOK_ID=new_book
    where BOOK_ID=book;
    delete from BOOKS B where B.ID=book;
    rollback to sp;
end;
