--用于1.2W之前的版本升级到1.2W的迁移SQL

--迁移API密钥授权关系

ALTER TABLE IF EXISTS api_key_authorizations ADD COLUMN authorizer_player_id bigint;
ALTER TABLE IF EXISTS api_key_authorizations ADD COLUMN authorized_player_id bigint;

UPDATE api_key_authorizations
SET authorizer_player_id = -1
WHERE authorizer_player_id IS NULL;

UPDATE api_key_authorizations
SET authorized_player_id = -1
WHERE authorized_player_id IS NULL;

ALTER TABLE IF EXISTS api_key_authorizations ALTER COLUMN authorizer_player_id bigint NOT NULL;
ALTER TABLE IF EXISTS api_key_authorizations ALTER COLUMN authorized_player_id bigint NOT NULL;

-- 已有授权关系迁移到用户所拥有的第一个人物下
BEGIN;
UPDATE api_key_authorizations aka
SET authorizer_player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = aka.authorizer_user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE aka.authorizer_player_id = -1;

UPDATE api_key_authorizations aka
SET authorized_player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = aka.authorized_user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE aka.authorized_player_id = -1;
COMMIT;

ALTER TABLE IF EXISTS api_key_authorizations DROP COLUMN authorized_user_id;
ALTER TABLE IF EXISTS api_key_authorizations DROP COLUMN authorizer_user_id;
--迁移API密钥授权关系 结束

--迁移API密钥
ALTER TABLE IF EXISTS api_keys ADD COLUMN player_id bigint;
UPDATE api_keys
SET player_id = -1
WHERE player_id IS NULL;
ALTER TABLE IF EXISTS api_keys ALTER COLUMN player_id bigint NOT NULL;

--已有的API密钥变更到用户下第一个人物
BEGIN;
UPDATE api_keys ak
SET player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = ak.user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE ak.player_id = -1;
COMMIT;

ALTER TABLE IF EXISTS api_keys DROP COLUMN user_id;
--迁移API密钥 结束

--迁移模型API配置
ALTER TABLE IF EXISTS model_api_key_configs ADD COLUMN player_id bigint;
UPDATE model_api_key_configs
SET player_id = -1
WHERE player_id IS NULL;
ALTER TABLE IF EXISTS model_api_key_configs ALTER COLUMN player_id bigint NOT NULL;

--已有模型API配置变更到用户下第一个人物
BEGIN;
UPDATE model_api_key_configs makc
SET player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = makc.user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE makc.player_id = -1;
COMMIT;

ALTER TABLE IF EXISTS model_api_key_configs DROP COLUMN user_id;
--迁移模型API配置 结束

--迁移配置项
ALTER TABLE IF EXISTS config ADD COLUMN player_id bigint;

UPDATE config c SET player_id = -1
WHERE c.USER_ID != -1;

--已有配置变更到用户下第一个人物
UPDATE config c
SET player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = c.user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE c.player_id = -1;

ALTER TABLE IF EXISTS config DROP COLUMN user_id;
--迁移配置项 结束

--迁移ChatThread
ALTER TABLE IF EXISTS model_chat_thread ADD COLUMN player_id bigint;

UPDATE model_chat_thread
SET player_id = -1
WHERE player_id IS NULL;

ALTER TABLE IF EXISTS model_chat_thread ALTER COLUMN player_id bigint NOT NULL;

--已有CHAT-THREAD变更到用户下第一个人物
UPDATE model_chat_thread c
SET player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = c.user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE c.player_id = -1;

ALTER TABLE IF EXISTS model_chat_thread DROP COLUMN user_id;
--迁移ChatThread 结束

--迁移ChatHistory
BEGIN;
ALTER TABLE IF EXISTS model_chat_history ADD COLUMN player_id bigint;

UPDATE model_chat_history
SET player_id = -1
WHERE player_id IS NULL;

ALTER TABLE IF EXISTS model_chat_history ALTER COLUMN player_id bigint NOT NULL;

--已有ChatHistory变更到用户下第一个人物
UPDATE model_chat_history c
SET player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = c.user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE c.player_id = -1;

ALTER TABLE IF EXISTS model_chat_history DROP COLUMN user_id;
COMMIT;
--迁移ChatHistory 结束

--迁移modelChatSegment缓存
BEGIN;
ALTER TABLE IF EXISTS model_chat_segment ADD COLUMN player_id bigint;
DELETE FROM model_chat_segment WHERE 1=1;
ALTER TABLE IF EXISTS model_chat_segment ALTER COLUMN player_id bigint NOT NULL;
ALTER TABLE IF EXISTS model_chat_segment DROP COLUMN user_id;
COMMIT;
--迁移modelChatSegment缓存 结束


--迁移model_roles
BEGIN;
ALTER TABLE IF EXISTS model_roles ADD COLUMN player_id bigint;

UPDATE model_roles
SET player_id = -1
WHERE player_id IS NULL;

ALTER TABLE IF EXISTS model_roles ALTER COLUMN player_id bigint NOT NULL;

--已有ModelRoles变更到用户下第一个人物
UPDATE model_roles c
SET player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = c.user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE c.player_id = -1;

ALTER TABLE IF EXISTS model_roles DROP COLUMN user_id;
COMMIT;

