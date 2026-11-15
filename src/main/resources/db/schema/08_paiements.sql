-- ============================================================================
-- 08_paiements.sql
-- M6 : paiements des factures
-- Cible : MySQL 8.0.16+ (CHECK réellement appliqués)
-- Dépend de : 02_utilisateurs_roles.sql (utilisateurs)
--             07_factures.sql (situation_financiere, facture)
-- Ordre complet : voir ../install.sql.
-- Ajout uniquement : aucune table ni donnée existante n'est supprimée.
-- NOTE : cree_par / modifie_par sont des références LOGIQUES vers
--        utilisateurs.id_utilisateur, comme dans 03_patients_dossiers.sql.
--        auteur_id est une référence métier avec FK, distincte de l'audit.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Paiements (RG-08 / RG-10)
--   - montant : DECIMAL(12,2), strictement positif
--   - reference : identifiant de soumission, réutilisé lors d'un retry
--   - auteur_id : utilisateur authentifié ayant enregistré le paiement
--   - ON DELETE RESTRICT : préserver les liens financiers et leur auteur
--   - le service vérifie les droits, l'état de facture et montant <= reste
--     dans une transaction JDBC avec verrouillage de la facture ; il traite
--     une référence déjà reçue sans créer de deuxième encaissement
--   - paiements non modifiables/supprimables par les services (F11.2) ;
--     les colonnes d'audit communes n'autorisent pas leur modification
-- ----------------------------------------------------------------------------
CREATE TABLE paiement (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    facture_id                  BIGINT         NOT NULL,
    auteur_id                   BIGINT         NOT NULL,
    reference                   CHAR(36)       NOT NULL,
    date_paiement               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    montant                     DECIMAL(12,2)  NOT NULL,
    mode                        ENUM('ESPECES','CARTE','CHEQUE','VIREMENT') NOT NULL,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_paiement_reference UNIQUE (reference),
    KEY idx_paiement_facture (facture_id),
    KEY idx_paiement_auteur (auteur_id),
    KEY idx_paiement_date (date_paiement),
    CONSTRAINT fk_paiement_facture
        FOREIGN KEY (facture_id) REFERENCES facture (id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT fk_paiement_auteur
        FOREIGN KEY (auteur_id) REFERENCES utilisateurs (id_utilisateur)
            ON UPDATE CASCADE
            ON DELETE RESTRICT,
    CONSTRAINT chk_paiement_montant
        CHECK (montant > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
