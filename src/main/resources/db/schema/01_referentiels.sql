-- ============================================================================
-- 01_referentiels.sql
-- M2 : catalogues de référence
-- Dépend de : 00_base_cabinet.sql (base dentalsoftbd)
-- Cible : MySQL 8.0.16+ ; installation neuve, sans DROP ni données de test.
-- Ordre complet : voir ../install.sql, à lancer depuis la racine du projet.
-- Audit cree_par / modifie_par : références logiques, sans FK.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Référentiel des assurances (structure existante conservée)
-- ----------------------------------------------------------------------------
CREATE TABLE assurance (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    libelle                     VARCHAR(80)    NOT NULL,
    taux_remboursement          DECIMAL(5,2)   NOT NULL DEFAULT 0.00,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_assurance_libelle UNIQUE (libelle),
    CONSTRAINT chk_assurance_taux
        CHECK (taux_remboursement >= 0 AND taux_remboursement <= 100)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Catalogue des actes ; les interventions conservent leurs propres prix/libellés
-- ----------------------------------------------------------------------------
CREATE TABLE acte (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    libelle                     VARCHAR(120)   NOT NULL,
    categorie                   VARCHAR(100)   NOT NULL,
    prix_base                   DECIMAL(12,2)  NOT NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    KEY idx_acte_libelle (libelle),
    CONSTRAINT chk_acte_prix
        CHECK (prix_base >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Catalogue des antécédents ; risque selon le dossier de spécifications
-- ----------------------------------------------------------------------------
CREATE TABLE antecedent (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    nom                         VARCHAR(120)   NOT NULL,
    categorie                   VARCHAR(100)   NOT NULL,
    niveau_risque               ENUM('FAIBLE','MOYEN','ELEVE') NOT NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    KEY idx_antecedent_nom (nom)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Catalogue des médicaments ; valeurs de forme à définir côté métier
-- ----------------------------------------------------------------------------
CREATE TABLE medicament (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    nom                         VARCHAR(120)   NOT NULL,
    laboratoire                 VARCHAR(120)   NULL,
    type                        VARCHAR(80)    NULL,
    forme                       VARCHAR(30)    NULL,
    remboursable                BOOLEAN        NOT NULL DEFAULT FALSE,
    prix_unitaire               DECIMAL(12,2)  NOT NULL,
    description                 TEXT           NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    KEY idx_medicament_nom (nom),
    CONSTRAINT chk_medicament_prix
        CHECK (prix_unitaire >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
