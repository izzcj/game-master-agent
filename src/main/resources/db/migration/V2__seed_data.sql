INSERT INTO knowledge_base (id, name, description, source_type, status, created_at, updated_at)
VALUES (
    'kb-default',
    'Default Knowledge Base',
    'Default internal knowledge base for uploaded documents and seeded content.',
    'BUILTIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO game_catalog (id, name, aliases, platforms, tags, created_at, updated_at)
VALUES
    ('game-elden-ring', 'Elden Ring', '艾尔登法环,老头环', 'PC,PS5,Xbox', 'soulslike,open-world', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('game-monster-hunter-world', 'Monster Hunter World', '怪物猎人世界,MHW', 'PC,PS4,Xbox', 'action,hunting,build', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('game-cyberpunk-2077', 'Cyberpunk 2077', '赛博朋克2077,CP2077', 'PC,PS5,Xbox', 'rpg,build,story', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO web_source_cache (id, query_text, title, url, snippet, source_name, created_at, updated_at)
VALUES
    (
        'web-elden-ring-build',
        'elden ring build guide',
        'Elden Ring Build Guide Overview',
        'https://example.com/elden-ring-build',
        'Recommended melee builds prioritize vigor and a primary damage stat, then scale talismans and weapon affinity around that core.',
        'local-cache',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'web-mhw-longsword',
        'monster hunter world longsword build',
        'Monster Hunter World Long Sword Build Notes',
        'https://example.com/mhw-longsword',
        'Long Sword progression typically values affinity, weak point exploit, critical boost and sharpness maintenance before comfort skills.',
        'local-cache',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );
