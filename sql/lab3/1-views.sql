-- Создать представление, отображающее все книги и читателей,
-- о которых найдены записи в журнале с заданной даты по заданную дату.
create or replace view view_1 as
select J.ID, B.NAME, C2.FIRST_NAME, C2.LAST_NAME, C2.FATHER_NAME
from JOURNAL J
         join BOOKS B on B.ID = J.BOOK_ID
         join CLIENTS C2 on C2.ID = J.CLIENT_ID
where J.DATE_BEG between to_date('03-03-2021', 'dd-mm-yyyy') and to_date('10-03-2021', 'dd-mm-yyyy');


-- Создать представление, отображающее всех читателей и количество книг, находящихся у них на руках.
create or replace view view_2 as
select ID, FIRST_NAME, LAST_NAME, s1.BOOKS
from CLIENTS join
     (select CLIENT_ID, count(CLIENT_ID) as BOOKS
      from JOURNAL where DATE_RET is null
      group by CLIENT_ID) s1 on s1.CLIENT_ID=CLIENTS.ID
union
select ID, FIRST_NAME, LAST_NAME, 0 as BOOKS from CLIENTS
where ID not in
      (select CLIENT_ID from JOURNAL where DATE_RET is null);