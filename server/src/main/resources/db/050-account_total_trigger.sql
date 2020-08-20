CREATE FUNCTION update_account_total() RETURNS trigger AS
$$
begin
    if (tg_op = 'INSERT') then
        update account set total = total + new.money where id = new.account_id;
        return new;
    elseif (tg_op = 'UPDATE') then
        update account set total = total - old.money where id = old.account_id;
        update account set total = total + new.money where id = new.account_id;
        return new;
    elseif (tg_op = 'DELETE') then
        update account set total = total - old.money where id = old.account_id;
        return old;
    end if;
end;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_account_total
    BEFORE INSERT OR UPDATE OR DELETE
    ON record_part
    FOR EACH ROW
EXECUTE PROCEDURE update_account_total();
