-- ============================================================================
-- 03_patients_dossiers.sql
-- M2 : patients et dossier médical
-- Dépend de : 01_referentiels.sql (assurance)
-- NOTE : cree_par / modifie_par sont des références LOGIQUES vers
--        utilisateurs.id_utilisateur (aucune FK déclarée volontairement,
--        la cohérence est assurée côté service applicatif).
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Patients (identité administrative)
--   - cin nullable mais UNIQUE quand renseigné (plusieurs NULL autorisés)
--   - archive : RG-04, on n'efface jamais un patient
--   - pas de UNIQUE(nom, date_naissance) : deux homonymes peuvent exister ;
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
-- Dossier médical (1 patient <-> 1 dossier, RG-01)
--   - patient_id UNIQUE : garantit l'unicité au niveau base
--   - ON DELETE RESTRICT : un patient avec dossier ne peut pas être supprimé
--   - medecin_id sera ajouté plus tard (M3)
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
