--用于迁移所有常规对话数据
INSERT INTO PUBLIC.CHAT_THREAD (
    ID,
    ACTIVE,                 -- INTEGER NOT NULL
    COIN_USAGE,             -- NUMERIC(38,2) NOT NULL
    CREATE_TIME,            -- TIMESTAMP NOT NULL (来自源表)
    DESCRIPTION,            -- CHARACTER VARYING(3750)
    MODEL_CODE,             -- CHARACTER VARYING(50) NOT NULL (来自源表)
    PUBLIC_INFO,            -- CHARACTER VARYING(1024)
    TITLE,                  -- CHARACTER VARYING(240) NOT NULL (来自源表, 处理 NULL)
    TITLE_GENERATED,        -- INTEGER NOT NULL (来自源表, 处理 NULL)
    TOKEN_INPUT,            -- BIGINT NOT NULL
    TOKEN_OUTPUT,           -- BIGINT NOT NULL
    TOKEN_THOUGHTS,         -- BIGINT NOT NULL
    "TYPE",                 -- INTEGER
    UPDATE_TIME,            -- TIMESTAMP NOT NULL (来自源表)
    LAST_MESSAGE_ID,        -- BIGINT
    NPC_ID,                 -- BIGINT
    PLAYER_ID,              -- BIGINT NOT NULL (来自源表)
    USER_ID                 -- BIGINT NOT NULL (重要：需要默认值)
)
SELECT
    mct.id,
    -- 从源表 MODEL_CHAT_THREAD (别名 mct) 选择数据或提供默认值
    0,                                      -- ACTIVE: 假设迁移的数据默认为激活状态 (1)
    0.00,                                   -- COIN_USAGE: 默认设置为 0.00
    mct.CREATE_TIME,                        -- CREATE_TIME: 从源表获取
    NULL,                                   -- DESCRIPTION: 源表中无此字段，设置为 NULL (如果该列允许NULL)
    mct.MODEL_CODE,                         -- MODEL_CODE: 从源表获取
    NULL,                                   -- PUBLIC_INFO: 源表中无此字段，设置为 NULL (如果该列允许NULL)
    COALESCE(mct.TITLE, 'Untitled'),        -- TITLE: 从源表获取，如果源 TITLE 为 NULL，则设置为 'Untitled' (因为目标列NOT NULL)
    COALESCE(mct.TITLE_GENERATED, 1),       -- TITLE_GENERATED: 从源表获取，如果源 TITLE_GENERATED 为 NULL，则设置为 0 (因为目标列NOT NULL)
    0,                                      -- TOKEN_INPUT: 默认设置为 0
    0,                                      -- TOKEN_OUTPUT: 默认设置为 0
    0,                                      -- TOKEN_THOUGHTS: 默认设置为 0
    0,                                   -- "TYPE": 源表中无此字段，设置为 NULL (如果该列允许NULL)
    mct.UPDATE_TIME,                        -- UPDATE_TIME: 从源表获取
    NULL,                                   -- LAST_MESSAGE_ID: 源表中无此字段，设置为 NULL (如果该列允许NULL)
    NULL,                                   -- NPC_ID: 源表中无此字段，设置为 NULL (如果该列允许NULL)
    mct.PLAYER_ID,                          -- PLAYER_ID: 从源表获取
    -1                                       -- USER_ID: 源表中无此字段，但目标列是 NOT NULL。这里使用 0 作为占位符/默认值。
FROM
    PUBLIC.MODEL_CHAT_THREAD mct;

UPDATE PUBLIC.CHAT_THREAD SET ID = ID + 10000;
UPDATE PUBLIC.CHAT_THREAD SET USER_ID = -1;

INSERT INTO PUBLIC.CHAT_MESSAGE (
    ID,
    CREATE_TIME,
    CONTENT,
    SEQ,
    SENDER_ROLE,
    THREAD_ID,
    SENDER_NAME,
    TOKEN_INPUT,
    TOKEN_OUTPUT,
    TOKEN_THOUGHTS,
    UPDATE_TIME
)
SELECT
    nch.id,
    nch.CREATE_TIME,
    nch.RP_CONTENT ,      -- 或者 nch.RP_CONTENT，取决于您想迁移哪个内容
    nch."SEQUENCE",
    nch."TYPE",           -- 请确认 TYPE 和 SENDER_ROLE 之间的映射关系是否正确
    nch.THREAD_ID,
    '未知',                -- SENDER_NAME 的默认值，您可以按需修改
    0,                    -- TOKEN_INPUT 的默认值
    0,                    -- TOKEN_OUTPUT 的默认值
    0,                    -- TOKEN_THOUGHTS 的默认值
    nch.CREATE_TIME       -- UPDATE_TIME 使用旧表的 CREATE_TIME
