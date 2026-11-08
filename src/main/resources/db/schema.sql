-- ============================================================================
-- DentalSoft : module M1 (utilisateurs, rôles, permissions)
-- Exécution : mysql -u root -p < schema.sql   (ou dans MySQL Workbench / IntelliJ)
-- ATTENTION : les tables sont recréées à chaque exécution (les données sont effacées).
-- ============================================================================
CREATE DATABASE IF NOT EXISTS dentalSoftBD
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE dentalSoftBD;

DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS utilisateurs;
DROP TABLE IF EXISTS roles;

CREATE TABLE roles (
                       id_role  BIGINT       NOT NULL AUTO_INCREMENT,
                       code     VARCHAR(30)  NOT NULL,
                       libelle  VARCHAR(100) NOT NULL,
                       CONSTRAINT pk_roles PRIMARY KEY (id_role),
                       CONSTRAINT uq_roles_code UNIQUE (code)
);

-- Les valeurs de « permission » correspondent à l'enum Java ma.dentalsoft.domain.Permission
CREATE TABLE role_permissions (
                                  id_role    BIGINT      NOT NULL,
                                  permission VARCHAR(50) NOT NULL,
                                  CONSTRAINT pk_role_permissions PRIMARY KEY (id_role, permission),
                                  CONSTRAINT fk_rp_role FOREIGN KEY (id_role) REFERENCES roles (id_role) ON DELETE CASCADE
);

-- Une seule table pour les trois types d'utilisateurs :
--   specialite  -> médecin ; num_cnss et commission -> secrétaire
CREATE TABLE utilisateurs (
                              id_utilisateur BIGINT        NOT NULL AUTO_INCREMENT,
                              nom            VARCHAR(100)  NOT NULL,
                              prenom         VARCHAR(100)  NOT NULL,
                              login          VARCHAR(50)   NOT NULL,
                              mot_de_passe   VARCHAR(255)  NOT NULL,   -- hash PBKDF2, jamais le mot de passe en clair
                              actif          BOOLEAN       NOT NULL DEFAULT TRUE,
                              id_role        BIGINT        NOT NULL,
                              specialite     VARCHAR(100)  NULL,
                              num_cnss       VARCHAR(30)   NULL,
                              commission     DECIMAL(5,2)  NULL,
                              date_creation  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT pk_utilisateurs PRIMARY KEY (id_utilisateur),
                              CONSTRAINT uq_utilisateurs_login UNIQUE (login),
                              CONSTRAINT fk_utilisateurs_role FOREIGN KEY (id_role) REFERENCES roles (id_role)
);

-- ---------------------------------------------------------------------------
-- Rôles
-- ---------------------------------------------------------------------------
INSERT INTO roles (id_role, code, libelle) VALUES
                                               (1, 'ADMIN',      'Administrateur'),
                                               (2, 'MEDECIN',    'Médecin'),
                                               (3, 'SECRETAIRE', 'Secrétaire');

-- ---------------------------------------------------------------------------
-- Permissions par rôle (à ajuster selon la conception du groupe)
-- ---------------------------------------------------------------------------
INSERT INTO role_permissions (id_role, permission) VALUES
                                                       (1, 'GERER_UTILISATEURS'),
                                                       (1, 'GERER_ROLES'),
                                                       (2, 'GERER_PATIENTS'),
                                                       (2, 'GERER_CONSULTATIONS'),
                                                       (2, 'GERER_AGENDA'),
                                                       (3, 'GERER_PATIENTS'),
                                                       (3, 'GERER_AGENDA'),
                                                       (3, 'GERER_CAISSE');

-- ---------------------------------------------------------------------------
-- Comptes fictifs (mots de passe de test : admin123 / medecin123 / secretaire123)
-- ---------------------------------------------------------------------------
INSERT INTO utilisateurs (nom, prenom, login, mot_de_passe, actif, id_role, specialite, num_cnss, commission) VALUES
                                                                                                                  ('Admin',   'DentalSoft', 'admin',
                                                                                                                   'pbkdf2$120000$uFMkWAiuouY80VW378rMvQ==$X0DBhH+NycaHi8NOscSm6FDl9qU28F1vumHuZTjDjcY=',
                                                                                                                   TRUE, 1, NULL, NULL, NULL),
                                                                                                                  ('Alami',   'Sara',       'dr.alami',
                                                                                                                   'pbkdf2$120000$KS/a9QjaIO3D8Oradzsz0w==$8zo9oxpsIPuARhG+4BpgcqvH3A6kK6D/M26xySU/0Sk=',
                                                                                                                   TRUE, 2, 'Orthodontie', NULL, NULL),
                                                                                                                  ('Bennani', 'Imane',      'secretaire',
                                                                                                                   'pbkdf2$120000$kF0f4xfy0LX8AHblIs/pag==$+L72rTIcNc81HfoDfUG8d+vkCx8i1H3i8rPk6As/y7Q=',
                                                                                                                   TRUE, 3, NULL, '1234567', 5.00);
