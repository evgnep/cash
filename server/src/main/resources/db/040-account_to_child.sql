-- Доступность кошелька ребенку
alter table account
    add column available_to_child bool not null default false;

