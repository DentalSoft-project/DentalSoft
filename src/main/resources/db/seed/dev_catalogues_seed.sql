-- ============================================================================
-- dev_catalogues_seed.sql — catalogues fictifs de développement uniquement
-- À exécuter APRÈS install.sql, une seule fois sur une installation neuve.
-- Complète dev_seed.sql sans créer de patient ni de donnée financière.
-- Libellés, prix et niveaux de risque : exemples de démonstration uniquement.
-- Aucun médicament réel, dosage, prescription ou conseil clinique.
-- Identifiants laissés à AUTO_INCREMENT ; aucune dépendance à un ID fixe.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

START TRANSACTION;

-- ----------------------------------------------------------------------------
-- Catalogue des actes : prix fictifs en DH, sans valeur tarifaire de référence
-- ----------------------------------------------------------------------------
INSERT INTO acte (libelle, categorie, prix_base) VALUES
    ('DEMO - Consultation dentaire', 'CONSULTATION', 150.00),
    ('DEMO - Détartrage',            'PREVENTION',   300.00),
    ('DEMO - Soin conservateur',     'SOINS',        450.00);

-- ----------------------------------------------------------------------------
-- Catalogue des antécédents : libellés inventés pour tester les trois risques
-- Aucun lien avec un patient ; aucune classification médicale réelle
-- ----------------------------------------------------------------------------
INSERT INTO antecedent (nom, categorie, niveau_risque) VALUES
    ('DEMO - Antécédent Alpha', 'DEMONSTRATION', 'FAIBLE'),
    ('DEMO - Antécédent Beta',  'DEMONSTRATION', 'MOYEN'),
    ('DEMO - Antécédent Gamma', 'DEMONSTRATION', 'ELEVE');

-- ----------------------------------------------------------------------------
-- Catalogue des médicaments : produits entièrement fictifs
-- Type/forme restent NULL : aucun code métier non validé n'est inventé
-- ----------------------------------------------------------------------------
INSERT INTO medicament (nom, laboratoire, remboursable, prix_unitaire, description) VALUES
    ('DEMO - Médicament Alpha', 'Laboratoire fictif', FALSE, 10.00, 'Produit fictif pour démonstration logicielle uniquement.'),
    ('DEMO - Médicament Beta',  'Laboratoire fictif', FALSE, 20.00, 'Produit fictif pour démonstration logicielle uniquement.'),
    ('DEMO - Médicament Gamma', 'Laboratoire fictif', FALSE, 30.00, 'Produit fictif pour démonstration logicielle uniquement.');

COMMIT;
