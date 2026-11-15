-- ============================================================================
-- 06_documents_medicaux.sql
-- M5 : ordonnances, prescriptions, certificats et documents
-- Cible : MySQL 8.0.16+ ; installation neuve uniquement.
-- Dépend de : 01_referentiels.sql, 02_utilisateurs_roles.sql,
--             03_patients_dossiers.sql, 05_consultations_interventions.sql
-- Audit cree_par / modifie_par : références logiques vers utilisateurs.
-- Aucun nettoyage : les tables existantes ne sont pas supprimées.
-- Le service vérifie le dossier commun à chaque document et sa consultation.
-- Les codes type/statut non définis par le diagramme restent à valider.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Ordonnance ; le service garantit dossier cohérent et au moins une prescription
-- ----------------------------------------------------------------------------
CREATE TABLE ordonnance (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    dossier_id                  BIGINT         NOT NULL,
    consultation_id             BIGINT         NOT NULL,
    date_ordonnance             DATE           NOT NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_ordonnance_dossier
        FOREIGN KEY (dossier_id) REFERENCES dossier_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_ordonnance_consultation
        FOREIGN KEY (consultation_id) REFERENCES consultation (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Ligne de prescription ; fréquence textuelle, durée en jours
-- ----------------------------------------------------------------------------
CREATE TABLE prescription (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    ordonnance_id               BIGINT         NOT NULL,
    medicament_id               BIGINT         NOT NULL,
    quantite                    INT            NOT NULL,
    frequence                   VARCHAR(120)   NOT NULL,
    duree_jours                 INT            NOT NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_prescription_ordonnance
        FOREIGN KEY (ordonnance_id) REFERENCES ordonnance (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_prescription_medicament
        FOREIGN KEY (medicament_id) REFERENCES medicament (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_prescription_quantite
        CHECK (quantite > 0),
    CONSTRAINT chk_prescription_duree
        CHECK (duree_jours > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Certificat ; durée calculée par le service, jamais saisie librement.
-- M5 doit préciser la convention de comptage des jours (bornes incluses ou non).
-- ----------------------------------------------------------------------------
CREATE TABLE certificat (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    dossier_id                  BIGINT         NOT NULL,
    consultation_id             BIGINT         NOT NULL,
    date_debut                  DATE           NOT NULL,
    date_fin                    DATE           NOT NULL,
    duree_jours                 INT            NOT NULL,
    motif                       TEXT           NULL,
    note_medecin                TEXT           NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_certificat_dossier
        FOREIGN KEY (dossier_id) REFERENCES dossier_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_certificat_consultation
        FOREIGN KEY (consultation_id) REFERENCES consultation (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_certificat_dates
        CHECK (date_fin >= date_debut),
    CONSTRAINT chk_certificat_duree
        CHECK (duree_jours >= 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Document médical : métadonnées et fichier ; accès clinique contrôlé au service
-- ----------------------------------------------------------------------------
CREATE TABLE document_medical (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    dossier_id                  BIGINT         NOT NULL,
    auteur_id                   BIGINT         NOT NULL,
    type                        VARCHAR(30)    NOT NULL,
    titre                       VARCHAR(255)   NOT NULL,
    contenu                     TEXT           NULL,
    statut                      VARCHAR(30)    NOT NULL,
    date_emission               DATETIME       NULL,
    date_expiration             DATETIME       NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    KEY idx_document_dossier (dossier_id),
    CONSTRAINT fk_document_dossier
        FOREIGN KEY (dossier_id) REFERENCES dossier_medical (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_document_auteur
        FOREIGN KEY (auteur_id) REFERENCES utilisateurs (id_utilisateur)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_document_dates
        CHECK (date_emission IS NULL OR date_expiration IS NULL OR date_expiration >= date_emission)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
