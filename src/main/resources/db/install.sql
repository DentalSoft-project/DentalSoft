-- ============================================================================
-- DentalSoft — Installation neuve du schéma complet
-- Client requis : mysql (SOURCE est une commande client, pas du SQL JDBC).
-- Lancer depuis la RACINE DU PROJET :
--   mysql --default-character-set=utf8mb4 -u <utilisateur> -p
--         -e "SOURCE src/main/resources/db/install.sql;"
-- Le mode -e s'arrête sur erreur ; ne pas utiliser --force.
-- Base : dentalsoftbd ; aucune suppression et aucun seed automatique.
-- Ne pas utiliser comme migration sur une base déjà installée.
-- Définitions uniques dans schema/*.sql ; voir README.md pour les limites.
-- ============================================================================

SOURCE src/main/resources/db/schema/00_base_cabinet.sql;
SOURCE src/main/resources/db/schema/01_referentiels.sql;
SOURCE src/main/resources/db/schema/02_utilisateurs_roles.sql;
SOURCE src/main/resources/db/schema/03_patients_dossiers.sql;
SOURCE src/main/resources/db/schema/04_agenda_rdv.sql;
SOURCE src/main/resources/db/schema/05_consultations_interventions.sql;
SOURCE src/main/resources/db/schema/06_documents_medicaux.sql;
SOURCE src/main/resources/db/schema/07_factures.sql;
SOURCE src/main/resources/db/schema/08_paiements.sql;
SOURCE src/main/resources/db/schema/09_caisse.sql;
SOURCE src/main/resources/db/schema/10_notifications.sql;
SOURCE src/main/resources/db/schema/11_vues_financieres.sql;
