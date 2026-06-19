alter table collection_books add column if not exists position integer not null default 0;

with ranked as (
    select id, row_number() over (partition by collection_id order by created_at asc, id asc) - 1 as new_position
    from collection_books
)
update collection_books cb
set position = ranked.new_position
from ranked
where cb.id = ranked.id;
