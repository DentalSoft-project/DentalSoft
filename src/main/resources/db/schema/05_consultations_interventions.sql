-- ============================================================================
-- 05_consultations_interventions.sql
-- M5 : consultations et interventions
-- Cible : MySQL 8.0.16+ ; installation neuve uniquement.
-- Dépend de : 01_referentiels.sql, 02_utilisateurs_roles.sql, 03_patients_dossiers.sql, 04_agenda_rdv.sql
-- Audit cree_par / modifie_par : références logiques vers utilisateurs.
-- Aucun nettoyage : les tables existantes ne sont pas supprimées.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Consultation ; RDV facultatif et unique (RG-06).
-- Décision approuvée : TERMINEE représente la validation requise pour facturer.
-- Le service contrôle médecin, cohérence dossier/médecin du RDV et transitions.
-- ----------------------------------------------------------------------------
CREATE TABLE consultation (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    dossier_id                  BIGINT         NOT NULL,
    medecin_id                  BIGINT         NOT NULL,
    rdv_id                      BIGINT         NULL,
    date_consultation           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observation                 TEXT           NULL,
    statut                      ENUM('EN_COURS','TERMINEE','ANNULEE') NOT NULL DEFAULT 'EN_COURS',
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_consultation_rdv UNIQUE (rdv_id),
    KEY idx_consultation_dossier_date (dossier_id, date_consultation),
    KEY idx_consultation_medecin (medecin_id),
    CONSTRAINT fk_consultation_dossier
        FOREIGN KEY (dossier_id) REFERENCES dossier_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_consultation_medecin
        FOREIGN KEY (medecin_id) REFERENCES utilisateurs (id_utilisateur)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_consultation_rdv
        FOREIGN KEY (rdv_id) REFERENCES rdv (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Actes réalisés : quantité, libellé et prix appliqués conservés (RG-07/08).
-- Le service interdit les modifications après validation/facturation.
-- ----------------------------------------------------------------------------
CREATE TABLE intervention (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    consultation_id             BIGINT         NOT NULL,
    acte_id                     BIGINT         NOT NULL,
    libelle_acte_snapshot       VARCHAR(120)   NOT NULL,
    dent                        VARCHAR(10)    NULL,
    quantite                    INT            NOT NULL,
    prix_unitaire               DECIMAL(12,2)  NOT NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    KEY idx_intervention_consultation (consultation_id),
    CONSTRAINT fk_intervention_consultation
        FOREIGN KEY (consultation_id) REFERENCES consultation (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_intervention_acte
        FOREIGN KEY (acte_id) REFERENCES acte (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_intervention_quantite
        CHECK (quantite > 0),
    CONSTRAINT chk_intervention_prix
        CHECK (prix_unitaire >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
