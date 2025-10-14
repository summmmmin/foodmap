-- USERS
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id    VARCHAR(100) NOT NULL,               -- 로그인 ID
    password    VARCHAR(100) NOT NULL,
    username    VARCHAR(100) NOT NULL,
    use_yn      CHAR(1)     NOT NULL DEFAULT 'Y',
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_login_id (login_id),
    KEY idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PLACE
CREATE TABLE IF NOT EXISTS place (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    kakao_place_id        VARCHAR(100) NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    category_group_code   VARCHAR(20),
    category_group_name   VARCHAR(100),
    category_name         VARCHAR(200),
    phone                 VARCHAR(50),
    address               VARCHAR(300),
    road_address          VARCHAR(300),
    longitude             DOUBLE NOT NULL,            -- x
    latitude              DOUBLE NOT NULL,            -- y
    location              POINT NOT NULL SRID 4326,   -- 좌표계

    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_place_kakao (kakao_place_id),
    KEY idx_place_category (category_group_code),
    SPATIAL INDEX sp_idx_place_location (location),
    FULLTEXT KEY ft_place_name_addr (name, road_address, address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- BOOKMARK
CREATE TABLE IF NOT EXISTS bookmark (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,                     -- users.id 참조
    place_id    BIGINT NOT NULL,                     -- place.id  참조
    memo        VARCHAR(200),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_bookmark_user_place (user_id, place_id),
    KEY idx_bookmark_place (place_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS top_bookmarked_snapshot_hist (
    snapshot_date          DATE         NOT NULL,
    period_days            INT          NOT NULL,
    category_group_code    VARCHAR(8)   NULL,
    rank_no                INT          NOT NULL,
    place_id               BIGINT       NOT NULL,
    bookmark_count         BIGINT       NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (snapshot_date, period_days, category_group_code, rank_no),
    UNIQUE KEY uq_snapshot_place (snapshot_date, period_days, category_group_code, place_id),
    KEY idx_bookmark_lookup (snapshot_date, period_days, category_group_code, rank_no, place_id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;