FROM
    PUBLIC.NPC_CHAT_HISTORY nch;
UPDATE PUBLIC.CHAT_MESSAGE SET id = id + 10000;
UPDATE PUBLIC.CHAT_MESSAGE SET THREAD_ID = THREAD_ID + 10000;

-- 另一种使用子查询的方式 (通常兼容性更广，但性能可能稍逊于 UPDATE FROM)
UPDATE PUBLIC.CHAT_THREAD ct
SET USER_ID = (
    SELECT p.USER_ID
    FROM PUBLIC.PLAYER p
    WHERE p.ID = ct.PLAYER_ID
)
WHERE ct.USER_ID = -1
  AND EXISTS ( -- 确保 PLAYER 记录存在，避免 USER_ID 被设为 NULL (尽管这里 USER_ID 非空)
    SELECT 1
    FROM PUBLIC.PLAYER p
    WHERE p.ID = ct.PLAYER_ID
);
--所有常规对话数据迁移完成

--迁移所有RP对话数据
INSERT INTO PUBLIC.CHAT_THREAD (
    ID,
    ACTIVE,
    COIN_USAGE,
    CREATE_TIME,
    DESCRIPTION,
    MODEL_CODE,
    PUBLIC_INFO,
    TITLE,
    TITLE_GENERATED,
    TOKEN_INPUT,
    TOKEN_OUTPUT,
    TOKEN_THOUGHTS,
    "TYPE",
    UPDATE_TIME,
    LAST_MESSAGE_ID,
    NPC_ID,
    PLAYER_ID,
    USER_ID
)
SELECT
    nct.id,
    nct.ACTIVE,
    0.00,                                   -- COIN_USAGE (默认值, NOT NULL)
    nct.CREATE_TIME,
    nct.DESCRIPTION,                        -- DESCRIPTION 长度从 VARCHAR(500) 到 VARCHAR(3750) 是兼容的
    nct.MODEL_CODE,
    NULL,                                   -- PUBLIC_INFO (默认 NULL, 可空)
    COALESCE(nct.TITLE, 'Untitled Thread'), -- TITLE (处理 NULL 值，目标列 NOT NULL, 长度从 VARCHAR(100) 到 VARCHAR(240) 兼容)
    0,                                      -- TITLE_GENERATED (默认值 0, NOT NULL)
    0,                                      -- TOKEN_INPUT (默认值, NOT NULL)
    0,                                      -- TOKEN_OUTPUT (默认值, NOT NULL)
    0,                                      -- TOKEN_THOUGHTS (默认值, NOT NULL)
    1,                                   -- "TYPE"
    nct.UPDATE_TIME,
    NULL,                                   -- LAST_MESSAGE_ID (默认 NULL, 可空)
    nct.NPC_ID,
    nct.PLAYER_ID,
    p.USER_ID                               -- 从 PUBLIC.PLAYER 表获取 USER_ID
FROM
    PUBLIC.NPC_CHAT_THREAD nct
        JOIN
    PUBLIC.PLAYER p ON nct.PLAYER_ID = p.ID; -- 通过 PLAYER_ID 关联 PLAYER 表以获取 USER_ID

INSERT INTO PUBLIC.CHAT_MESSAGE (
    CREATE_TIME,
    CONTENT,
    SEQ,
    SENDER_ROLE,
    THREAD_ID,
    SENDER_NAME,
    TOKEN_INPUT,
    TOKEN_OUTPUT,
    TOKEN_THOUGHTS,
    UPDATE_TIME
)
SELECT
    nch.CREATE_TIME,
    nch.RAW_CONTENT,      -- 或者 nch.RP_CONTENT，取决于您想迁移哪个内容
    nch."SEQUENCE",
    nch."TYPE",           -- 请确认 TYPE 和 SENDER_ROLE 之间的映射关系是否正确
    nch.THREAD_ID,
    '未知',                -- SENDER_NAME 的默认值，您可以按需修改
    0,                    -- TOKEN_INPUT 的默认值
    0,                    -- TOKEN_OUTPUT 的默认值
    0,                    -- TOKEN_THOUGHTS 的默认值
    nch.CREATE_TIME       -- UPDATE_TIME 使用旧表的 CREATE_TIME
FROM
    PUBLIC.NPC_CHAT_HISTORY nch;








