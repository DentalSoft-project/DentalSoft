-- ============================================================================
-- 00_base_cabinet.sql
-- Initialisation de la base et configuration du cabinet
-- Cible : MySQL 8.0.16+ ; installation neuve, sans DROP ni données de test.
-- À exécuter avant tous les autres scripts de schema/.
-- Audit cree_par / modifie_par : références logiques, sans FK.
-- ============================================================================

CREATE DATABASE IF NOT EXISTS dentalsoftbd
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Cabinet médical ; les coordonnées reprennent les attributs UML
-- ----------------------------------------------------------------------------
CREATE TABLE cabinet_medical (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    nom                         VARCHAR(120)   NOT NULL,
    email                       VARCHAR(120)   NULL,
    logo_path                   VARCHAR(512)   NULL,
    adresse                     VARCHAR(255)   NULL,
    cin                         VARCHAR(30)    NULL,
    telephone1                  VARCHAR(20)    NULL,
    telephone2                  VARCHAR(20)    NULL,
    site_web                    VARCHAR(255)   NULL,
    instagram                   VARCHAR(255)   NULL,
    facebook                    VARCHAR(255)   NULL,
    description                 TEXT           NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
