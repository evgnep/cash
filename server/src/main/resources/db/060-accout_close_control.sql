-- noinspection SqlResolveForFile

-- update_account_total ранее меняла итог по счету даже при отсутствии необходимости, в результате счет открывался заново
CREATE OR REPLACE FUNCTION update_account_total() RETURNS trigger AS
$$
begin
    if (tg_op = 'INSERT') then
        update account set total = total + new.money where id = new.account_id;
        return new;
    elseif (tg_op = 'UPDATE') then
        if (old.money <> new.money or old.account_id <> new.account_id) then
            update account set total = total - old.money where id = old.account_id;
            update account set total = total + new.money where id = new.account_id;
        end if;
        return new;
    elseif (tg_op = 'DELETE') then
        update account set total = total - old.money where id = old.account_id;
        return old;
    end if;
end;
$$ LANGUAGE plpgsql;

-- открываем счет, если меняется его остаток
CREATE OR REPLACE FUNCTION update_account_close() RETURNS trigger AS
$$
begin
    if (new.total <> 0) then
        new.closed = false;
    end if;
    return new;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_account_close
    BEFORE UPDATE
    ON account
    FOR EACH ROW
EXECUTE PROCEDURE update_account_close();
