-- Создать хранимую процедуру, выводящую все книги и среднее время, на которое их брали, в днях.
create or replace procedure avg_days as
    cursor cursor1 is
        select ID, NAME, AVG(DAYS) from
            (select B.ID, NAME, AVG(trunc(DATE_RET)-trunc(DATE_BEG)) AS DAYS from BOOKS B left join JOURNAL J on B.ID = J.BOOK_ID
             where DATE_RET is not null group by B.ID, NAME
             union
             select B.ID, NAME, AVG(trunc(current_date)-trunc(DATE_BEG)) AS DAYS from BOOKS B left join JOURNAL J on B.ID = J.BOOK_ID
             where DATE_RET is null and DATE_BEG is not null group by B.ID, NAME
             union
             -- книги, которые вообще не брали
             select B.ID, NAME, 0 AS DAYS from BOOKS B left join JOURNAL J on B.ID = J.BOOK_ID
             where DATE_BEG is null group by B.ID, NAME)
        group by ID, NAME;

    book number;
    book_name varchar2(50);
    days_num number;
begin
    open cursor1;
    fetch cursor1 into book, book_name, days_num;
    --     DBMS_OUTPUT.ENABLE;
--     DBMS_OUTPUT.PUT_LINE('Start');
--     loop
--         fetch cursor1 into book, book_name, days_num;
--         if cursor1%notfound then
--             DBMS_OUTPUT.PUT_LINE('Fsyo');
--             exit;
--         else
--             DBMS_OUTPUT.PUT_LINE('ID = ' || book
--                 || ' NAME = ' || book_name
--                 || ' DAYS = ' || days_num);
--         end if;
--     end loop;
    close cursor1;
end;

-- set serveroutput on
-- begin
--     AVG_DAYS();
-- end;


-- Создать хранимую процедуру, имеющую два параметра «книга1» и «книга2».
-- Она должна возвращать клиентов, которые вернули «книгу1» быстрее чем «книгу2».
-- Если какой-либо клиент не брал одну из книг – он не рассматривается.

-- быстрее = раньше вернул или прошло меньше дней?
create or replace procedure who_was_first(book1 in number, book2 in number) as
    cursor cursor2 is
        select s1.CLIENT_ID from
            (select CLIENT_ID, DATE_RET from JOURNAL where BOOK_ID=book1) s1
                join
            (select CLIENT_ID, DATE_RET from JOURNAL where BOOK_ID=book2) s2
            on s1.CLIENT_ID=s2.CLIENT_ID
        where s1.DATE_RET < s2.DATE_RET or (s1.DATE_RET is not null and s2.DATE_RET is null);

    client number;
begin
    open cursor2;
    fetch cursor2 into client;
    close cursor2;
end;


-- Создать хранимую процедуру с входным параметром «книга» и двумя выходными параметрами,
-- возвращающими самое большое время, на которое брали книгу, и читателя, поставившего рекорд.
create or replace procedure max_time(book in number, time out number, client out number) is
begin
    select CLIENT_ID, DAYS into client, time from
        --количество дней для тех, кто уже вернул книгу
        (select CLIENT_ID, (trunc(DATE_RET)-trunc(DATE_BEG)) AS DAYS
         from JOURNAL where BOOK_ID=book and DATE_RET is not null
         union
         -- + количество дней для тех, кто ее ещё не вернул -> считаем до сегодняшней даты
         select CLIENT_ID, (trunc(current_date)-trunc(DATE_BEG)) AS DAYS
         from JOURNAL where BOOK_ID=book and DATE_RET is null)
    order by DAYS desc fetch first 1 row only;
end;

-- set serveroutput on
-- declare
--     client number;
--     time number;
-- begin
-- max_time(13, time, client);
-- DBMS_OUTPUT.ENABLE(100);
-- DBMS_OUTPUT.PUT_LINE(client || ' - ' || time );
-- end;
