-- ============================================================================
-- 07_factures.sql
-- M6 : situation financière persistée et factures
-- Cible : MySQL 8.0.16+ ; installation neuve uniquement.
-- Dépend de : 03_patients_dossiers.sql, 05_consultations_interventions.sql
-- Audit cree_par / modifie_par : références logiques vers utilisateurs.
-- Aucun nettoyage : les tables existantes ne sont pas supprimées.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- SF persistée : une par dossier, choix approuvé.
-- total_actes = factures VALIDEE ; total_paye = paiements associés ; credit = reste dû.
-- Le service met à jour les trois montants atomiquement ; les CHECK ne comparent
-- pas les agrégats aux autres tables. Codes statut/en_promo non inventés.
-- ----------------------------------------------------------------------------
CREATE TABLE situation_financiere (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    id_dossier                  BIGINT         NOT NULL,
    total_actes                 DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    total_paye                  DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    credit                      DECIMAL(12,2)  NOT NULL DEFAULT 0.00,
    statut                      VARCHAR(30)    NULL,
    en_promo                    VARCHAR(30)    NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sf_dossier UNIQUE (id_dossier),
    CONSTRAINT fk_sf_dossier
        FOREIGN KEY (id_dossier) REFERENCES dossier_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_sf_total_actes
        CHECK (total_actes >= 0),
    CONSTRAINT chk_sf_total_paye
        CHECK (total_paye >= 0),
    CONSTRAINT chk_sf_credit
        CHECK (credit >= 0),
    CONSTRAINT chk_sf_solde
        CHECK (credit = total_actes - total_paye)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Une facture par consultation (RG-09), aucune relation directe aux interventions.
-- Le service impose consultation TERMINEE avec interventions, même dossier que SF,
-- total calculé/figé, droits et règles d'annulation.
-- Cycle documentaire distinct du règlement : voir v_facture_reglement.
-- ----------------------------------------------------------------------------
CREATE TABLE facture (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    consultation_id             BIGINT         NOT NULL,
    id_sf                       BIGINT         NOT NULL,
    numero                      VARCHAR(50)    NOT NULL,
    date_facture                DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total                       DECIMAL(12,2)  NOT NULL,
    statut                      ENUM('BROUILLON','VALIDEE','ANNULEE') NOT NULL DEFAULT 'BROUILLON',
    motif_annulation            VARCHAR(255)   NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_facture_consultation UNIQUE (consultation_id),
    CONSTRAINT uk_facture_numero UNIQUE (numero),
    KEY idx_facture_sf (id_sf),
    KEY idx_facture_statut (statut),
    KEY idx_facture_date (date_facture),
    CONSTRAINT fk_facture_consultation
        FOREIGN KEY (consultation_id) REFERENCES consultation (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_facture_sf
        FOREIGN KEY (id_sf) REFERENCES situation_financiere (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_facture_total
        CHECK (total >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
