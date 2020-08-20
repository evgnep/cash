-- проверяет инвариант проводки:
-- для каждой валюты сумма денег = сумме бюджета
-- Тригер срабатывает в конце транзакции на каждую операцию с record_part (что несколько избыточно...)
CREATE OR REPLACE FUNCTION check_record_invariant() RETURNS trigger AS
$$
declare
    rec_id uuid;
begin
    if (tg_op = 'INSERT' or tg_op = 'UPDATE') then
        rec_id = new.record_id;
    else -- delete
        rec_id = old.record_id;
    end if;

    if (exists(
            select a.currency_id
            from record_part rp
                     inner join account a on rp.account_id = a.id
            where rp.record_id = rec_id
            group by a.currency_id
            having sum(rp.money * case when a.is_money then 1 else -1 end) <> 0
        )) then
        raise 'Invalid record %: total invariant', rec_id;
    end if;

    return null;
end;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER check_record_invariant
    AFTER INSERT or UPDATE or DELETE
    ON record_part
    INITIALLY DEFERRED
    FOR EACH ROW
EXECUTE PROCEDURE check_record_invariant();



CREATE OR REPLACE FUNCTION check_record_part() RETURNS trigger AS
$$
begin
    if (new.record_id <> old.record_id) then
        raise 'Cant change record_id in record_part %', new.id;
    end if;

    return new;
end;
$$ LANGUAGE plpgsql;


CREATE TRIGGER check_record_part
    BEFORE UPDATE
    ON record_part
    FOR EACH ROW
EXECUTE PROCEDURE check_record_part();
