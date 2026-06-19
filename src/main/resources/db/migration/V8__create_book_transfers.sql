create table if not exists book_transfers (
    id bigserial primary key,
    sender_id bigint not null references users(id),
    receiver_id bigint not null references users(id),
    file_key varchar(1024),
    book_title varchar(255) not null,
    original_hash varchar(64) not null,
    status varchar(20) not null default 'pending',
    created_at timestamptz not null default now(),
    expires_at timestamptz not null
);

create index if not exists idx_book_transfers_receiver_created_at
    on book_transfers(receiver_id, created_at desc);

create index if not exists idx_book_transfers_status_expires_at
    on book_transfers(status, expires_at);

create index if not exists idx_book_transfers_receiver_hash
    on book_transfers(receiver_id, original_hash);
