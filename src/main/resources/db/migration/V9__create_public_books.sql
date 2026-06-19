create table if not exists public_books (
    id bigserial primary key,
    book_hash varchar(64) not null unique,
    title varchar(255) not null,
    author varchar(255),
    description text,
    language varchar(32),
    cover_key varchar(1024),
    file_key varchar(1024) not null,
    file_format varchar(16) not null,
    file_size bigint not null,
    category varchar(120),
    added_by bigint not null references users(id),
    download_count bigint not null default 0,
    is_active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index if not exists idx_public_books_active_created_at
    on public_books(is_active, created_at desc);

create index if not exists idx_public_books_category_active
    on public_books(category, is_active);
