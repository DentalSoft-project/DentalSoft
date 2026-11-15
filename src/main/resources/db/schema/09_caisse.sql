-- ============================================================================
-- 09_caisse.sql
-- M6 : charges et revenus du cabinet
-- Cible : MySQL 8.0.16+ ; installation neuve uniquement.
-- Dépend de : 00_base_cabinet.sql, 02_utilisateurs_roles.sql, 08_paiements.sql
-- Audit cree_par / modifie_par : références logiques vers utilisateurs.
-- Aucun nettoyage : les tables existantes ne sont pas supprimées.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Charges du cabinet ; distinctes des paiements encaissés
-- ----------------------------------------------------------------------------
CREATE TABLE charge (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    cabinet_id                  BIGINT         NOT NULL,
    auteur_id                   BIGINT         NOT NULL,
    titre                       VARCHAR(120)   NOT NULL,
    description                 TEXT           NULL,
    montant                     DECIMAL(12,2)  NOT NULL,
    date_charge                 DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    KEY idx_charge_cabinet_date (cabinet_id, date_charge),
    CONSTRAINT fk_charge_cabinet
        FOREIGN KEY (cabinet_id) REFERENCES cabinet_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_charge_auteur
        FOREIGN KEY (auteur_id) REFERENCES utilisateurs (id_utilisateur)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_charge_montant
        CHECK (montant > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Revenus du cabinet ; un paiement ne produit pas deux revenus.
-- Le service vérifie montant, auteur/cabinet et date associés au paiement ;
-- insertion paiement/revenu et agrégats dans une même transaction.
-- Ne pas additionner les paiements à leurs revenus pour compter les recettes.
-- ----------------------------------------------------------------------------
CREATE TABLE revenu (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    cabinet_id                  BIGINT         NOT NULL,
    auteur_id                   BIGINT         NOT NULL,
    paiement_id                 BIGINT         NULL,
    titre                       VARCHAR(120)   NOT NULL,
    description                 TEXT           NULL,
    montant                     DECIMAL(12,2)  NOT NULL,
    date_revenu                 DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_revenu_paiement UNIQUE (paiement_id),
    KEY idx_revenu_cabinet_date (cabinet_id, date_revenu),
    CONSTRAINT fk_revenu_cabinet
        FOREIGN KEY (cabinet_id) REFERENCES cabinet_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_revenu_auteur
        FOREIGN KEY (auteur_id) REFERENCES utilisateurs (id_utilisateur)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_revenu_montant
        CHECK (montant > 0),
    CONSTRAINT fk_revenu_paiement
        FOREIGN KEY (paiement_id) REFERENCES paiement (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
