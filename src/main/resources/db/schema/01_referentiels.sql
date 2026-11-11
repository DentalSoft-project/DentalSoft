-- ============================================================================
-- 01_referentiels.sql
-- Création de la base + nettoyage + tables de référence (assurance)
-- ATTENTION : détruit et recrée TOUTES les tables du projet.
-- À exécuter en premier (ordre : 01 -> 02 -> 03 -> seed).
-- ============================================================================

CREATE DATABASE IF NOT EXISTS dentalsoftbd
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Nettoyage global (enfant -> parent)
-- ----------------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS dossier_medical;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS assurance;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS utilisateurs;
DROP TABLE IF EXISTS roles;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------------
-- Table de référence des assurances
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
