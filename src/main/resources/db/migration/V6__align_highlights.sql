alter table highlights add column if not exists chapter_idx integer not null default 0;
alter table highlights add column if not exists start_offset integer not null default 0;
alter table highlights add column if not exists end_offset integer not null default 0;
alter table highlights add column if not exists text_content text;
alter table highlights add column if not exists is_deleted boolean not null default false;
