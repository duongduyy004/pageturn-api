alter table users rename column active to is_active;
alter table users alter column role set default 'USER';
alter table users alter column is_active set default true;
