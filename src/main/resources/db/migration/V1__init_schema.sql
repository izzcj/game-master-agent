CREATE TABLE game_catalog (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    aliases TEXT,
    platforms VARCHAR(255),
    tags VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE knowledge_base (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    source_type VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE knowledge_file (
    id VARCHAR(64) PRIMARY KEY,
    knowledge_base_id VARCHAR(64) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    storage_path TEXT NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    game_name VARCHAR(255),
    platform VARCHAR(255),
    tags VARCHAR(255),
    summary TEXT,
    status VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE ingest_job (
    id VARCHAR(64) PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    error_message TEXT,
    chunk_count INTEGER,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE knowledge_chunk_snapshot (
    id VARCHAR(64) PRIMARY KEY,
    file_id VARCHAR(64) NOT NULL,
    knowledge_base_id VARCHAR(64) NOT NULL,
    chunk_index INTEGER NOT NULL,
    title VARCHAR(255),
    game_name VARCHAR(255),
    platform VARCHAR(255),
    language VARCHAR(64),
    tags VARCHAR(255),
    source_url TEXT,
    content TEXT NOT NULL,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE web_source_cache (
    id VARCHAR(64) PRIMARY KEY,
    query_text VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    snippet TEXT,
    source_name VARCHAR(128) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_session (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE chat_message (
    id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    role VARCHAR(32) NOT NULL,
    content TEXT NOT NULL,
    citations TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_knowledge_file_kb ON knowledge_file (knowledge_base_id);
CREATE INDEX idx_ingest_job_file ON ingest_job (file_id);
CREATE INDEX idx_chunk_file ON knowledge_chunk_snapshot (file_id);
CREATE INDEX idx_chunk_kb ON knowledge_chunk_snapshot (knowledge_base_id);
CREATE INDEX idx_web_source_query ON web_source_cache (query_text);
CREATE INDEX idx_chat_message_session ON chat_message (session_id);
