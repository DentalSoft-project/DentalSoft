-- ============================================================================
-- Vérification du schéma sur une INSTANCE MYSQL JETABLE uniquement.
-- Prérequis : install.sql puis seed/dev_seed.sql, exécutés une seule fois.
-- Compatible avec le catalogue optionnel seed/dev_catalogues_seed.sql.
-- Lancer depuis la racine : mysql ... -e "SOURCE src/test/resources/db/schema_checks.sql;"
-- Les fixtures métier sont annulées par ROLLBACK si tous les contrôles passent.
-- En cas d'échec, abandonner l'instance jetable ; ne pas relancer sur une base réelle.
-- Ces tests vérifient le DDL, pas les règles transactionnelles des futurs services.
-- ============================================================================
USE dentalsoftbd;
SET NAMES utf8mb4;
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION';
SET @checks = 0;

DELIMITER $$
CREATE PROCEDURE test_assert(IN p_condition BOOLEAN, IN p_message VARCHAR(255))
BEGIN
    IF p_condition IS NULL OR NOT p_condition THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = p_message;
    END IF;
    SET @checks = @checks + 1;
END$$

CREATE PROCEDURE test_expect_error(IN p_sql TEXT, IN p_expected INT)
BEGIN
    DECLARE v_errno INT DEFAULT 0;
    DECLARE v_prepared BOOLEAN DEFAULT FALSE;
    DECLARE v_message VARCHAR(255);
    SET @test_sql = p_sql;
    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION
            GET DIAGNOSTICS CONDITION 1 v_errno = MYSQL_ERRNO;
        PREPARE test_statement FROM @test_sql;
        SET v_prepared = TRUE;
        EXECUTE test_statement;
    END;
    IF v_prepared THEN
        DEALLOCATE PREPARE test_statement;
    END IF;
    IF v_errno <> p_expected THEN
        SET v_message = CONCAT('Expected MySQL error ', p_expected, ', received ', v_errno);
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = v_message;
    END IF;
    SET @checks = @checks + 1;
END$$
DELIMITER ;

