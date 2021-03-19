-- самый большой штраф
create or replace procedure biggest_fine(res out number) is
begin
    select max(TOTAL_FINE) into res from
        (select ((trunc(DATE_RET) - trunc(DATE_END)) * FINE) as TOTAL_FINE
         from JOURNAL J join BOOKS B on B.ID = J.BOOK_ID join BOOK_TYPES BT on BT.ID = B.TYPE_ID
         where DATE_RET is not null and DATE_RET > J.DATE_END);
end;


-- три самые популярные книги
create or replace procedure popular_books(res out sys_refcursor) as
begin
    open res for
    select B.NAME from JOURNAL J join BOOKS B on B.ID = J.BOOK_ID
    group by B.NAME order by count(J.BOOK_ID) desc fetch first 3 rows only;
end;


-- сколько книг на руках у заданного клиента
create or replace procedure books_not_returned(client in number, res out number) is
begin
    select count(ID) into res from JOURNAL where CLIENT_ID=client and DATE_RET is null;
end;


-- штраф заданного клиента
create or replace procedure clients_fine(client in number, res out number) is
begin
    select sum(DAYS*FINE) into res from
    (select (trunc(DATE_RET) - trunc(DATE_END)) as DAYS, FINE
    from JOURNAL J join BOOKS B on B.ID = J.BOOK_ID join BOOK_TYPES BT on BT.ID = B.TYPE_ID
    where DATE_RET is not null and DATE_RET > J.DATE_END and CLIENT_ID=client);
end;


-- 10 книг на руках
create or replace trigger prohibit_if_10_not_returned
    before insert on JOURNAL
    for each row
declare
    books_number number;
begin
    select count(ID) into books_number from JOURNAL
    where CLIENT_ID = :NEW.client_id and DATE_RET is null;
    if (books_number > 10) then
        raise_application_error(-20000,
                                'Ошибка. У читателя на руках 10 книг');
    end if;
end;


-- триггер для изменения количества книг после выдачи
create or replace trigger inc_book_cnt
    after insert on JOURNAL
    for each row
declare
    book_count number;
begin
    select CNT into book_count from BOOKS where ID=:NEW.BOOK_ID;
    update BOOKS set CNT=book_count-1 where ID=:NEW.BOOK_ID;
end;


-- триггер для изменения количества книг после возврата
create or replace trigger dec_book_cnt
    after update on JOURNAL
    for each row
declare
    book_count number;
begin
    select CNT into book_count from BOOKS where ID=:OLD.BOOK_ID;
    update BOOKS set CNT=book_count+1 where ID=:OLD.BOOK_ID;
end;


-- триггер для проверки, есть ли книга в наличии (cnt > 0)
create or replace trigger check_if_in_stock
    before insert on JOURNAL
    for each row
declare
    book_count number;
begin
    select CNT into book_count from BOOKS where ID=:NEW.BOOK_ID;
    if (book_count < 1) then
        raise_application_error(-20000,
                                'Ошибочька. Книги нет в наличии');
    end if;
end;


-- расчет штрафа для заданной записи в журнале
CREATE OR REPLACE PROCEDURE CALCULATE_FINE(RECORD_ID IN NUMBER, RES OUT NUMBER) IS
    bookId NUMBER;
    oneDayFine NUMBER;
    daysCount NUMBER;
BEGIN
    SELECT BOOK_ID, TRUNC(DATE_RET)-TRUNC(DATE_END) INTO bookId, daysCount
    FROM JOURNAL WHERE ID=RECORD_ID;
    SELECT FINE INTO oneDayFine FROM BOOKS B JOIN BOOK_TYPES BT on BT.ID = B.TYPE_ID
    WHERE B.ID=bookId;
    RES := oneDayFine * daysCount;
END;
