-- ============================================================================
-- 02_utilisateurs_roles.sql
-- M1 : utilisateurs, rôle unique et permissions
-- Cible : MySQL 8.0.16+ ; installation neuve uniquement.
-- Dépend de : 00_base_cabinet.sql (cabinet_medical)
-- Audit cree_par / modifie_par : références logiques vers utilisateurs.
-- Aucun nettoyage : les tables existantes ne sont pas supprimées.
-- Un seul rôle obligatoire par utilisateur, comme dans le Java existant.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Rôles applicatifs (identifiants existants conservés)
-- ----------------------------------------------------------------------------
CREATE TABLE roles (
    id_role                     BIGINT         NOT NULL AUTO_INCREMENT,
    code                        VARCHAR(30)    NOT NULL,
    libelle                     VARCHAR(100)   NOT NULL,
    PRIMARY KEY (id_role),
    CONSTRAINT uq_roles_code UNIQUE (code)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Permissions : codes de ma.dentalsoft.domain.Permission
-- ----------------------------------------------------------------------------
CREATE TABLE role_permissions (
    id_role                     BIGINT         NOT NULL,
    permission                  VARCHAR(50)    NOT NULL,
    PRIMARY KEY (id_role, permission),
    CONSTRAINT fk_rp_role
        FOREIGN KEY (id_role) REFERENCES roles (id_role)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Héritage dans une table ; le rôle détermine le profil Java.
-- Mot de passe haché uniquement ; attributs Staff conservés comme profil.
-- Le service contrôle les champs spécifiques et les autorisations.
-- ----------------------------------------------------------------------------
CREATE TABLE utilisateurs (
    id_utilisateur              BIGINT         NOT NULL AUTO_INCREMENT,
    nom                         VARCHAR(100)   NOT NULL,
    prenom                      VARCHAR(100)   NOT NULL,
    login                       VARCHAR(50)    NOT NULL,
    mot_de_passe                VARCHAR(255)   NOT NULL,
    actif                       BOOLEAN        NOT NULL DEFAULT TRUE,
    id_role                     BIGINT         NOT NULL,
    cabinet_id                  BIGINT         NULL,
    email                       VARCHAR(120)   NULL,
    adresse                     VARCHAR(255)   NULL,
    cin                         VARCHAR(30)    NULL,
    telephone                   VARCHAR(20)    NULL,
    sexe                        ENUM('M','F')  NULL,
    date_naissance              DATE           NULL,
    derniere_connexion          DATETIME       NULL,
    specialite                  VARCHAR(100)   NULL,
    num_cnss                    VARCHAR(30)    NULL,
    commission                  DECIMAL(5,2)   NULL,
    salaire                     DECIMAL(12,2)  NULL,
    prime                       DECIMAL(12,2)  NULL,
    date_recrutement            DATE           NULL,
    solde_conges                INT            NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id_utilisateur),
    CONSTRAINT uq_utilisateurs_login UNIQUE (login),
    CONSTRAINT uk_utilisateurs_cin UNIQUE (cin),
    CONSTRAINT fk_utilisateurs_role
        FOREIGN KEY (id_role) REFERENCES roles (id_role)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_utilisateurs_cabinet
        FOREIGN KEY (cabinet_id) REFERENCES cabinet_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_utilisateurs_commission
        CHECK (commission IS NULL OR (commission >= 0 AND commission <= 100)),
    CONSTRAINT chk_utilisateurs_salaire
        CHECK (salaire IS NULL OR salaire >= 0),
    CONSTRAINT chk_utilisateurs_prime
        CHECK (prime IS NULL OR prime >= 0),
    CONSTRAINT chk_utilisateurs_conges
        CHECK (solde_conges IS NULL OR solde_conges >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
