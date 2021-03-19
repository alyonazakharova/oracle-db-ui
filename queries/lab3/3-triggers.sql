-- Создать триггер, который не позволяет добавить читателя с номером паспорта, который уже есть у существующего читателя.
CREATE OR REPLACE TRIGGER prevent_duplicated_passport
    BEFORE INSERT OR UPDATE ON CLIENTS
    FOR EACH ROW
DECLARE
    clientsNum NUMBER;
BEGIN
    SELECT COUNT(ID) INTO clientsNum FROM CLIENTS
    WHERE PASSPORT_SERIA=:NEW.PASSPORT_SERIA AND PASSPORT_NUM=:NEW.PASSPORT_NUM;
    IF (clientsNum > 0) THEN
        RAISE_APPLICATION_ERROR(-20000,
                                'Ошибка. Клиент с таким паспортом уже существует');
    END IF;
END;


-- Создать триггер, который не позволяет установить реальную дату возврата в журнале библиотекаря меньше, чем дата выдачи.
CREATE OR REPLACE TRIGGER check_return_date
    BEFORE UPDATE ON JOURNAL
    FOR EACH ROW
BEGIN
    IF (TRUNC(:NEW.DATE_RET) < TRUNC(:NEW.DATE_BEG)) THEN
        RAISE_APPLICATION_ERROR(-20000,
                                'Ошибка. Дата возврата не может быть меньше даты выдачи');
    END IF;
END;


-- Создать триггер, который при удалении строки журнала в случае, если книга не возвращена - откатывает транзакцию.
CREATE OR REPLACE TRIGGER prohibit_deleting_if_not_returned
    BEFORE DELETE ON JOURNAL
    FOR EACH ROW
BEGIN
    IF (:OLD.DATE_RET IS NULL) THEN
        RAISE_APPLICATION_ERROR(-20000,
                                'Невозможно удалить запись, если книга не возвращена');
    END IF;
END;
