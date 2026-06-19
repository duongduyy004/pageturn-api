alter table reading_progress add column if not exists chapter_idx integer not null default 0;
alter table reading_progress add column if not exists scroll_pct double precision not null default 0;

update reading_progress
set scroll_pct = case
    when progress_percent <= 0 then 0
    when progress_percent >= 100 then 1
    else progress_percent / 100.0
end
where scroll_pct = 0;
