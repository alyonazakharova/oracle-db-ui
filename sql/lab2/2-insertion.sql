--Добавить нового клиента.
insert into CLIENTS (first_name, last_name, father_name, passport_seria, passport_num)
values ('Иван', 'Иванов', 'Петрович', '4002', '537264');


--Добавить в рамках транзакции клиента, книгу и запись в журнал библиотекаря о выдачи книги этому клиенту.
declare
    inserted_client_id number;
    inserted_book_id number;
    inserted_book_type_id number;
    inserted_book_day_count number;
begin
    insert into CLIENTS (first_name, last_name, father_name, passport_seria, passport_num)
    values ('Иван', 'Петров', 'Иванович', '4010', '458374')
    returning ID into inserted_client_id;
    insert into BOOKS (name, cnt, type_id)
    values ('Самоучитель по C++', 5, 1)
    returning ID, TYPE_ID, CNT into inserted_book_id, inserted_book_type_id;
    select DAY_COUNT into inserted_book_day_count
    from BOOK_TYPES where ID=inserted_book_type_id;
    insert into JOURNAL (book_id, client_id, date_beg, date_end)
    values (inserted_book_id, inserted_client_id, current_date, current_date+inserted_book_day_count);
    commit;
end;


--Добавить запись в журнал в случае, если книг у данного клиента больше 10, транзакцию откатить.
declare
    client number;
    book number;
    book_day_count number;
    books_number number;
begin
    savepoint sp;
    client := 1;
    book := 12;
    select DAY_COUNT into book_day_count
    from BOOK_TYPES join BOOKS B on BOOK_TYPES.ID = B.TYPE_ID where B.ID=book;
    insert into JOURNAL (book_id, client_id, date_beg, date_end)
    values (book, client, current_date, current_date+book_day_count);
    select count(ID) into books_number from JOURNAL
    where CLIENT_ID = client and DATE_RET is null;
    if (books_number > 10) then
        rollback to sp;
    end if;
    commit;
end;