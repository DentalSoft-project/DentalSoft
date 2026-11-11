-- ============================================================================
-- 02_utilisateurs_roles.sql
-- M1 : rôles, permissions, utilisateurs
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

CREATE TABLE roles (
    id_role BIGINT       NOT NULL AUTO_INCREMENT,
    code    VARCHAR(30)  NOT NULL,
    libelle VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_role),
    CONSTRAINT uq_roles_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Les valeurs de « permission » correspondent à l'enum Java
-- ma.dentalsoft.domain.Permission
CREATE TABLE role_permissions (
    id_role    BIGINT      NOT NULL,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_role, permission),
    CONSTRAINT fk_rp_role
        FOREIGN KEY (id_role) REFERENCES roles (id_role)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Une seule table pour les trois types d'utilisateurs :
--   specialite            -> médecin
--   num_cnss, commission  -> secrétaire
CREATE TABLE utilisateurs (
    id_utilisateur BIGINT        NOT NULL AUTO_INCREMENT,
    nom            VARCHAR(100)  NOT NULL,
    prenom         VARCHAR(100)  NOT NULL,
    login          VARCHAR(50)   NOT NULL,
    mot_de_passe   VARCHAR(255)  NOT NULL,   -- hash PBKDF2, jamais en clair
    actif          BOOLEAN       NOT NULL DEFAULT TRUE,
    id_role        BIGINT        NOT NULL,
    specialite     VARCHAR(100)  NULL,
    num_cnss       VARCHAR(30)   NULL,
    commission     DECIMAL(5,2)  NULL,
    date_creation  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_utilisateur),
    CONSTRAINT uq_utilisateurs_login UNIQUE (login),
    CONSTRAINT fk_utilisateurs_role
        FOREIGN KEY (id_role) REFERENCES roles (id_role),
    CONSTRAINT chk_utilisateurs_commission
        CHECK (commission IS NULL OR (commission >= 0 AND commission <= 100))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
