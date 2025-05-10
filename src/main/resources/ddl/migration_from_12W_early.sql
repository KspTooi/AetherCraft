--用于1.2W之前的版本升级到1.2W的迁移SQL

--迁移API密钥授权关系

-- 添加字段，暂时允许 NULL
ALTER TABLE IF EXISTS api_key_authorizations ADD COLUMN authorizer_player_id bigint;
ALTER TABLE IF EXISTS api_key_authorizations ADD COLUMN authorized_player_id bigint;

-- 更新已有记录，将值设置为 -1
UPDATE api_key_authorizations
SET authorizer_player_id = -1
WHERE authorizer_player_id IS NULL;

UPDATE api_key_authorizations
SET authorized_player_id = -1
WHERE authorized_player_id IS NULL;

-- 最后设置字段为 NOT NULL
ALTER TABLE IF EXISTS api_key_authorizations ALTER COLUMN authorizer_player_id bigint NOT NULL;
ALTER TABLE IF EXISTS api_key_authorizations ALTER COLUMN authorized_player_id bigint NOT NULL;

-- 已有授权关系迁移到用户所拥有的第一个人物下
BEGIN;
-- 更新 authorizer_player_id，仅对值为 -1 的记录进行更新，获取 authorizer_user_id 对应的 player 表中创建时间最新的 player.id
UPDATE api_key_authorizations aka
SET authorizer_player_id = (
    SELECT p.id
    FROM player p
    WHERE p.user_id = aka.authorizer_user_id
    ORDER BY p.create_time ASC
    LIMIT 1
    )
WHERE aka.authorizer_player_id = -1;

-- 更新 authorized_player_id，仅对值为 -1 的记录进行更新，获取 authorized_user_id 对应的 player 表中创建时间最新的 player.id
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
--迁移模型API配置 结束
