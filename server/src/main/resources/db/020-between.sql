CREATE OR REPLACE FUNCTION betweenIfNotNull(value anyelement, "from" anyelement, "to" anyelement)
    RETURNS boolean
    IMMUTABLE
    LANGUAGE 'sql' AS
$$
    select ("from" is null or value >= "from") and ("to" is null or value < "to");
$$;

-- Связка Hibernate + Data JPA передает параметр null как bytea :(
-- Плюс почему-то !null Instant прилетает как строковый литерал
-- Данные две функции конвертят параметр типа Istant в timestamptz,
-- корректно обрабатывая случай null
CREATE OR REPLACE FUNCTION toTimestampTz(value text)
    RETURNS timestamptz
    IMMUTABLE
    LANGUAGE 'sql' AS
$$
select value::timestamptz;
$$;

CREATE OR REPLACE FUNCTION toTimestampTz(value bytea)
    RETURNS timestamptz
    IMMUTABLE
    LANGUAGE 'sql' AS
$$
select null::timestamptz;
$$;
