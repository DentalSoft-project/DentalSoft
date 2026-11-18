-- ============================================================================
-- assurances_seed.sql
-- À exécuter sur une base DÉJÀ créée et alimentée (ne touche ni aux patients ni aux comptes).
-- ============================================================================
USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Assurances fictives (liste déroulante du formulaire patient, T05)
-- INSERT IGNORE : relançable sans doublon (libelle est UNIQUE)
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO assurance (libelle, taux_remboursement) VALUES
    ('CNSS', 70.00),
    ('CNOPS', 80.00),
    ('AXA Assurance Maroc', 75.00),
    ('Wafa Assurance', 75.00),
    ('RMA', 70.00),
    ('Saham Assurance', 70.00);
