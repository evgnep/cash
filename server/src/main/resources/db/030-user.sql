-- Поля пользователя для аутентификации
alter table appl_user
    add column password text;
alter table appl_user
    add column enabled bool not null default true;
