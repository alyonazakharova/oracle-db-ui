--Вывести все строки из журнала библиотекаря, дата возврата которых меньше некоторой даты.
select * from JOURNAL
where DATE_RET < '12.03.2020';


--Посчитать количество книг, которых нет в наличии.
select count(ID) from BOOKS
where CNT=0;


--Вывести все книги типа «уникальные», которые на руках у читателей.
select B.NAME from JOURNAL J
                       join BOOKS B on B.ID = J.BOOK_ID
                       join BOOK_TYPES BT on BT.ID = B.TYPE_ID
where BT.ID=3 and J.DATE_RET is null;


--Вывести журнал библиотекаря, читателей, включая читателей, которые не брали книг и книги, включая книги, которых не выдавали.
select * from JOURNAL J
                  full join BOOKS B on B.ID = J.BOOK_ID
                  full join CLIENTS C2 on C2.ID = J.CLIENT_ID;


--Число книг на руках у заданного клиента.
select count(ID) from JOURNAL
where CLIENT_ID=1 and DATE_RET is null;


--Размер штрафа заданного клиента.



--Размер самого большого штрафа.


--Три самые популярные книги.
-- select books.name, count(journal.book_id) as n from
select B.NAME from JOURNAL J join BOOKS B on B.ID = J.BOOK_ID
group by B.NAME order by count(J.BOOK_ID) desc fetch first 3 rows only;