-- ============================================================================
-- DentalSoft — Schéma complet (M1 : utilisateurs/rôles/permissions
--                            + M2 : assurance/patient/dossier_medical)
-- ----------------------------------------------------------------------------
-- Cible      : MySQL 8.0.16+ (CHECK réellement appliqués)
-- Charset    : utf8mb4
-- Collation  : utf8mb4_unicode_ci (compatible 5.7 et 8.0)
-- Base       : dentalsoftbd  (minuscules, partout)
-- ----------------------------------------------------------------------------
-- ATTENTION : ce script DÉTRUIT et RECRÉE toutes les tables.
--             Ne pas exécuter en production sans sauvegarde.
-- ----------------------------------------------------------------------------
-- Ordre d'exécution : ce fichier unique, une seule fois.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS dentalsoftbd
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Drop dans l'ordre enfant → parent (évite les erreurs de FK)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS dossier_medical;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS assurance;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS utilisateurs;
DROP TABLE IF EXISTS roles;

-- ============================================================================
-- M1 — Utilisateurs, rôles, permissions
-- ============================================================================

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

-- ============================================================================
-- M2 — Référentiel assurances, patients, dossier médical
-- ============================================================================
-- NOTE : cree_par / modifie_par sont des références LOGIQUES vers
--        utilisateurs.id_utilisateur. Aucune FK n'est déclarée volontairement
--        pour éviter les cycles et faciliter la purge des comptes.
--        La cohérence est assurée côté service applicatif.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- Table de référence des assurances (M2 référentiels)
-- ----------------------------------------------------------------------------
CREATE TABLE assurance (
                           id                         BIGINT       NOT NULL AUTO_INCREMENT,
                           libelle                    VARCHAR(80)  NOT NULL,
                           taux_remboursement         DECIMAL(5,2) NOT NULL DEFAULT 0.00,
                           date_creation              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           date_derniere_modification DATETIME     NULL DEFAULT NULL,
                           cree_par                   BIGINT       NULL,
                           modifie_par                BIGINT       NULL,
                           PRIMARY KEY (id),
                           CONSTRAINT uk_assurance_libelle UNIQUE (libelle),
                           CONSTRAINT chk_assurance_taux
                               CHECK (taux_remboursement >= 0 AND taux_remboursement <= 100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Patients (identité administrative). Cin nullable mais UNIQUE quand renseigner (plusieurs NULL autorisés). Archive : RG-04, on n'efface jamais un patient
--   — pas de UNIQUE(nom, date_naissance) : deux homonymes peuvent exister ;
--     le doublon est détecté dans PatientService (index dédié ci-dessous)
-- ----------------------------------------------------------------------------
CREATE TABLE patient (
                         id                         BIGINT        NOT NULL AUTO_INCREMENT,
                         nom                        VARCHAR(80)   NOT NULL,
                         prenom                     VARCHAR(80)   NOT NULL,
                         date_naissance             DATE          NOT NULL,
                         sexe                       ENUM('M','F') NOT NULL,
                         cin                        VARCHAR(20)   NULL,
                         telephone                  VARCHAR(20)   NOT NULL,
                         email                      VARCHAR(120)  NULL,
                         adresse                    VARCHAR(255)  NULL,
                         id_assurance               BIGINT        NULL,
                         archive                    BOOLEAN       NOT NULL DEFAULT FALSE,
                         date_creation              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         date_derniere_modification DATETIME      NULL DEFAULT NULL,
                         cree_par                   BIGINT        NULL,
                         modifie_par                BIGINT        NULL,
                         PRIMARY KEY (id),
                         CONSTRAINT uk_patient_cin UNIQUE (cin),
                         KEY idx_patient_nom_prenom (nom, prenom),
                         KEY idx_patient_doublon (nom, prenom, date_naissance),
                         KEY idx_patient_telephone (telephone),
                         KEY idx_patient_archive (archive),
                         CONSTRAINT fk_patient_assurance
                             FOREIGN KEY (id_assurance) REFERENCES assurance (id)
                                 ON UPDATE CASCADE
                                 ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Dossier médical (1 patient <> 1 dossier, RG-01)
--    patient_id UNIQUE : garantit l'unicité au niveau base
--   -ON DELETE RESTRICT : un patient avec dossier ne peut pas être supprimé
--   medecin_id sera ajouté en M3 quand la table medecin existera
-- ----------------------------------------------------------------------------
CREATE TABLE dossier_medical (
                                 id                         BIGINT   NOT NULL AUTO_INCREMENT,
                                 patient_id                 BIGINT   NOT NULL,
                                 date_creation              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 date_derniere_modification DATETIME NULL DEFAULT NULL,
                                 cree_par                   BIGINT   NULL,
                                 modifie_par                BIGINT   NULL,
                                 PRIMARY KEY (id),
                                 CONSTRAINT uk_dossier_patient UNIQUE (patient_id),
                                 CONSTRAINT fk_dossier_patient
                                     FOREIGN KEY (patient_id) REFERENCES patient (id)
                                         ON UPDATE CASCADE
                                         ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- Données de référence / comptes de test
-- ============================================================================

-- Rôles (sans id_role : laissé à l'AUTO_INCREMENT)
INSERT INTO roles (code, libelle) VALUES
                                      ('ADMIN',      'Administrateur'),
                                      ('MEDECIN',    'Médecin'),
                                      ('SECRETAIRE', 'Secrétaire');

-- Permissions par rôle (à ajuster selon la conception du groupe)
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_UTILISATEURS' FROM roles WHERE code = 'ADMIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_ROLES'        FROM roles WHERE code = 'ADMIN';

INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_PATIENTS'     FROM roles WHERE code = 'MEDECIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_CONSULTATIONS' FROM roles WHERE code = 'MEDECIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_AGENDA'       FROM roles WHERE code = 'MEDECIN';

INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_PATIENTS'     FROM roles WHERE code = 'SECRETAIRE';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_AGENDA'       FROM roles WHERE code = 'SECRETAIRE';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_CAISSE'       FROM roles WHERE code = 'SECRETAIRE';

-- Comptes fictifs (mots de passe de test : admin123 / medecin123 / secretaire123)
INSERT INTO utilisateurs
(nom, prenom, login, mot_de_passe, actif, id_role, specialite, num_cnss, commission)
SELECT 'Admin', 'DentalSoft', 'admin',
       'pbkdf2$120000$uFMkWAiuouY80VW378rMvQ==$X0DBhH+NycaHi8NOscSm6FDl9qU28F1vumHuZTjDjcY=',
       TRUE, r.id_role, NULL, NULL, NULL
FROM roles r WHERE r.code = 'ADMIN';

INSERT INTO utilisateurs
(nom, prenom, login, mot_de_passe, actif, id_role, specialite, num_cnss, commission)
SELECT 'Alami', 'Sara', 'dr.alami',
       'pbkdf2$120000$KS/a9QjaIO3D8Oradzsz0w==$8zo9oxpsIPuARhG+4BpgcqvH3A6kK6D/M26xySU/0Sk=',
       TRUE, r.id_role, 'Orthodontie', NULL, NULL
FROM roles r WHERE r.code = 'MEDECIN';

INSERT INTO utilisateurs
(nom, prenom, login, mot_de_passe, actif, id_role, specialite, num_cnss, commission)
SELECT 'Bennani', 'Imane', 'secretaire',
       'pbkdf2$120000$kF0f4xfy0LX8AHblIs/pag==$+L72rTIcNc81HfoDfUG8d+vkCx8i1H3i8rPk6As/y7Q=',
       TRUE, r.id_role, NULL, '1234567', 5.00
FROM roles r WHERE r.code = 'SECRETAIRE';