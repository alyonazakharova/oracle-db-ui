SELECT B.ID, B.NAME, B.CNT, BT.NAME FROM BOOKS B JOIN BOOK_TYPES BT on B.TYPE_ID = BT.ID;


SELECT FINE FROM BOOKS B JOIN BOOK_TYPES BT ON BT.ID = B.TYPE_ID WHERE B.ID=11;

SELECT BOOK_ID FROM JOURNAL WHERE ID=4;

SELECT TRUNC(DATE_RET)-TRUNC(DATE_END) AS DAYS FROM JOURNAL WHERE ID=7;

UPDATE BOOKS SET CNT=CNT+1 WHERE ID=11;
SELECT * FROM BOOKS;

create sequence users_seq start with 1 increment by 1 nomaxvalue nocache;
create table users
(
    id number(*,0) default users_seq.nextval not null,
    login varchar2(20) not null,
    password varchar2(100) not null
);
alter table users
    add constraint users_pk primary key(id);
