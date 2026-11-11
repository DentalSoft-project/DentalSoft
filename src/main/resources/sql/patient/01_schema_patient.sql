SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS dentalsoftBD
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE dentalsoftBD;

-- ----------------------------------------------------------------------------
-- 1) Table de référence des assurances
--    Périmètre M2 (référentiels) : créée ici en provisoire car
--    patient.id_assurance en dépend. Khalil peut l'étendre ou la remplacer.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS assurance (
                                         id                         BIGINT       NOT NULL AUTO_INCREMENT,
                                         libelle                    VARCHAR(80)  NOT NULL,
    taux_remboursement         DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    date_creation              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification DATETIME     NULL DEFAULT NULL,
    cree_par                   BIGINT       NULL,
    modifie_par                BIGINT       NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_assurance_libelle (libelle),
    CONSTRAINT chk_assurance_taux
    CHECK (taux_remboursement >= 0 AND taux_remboursement <= 100)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------------------------------------------------------
-- 2) Patients (identité administrative)
--    - cin nullable mais UNIQUE quand renseigné (plusieurs NULL autorisés)
--    - archive : RG-04, on n'efface jamais un patient
--    - pas de UNIQUE(nom, date_naissance) : deux homonymes peuvent exister ;
--      le doublon est détecté dans PatientService (index dédié ci-dessous)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS patient (
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
    UNIQUE KEY uk_patient_cin (cin),
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
    COLLATE = utf8mb4_0900_ai_ci;

-- ----------------------------------------------------------------------------
-- 3) Dossier médical (1 patient <-> 1 dossier, RG-01)
--    - patient_id UNIQUE : garantit l'unicité au niveau base
--    - ON DELETE RESTRICT : un patient avec dossier ne peut pas être supprimé
--    - pas de medecin_id pour l'instant (table medecin du lot M1 absente)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS dossier_medical (
                                               id                         BIGINT   NOT NULL AUTO_INCREMENT,
                                               patient_id                 BIGINT   NOT NULL,
                                               date_creation              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               date_derniere_modification DATETIME NULL DEFAULT NULL,
                                               cree_par                   BIGINT   NULL,
                                               modifie_par                BIGINT   NULL,
                                               PRIMARY KEY (id),
    UNIQUE KEY uk_dossier_patient (patient_id),
    CONSTRAINT fk_dossier_patient
    FOREIGN KEY (patient_id) REFERENCES patient (id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;