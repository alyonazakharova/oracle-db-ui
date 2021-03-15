-- Создать триггер, который не позволяет добавить читателя с номером паспорта, который уже есть у существующего читателя.
create or replace trigger prevent_duplicated_passport
    before insert on CLIENTS
    for each row
declare
    clients_num number;
begin
    select count(ID) into clients_num from CLIENTS
    where PASSPORT_SERIA=:NEW.passport_seria and PASSPORT_NUM=:NEW.passport_num;
    if (clients_num > 0) then
        raise_application_error(-20000,
                                'A client with the same passport number already exists');
    end if;
end;


-- Создать триггер, который не позволяет установить реальную дату возврата в журнале библиотекаря меньше, чем дата выдачи.
create or replace trigger check_return_date
    before update
    on JOURNAL
    for each row
begin
    if (trunc(:NEW.date_ret) < trunc(:NEW.date_beg)) then
        raise_application_error(-20000,
                                'Return date cannot be less than the begin date');
    end if;
end;


-- Создать триггер, который при удалении строки журнала в случае, если книга не возвращена - откатывает транзакцию.
create or replace trigger prohibit_deleting_if_not_returned
    before delete
    on JOURNAL
    for each row
begin
    if (:OLD.date_ret is null) then
        raise_application_error(-20000,
                                'You cannot delete record from journal if the book is not returned');
    end if;
end;
