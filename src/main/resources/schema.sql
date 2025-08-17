--SET NAMES utf8mb4;
--SET SESSION sql_mode='STRICT_ALL_TABLES';

-- 1) USERS
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    user_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    id          VARCHAR(100) NOT NULL,           -- 로그인 ID (unique)
    password    VARCHAR(100) NOT NULL,
    username    VARCHAR(100) NOT NULL,
    use_yn      CHAR(1)     NOT NULL DEFAULT 'Y',-- 'Y' or 'N'
    created_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_id (id),
    KEY idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2) PLACE
DROP TABLE IF EXISTS place;
CREATE TABLE place (
    place_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    kakao_place_id        VARCHAR(100) NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    category_group_code   VARCHAR(20),
    category_group_name   VARCHAR(100),
    category_name         VARCHAR(200),
    phone                 VARCHAR(50),

    address               VARCHAR(300),
    road_address          VARCHAR(300),

    longitude             DOUBLE NOT NULL,           -- x
    latitude              DOUBLE NOT NULL,           -- y
    location              POINT NOT NULL SRID 4326,  -- 좌표계

    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_place_kakao (kakao_place_id),
    KEY idx_place_category (category_group_code),
    SPATIAL INDEX sp_idx_place_location (location),
    FULLTEXT KEY ft_place_name_addr (name, road_address, address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3) BOOKMARK
DROP TABLE IF EXISTS bookmark;
CREATE TABLE bookmark (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NOT NULL,
    place_id    BIGINT NOT NULL,
    memo        VARCHAR(200),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY uk_bookmark_user_place (user_id, place_id),
    KEY idx_bookmark_user (user_id),
    KEY idx_bookmark_place (place_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;