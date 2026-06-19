alter table bookmarks add column if not exists chapter_idx integer not null default 0;
alter table bookmarks add column if not exists scroll_pct double precision not null default 0;
alter table bookmarks add column if not exists snippet varchar(2000);
alter table bookmarks add column if not exists is_deleted boolean not null default false;