CALL test_assert((SELECT COUNT(*) = 26 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'), 'Expected 26 tables');
CALL test_assert((SELECT COUNT(*) = 3 FROM utilisateurs u JOIN roles r ON r.id_role = u.id_role), 'Expected three seed users with valid roles');
SET @medecin = (SELECT id_utilisateur FROM utilisateurs WHERE login = 'dr.alami');
SET @auteur = (SELECT id_utilisateur FROM utilisateurs WHERE login = 'secretaire');
SET @cabinet = (SELECT cabinet_id FROM utilisateurs WHERE id_utilisateur = @medecin);

START TRANSACTION;
INSERT INTO patient (nom, prenom, date_naissance, sexe, telephone)
VALUES ('Fixture', 'Schema', '2000-01-01', 'F', '0600000000');
SET @patient = LAST_INSERT_ID();
INSERT INTO dossier_medical (patient_id) VALUES (@patient);
SET @dossier = LAST_INSERT_ID();
INSERT INTO situation_financiere (id_dossier) VALUES (@dossier);
SET @sf = LAST_INSERT_ID();
INSERT INTO acte (libelle, categorie, prix_base) VALUES ('Acte fictif', 'TEST', 850.00);
SET @acte = LAST_INSERT_ID();
INSERT INTO antecedent (nom, categorie, niveau_risque) VALUES ('Fictif', 'TEST', 'FAIBLE');
SET @antecedent = LAST_INSERT_ID();
INSERT INTO affectation_antecedent (patient_id, antecedent_id) VALUES (@patient, @antecedent);
INSERT INTO medicament (nom, prix_unitaire) VALUES ('Medicament fictif', 10.00);
SET @medicament = LAST_INSERT_ID();
INSERT INTO agenda_mensuel (medecin_id, annee, mois) VALUES (@medecin, 2026, 11);
SET @agenda = LAST_INSERT_ID();
INSERT INTO jour_indisponible (agenda_id, date_jour) VALUES (@agenda, '2026-11-29');
INSERT INTO rdv (dossier_id, medecin_id, debut, fin, statut)
VALUES (@dossier, @medecin, '2026-11-16 09:00:00', '2026-11-16 09:30:00', 'TEST');
SET @rdv = LAST_INSERT_ID();
INSERT INTO consultation (dossier_id, medecin_id, rdv_id, statut) VALUES (@dossier, @medecin, @rdv, 'TERMINEE');
SET @consultation = LAST_INSERT_ID();
INSERT INTO intervention (consultation_id, acte_id, libelle_acte_snapshot, quantite, prix_unitaire)
VALUES (@consultation, @acte, 'Acte fictif', 1, 850.00);
INSERT INTO ordonnance (dossier_id, consultation_id, date_ordonnance) VALUES (@dossier, @consultation, '2026-11-16');
SET @ordonnance = LAST_INSERT_ID();
INSERT INTO prescription (ordonnance_id, medicament_id, quantite, frequence, duree_jours)
VALUES (@ordonnance, @medicament, 1, 'Fixture', 1);
INSERT INTO certificat (dossier_id, consultation_id, date_debut, date_fin, duree_jours)
VALUES (@dossier, @consultation, '2026-11-16', '2026-11-17', 2);
INSERT INTO document_medical (dossier_id, auteur_id, type, titre, statut)
VALUES (@dossier, @medecin, 'TEST', 'Fixture', 'TEST');
INSERT INTO facture (consultation_id, id_sf, numero, total, statut) VALUES (@consultation, @sf, 'TEST-001', 850.00, 'VALIDEE');
SET @facture = LAST_INSERT_ID();
UPDATE situation_financiere SET total_actes = 850.00, credit = 850.00 WHERE id = @sf;
INSERT INTO paiement (facture_id, auteur_id, reference, montant, mode)
VALUES (@facture, @auteur, '00000000-0000-0000-0000-000000000001', 300.00, 'ESPECES');
SET @paiement = LAST_INSERT_ID();
INSERT INTO revenu (cabinet_id, auteur_id, paiement_id, titre, montant) VALUES (@cabinet, @auteur, @paiement, 'Fixture', 300.00);
INSERT INTO charge (cabinet_id, auteur_id, titre, montant) VALUES (@cabinet, @medecin, 'Fixture', 200.00);
INSERT INTO notification (utilisateur_id, message, type) VALUES (@auteur, 'Fixture', 'TEST');
UPDATE situation_financiere SET total_paye = 300.00, credit = 550.00 WHERE id = @sf;
CALL test_assert((SELECT total_paye = 300.00 AND reste = 550.00 AND statut_reglement = 'PARTIELLEMENT_PAYEE' FROM v_facture_reglement WHERE id = @facture), 'Incorrect partial payment view');

-- Unicités : dossier, SF, affectation, rôle, agenda, jour, RDV, facture et référence.
CALL test_expect_error('INSERT INTO dossier_medical (patient_id) VALUES (@patient)', 1062);
CALL test_expect_error('INSERT INTO situation_financiere (id_dossier) VALUES (@dossier)', 1062);
CALL test_expect_error('INSERT INTO affectation_antecedent (patient_id, antecedent_id) VALUES (@patient, @antecedent)', 1062);
CALL test_expect_error('INSERT INTO utilisateurs (nom, prenom, login, mot_de_passe, id_role) VALUES (''Fixture'', ''Fixture'', ''invalid-role'', ''fixture'', 9999999)', 1452);
CALL test_expect_error('INSERT INTO agenda_mensuel (medecin_id, annee, mois) VALUES (@medecin, 2026, 11)', 1062);
CALL test_expect_error('INSERT INTO jour_indisponible (agenda_id, date_jour) VALUES (@agenda, ''2026-11-29'')', 1062);
CALL test_expect_error('INSERT INTO consultation (dossier_id, medecin_id, rdv_id) VALUES (@dossier, @medecin, @rdv)', 1062);
CALL test_expect_error('INSERT INTO facture (consultation_id, id_sf, numero, total) VALUES (@consultation, @sf, ''TEST-OTHER'', 850)', 1062);
INSERT INTO consultation (dossier_id, medecin_id) VALUES (@dossier, @medecin);
SET @consultation2 = LAST_INSERT_ID();
CALL test_expect_error('INSERT INTO facture (consultation_id, id_sf, numero, total) VALUES (@consultation2, @sf, ''TEST-001'', 850)', 1062);
CALL test_expect_error('INSERT INTO paiement (facture_id, auteur_id, reference, montant, mode) VALUES (@facture, @auteur, ''00000000-0000-0000-0000-000000000001'', 10, ''CARTE'')', 1062);
CALL test_expect_error('INSERT INTO revenu (cabinet_id, auteur_id, paiement_id, titre, montant) VALUES (@cabinet, @auteur, @paiement, ''Duplicate'', 300)', 1062);

-- Références obligatoires et protection de l'historique.
CALL test_expect_error('INSERT INTO facture (consultation_id, id_sf, numero, total) VALUES (9999999, @sf, ''TEST-NO-CONS'', 10)', 1452);
CALL test_expect_error('INSERT INTO facture (consultation_id, id_sf, numero, total) VALUES (@consultation2, 9999999, ''TEST-NO-SF'', 10)', 1452);
CALL test_expect_error('INSERT INTO facture (consultation_id, id_sf, numero, total) VALUES (@consultation2, NULL, ''TEST-NULL-SF'', 10)', 1048);
CALL test_expect_error('INSERT INTO paiement (facture_id, auteur_id, reference, montant, mode) VALUES (9999999, @auteur, UUID(), 10, ''CARTE'')', 1452);
CALL test_expect_error('INSERT INTO paiement (facture_id, auteur_id, reference, montant, mode) VALUES (@facture, 9999999, UUID(), 10, ''CARTE'')', 1452);
CALL test_expect_error('DELETE FROM facture WHERE id = @facture', 1451);
CALL test_expect_error('DELETE FROM patient WHERE id = @patient', 1451);

-- CHECK de montants, quantités, périodes et agrégats locaux.
CALL test_expect_error('INSERT INTO paiement (facture_id, auteur_id, reference, montant, mode) VALUES (@facture, @auteur, UUID(), 0, ''CARTE'')', 3819);
CALL test_expect_error('INSERT INTO paiement (facture_id, auteur_id, reference, montant, mode) VALUES (@facture, @auteur, UUID(), -1, ''CARTE'')', 3819);
CALL test_expect_error('INSERT INTO facture (consultation_id, id_sf, numero, total) VALUES (@consultation2, @sf, ''TEST-NEGATIVE'', -1)', 3819);
CALL test_expect_error('UPDATE situation_financiere SET credit = 1 WHERE id = @sf', 3819);
CALL test_expect_error('INSERT INTO agenda_mensuel (medecin_id, annee, mois) VALUES (@medecin, 2026, 13)', 3819);
CALL test_expect_error('INSERT INTO rdv (dossier_id, medecin_id, debut, fin, statut) VALUES (@dossier, @medecin, ''2026-11-16 10:00:00'', ''2026-11-16 09:00:00'', ''TEST'')', 3819);
CALL test_expect_error('INSERT INTO intervention (consultation_id, acte_id, libelle_acte_snapshot, quantite, prix_unitaire) VALUES (@consultation, @acte, ''Fixture'', 0, 10)', 3819);
CALL test_expect_error('INSERT INTO prescription (ordonnance_id, medicament_id, quantite, frequence, duree_jours) VALUES (@ordonnance, @medicament, 1, ''Fixture'', 0)', 3819);
CALL test_expect_error('INSERT INTO certificat (dossier_id, consultation_id, date_debut, date_fin, duree_jours) VALUES (@dossier, @consultation, ''2026-11-17'', ''2026-11-16'', 1)', 3819);
CALL test_expect_error('INSERT INTO utilisateurs (nom, prenom, login, mot_de_passe, id_role) VALUES (''Fixture'', ''Fixture'', ''missing-role'', ''fixture'', NULL)', 1048);

-- Rôle unique, absence de type redondant, consultations sans RDV et règlement total.
CALL test_assert((SELECT COUNT(*) = 0 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN ('utilisateur_role', 'journal_audit')), 'Unsupported tables remain');
CALL test_assert((SELECT COUNT(*) = 0 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'utilisateurs' AND column_name = 'type_utilisateur'), 'Redundant user type remains');
INSERT INTO consultation (dossier_id, medecin_id) VALUES (@dossier, @medecin);
CALL test_assert((SELECT COUNT(*) = 2 FROM consultation WHERE dossier_id = @dossier AND rdv_id IS NULL), 'Nullable unique appointment failed');
INSERT INTO paiement (facture_id, auteur_id, reference, montant, mode)
VALUES (@facture, @auteur, '00000000-0000-0000-0000-000000000002', 550.00, 'VIREMENT');
UPDATE situation_financiere SET total_paye = 850.00, credit = 0.00 WHERE id = @sf;
CALL test_assert((SELECT total_paye = 850.00 AND reste = 0.00 AND statut_reglement = 'PAYEE' FROM v_facture_reglement WHERE id = @facture), 'Incorrect full payment view');
ROLLBACK;

CALL test_assert((SELECT COUNT(*) = 0 FROM patient), 'Fixtures were not rolled back');
SELECT @checks AS controles_reussis;
DROP PROCEDURE test_expect_error;
DROP PROCEDURE test_assert;
