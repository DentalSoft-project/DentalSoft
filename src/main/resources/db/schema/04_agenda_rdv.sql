-- ============================================================================
-- 04_agenda_rdv.sql
-- M4 : agenda mensuel, indisponibilités et RDV
-- Cible : MySQL 8.0.16+ ; installation neuve uniquement.
-- Dépend de : 02_utilisateurs_roles.sql, 03_patients_dossiers.sql
-- Audit cree_par / modifie_par : références logiques vers utilisateurs.
-- Aucun nettoyage : les tables existantes ne sont pas supprimées.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Agenda mensuel ; le service contrôle le type MEDECIN
-- ----------------------------------------------------------------------------
CREATE TABLE agenda_mensuel (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    medecin_id                  BIGINT         NOT NULL,
    annee                       SMALLINT       NOT NULL,
    mois                        TINYINT        NOT NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_agenda_medecin_periode UNIQUE (medecin_id, annee, mois),
    CONSTRAINT fk_agenda_medecin
        FOREIGN KEY (medecin_id) REFERENCES utilisateurs (id_utilisateur)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_agenda_annee
        CHECK (annee BETWEEN 1000 AND 9999),
    CONSTRAINT chk_agenda_mois
        CHECK (mois BETWEEN 1 AND 12)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Jours indisponibles ; le service vérifie le mois/année et les RDV existants
-- ----------------------------------------------------------------------------
CREATE TABLE jour_indisponible (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    agenda_id                   BIGINT         NOT NULL,
    date_jour                   DATE           NOT NULL,
    motif                       VARCHAR(255)   NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_jour_agenda_date UNIQUE (agenda_id, date_jour),
    CONSTRAINT fk_jour_agenda
        FOREIGN KEY (agenda_id) REFERENCES agenda_mensuel (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- RDV ; codes de statut à valider côté métier, sans défaut inventé.
-- Les chevauchements, disponibilités et droits exigent un contrôle transactionnel.
-- ----------------------------------------------------------------------------
CREATE TABLE rdv (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    dossier_id                  BIGINT         NOT NULL,
    medecin_id                  BIGINT         NOT NULL,
    debut                       DATETIME       NOT NULL,
    fin                         DATETIME       NOT NULL,
    motif                       VARCHAR(255)   NULL,
    note_medecin                TEXT           NULL,
    statut                      VARCHAR(30)    NOT NULL,
    arrivee_at                  DATETIME       NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    KEY idx_rdv_medecin_debut (medecin_id, debut),
    KEY idx_rdv_dossier_debut (dossier_id, debut),
    KEY idx_rdv_statut_debut (statut, debut),
    CONSTRAINT fk_rdv_dossier
        FOREIGN KEY (dossier_id) REFERENCES dossier_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_rdv_medecin
        FOREIGN KEY (medecin_id) REFERENCES utilisateurs (id_utilisateur)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_rdv_periode
        CHECK (debut < fin)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
