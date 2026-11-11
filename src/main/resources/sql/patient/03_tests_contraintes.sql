USE dentalsoftbd;

-- État initial
SELECT COUNT(*) AS patients FROM patient;              -- 10
SELECT COUNT(*) AS dossiers FROM dossier_medical;      -- 10

-- ----------------------------------------------------------------------------
-- Test 1 — CIN en double → ERROR 1062
-- ----------------------------------------------------------------------------
INSERT INTO patient (nom, prenom, date_naissance, sexe, cin, telephone)
VALUES ('Doublon', 'Test', '1990-01-01', 'M', 'AB123456', '+212600000002');

-- ----------------------------------------------------------------------------
-- Test 2 — Deuxième dossier pour le même patient → ERROR 1062
-- ----------------------------------------------------------------------------
INSERT INTO dossier_medical (patient_id) VALUES (1);

-- ----------------------------------------------------------------------------
-- Test 3 — Assurance inexistante → ERROR 1452
-- ----------------------------------------------------------------------------
INSERT INTO patient (nom, prenom, date_naissance, sexe, telephone, id_assurance)
VALUES ('FK', 'Test', '1990-01-01', 'M', '+212600000003', 99999);

-- ----------------------------------------------------------------------------
-- Test 4 — Téléphone NULL → ERROR 1048
-- ----------------------------------------------------------------------------
INSERT INTO patient (nom, prenom, date_naissance, sexe, telephone)
VALUES ('Null', 'Test', '1990-01-01', 'M', NULL);

-- ----------------------------------------------------------------------------
-- Test 5 — Transaction annulée manuellement
-- ----------------------------------------------------------------------------
START TRANSACTION;
INSERT INTO patient (nom, prenom, date_naissance, sexe, cin, telephone, cree_par)
VALUES ('Test', 'Transaction', '1990-01-01', 'M', 'ZZ000002', '+212600000002', 1);

SELECT COUNT(*) AS visible_dans_transaction
FROM patient WHERE cin = 'ZZ000002';                   -- 1

ROLLBACK;

SELECT COUNT(*) AS apres_rollback
FROM patient WHERE cin = 'ZZ000002';                   -- 0

-- ----------------------------------------------------------------------------
-- Test 6 — Atomicité patient + dossier : le dossier échoue → ROLLBACK
-- ----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS test_rollback_patient;

DELIMITER $$
CREATE PROCEDURE test_rollback_patient()
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
BEGIN
ROLLBACK;
SELECT 'ROLLBACK effectué : aucune donnée conservée' AS resultat;
END;

START TRANSACTION;

INSERT INTO patient (nom, prenom, date_naissance, sexe, cin, telephone, cree_par)
VALUES ('Test', 'Rollback', '1990-01-01', 'M', 'ZZ000001', '+212600000001', 1);

-- Échec volontaire : patient_id inexistant → FK 1452
INSERT INTO dossier_medical (patient_id, cree_par) VALUES (999999, 1);

COMMIT;
END$$
DELIMITER ;

CALL test_rollback_patient();

SELECT COUNT(*) AS patients_test_restants
FROM patient WHERE cin = 'ZZ000001';                   -- 0

DROP PROCEDURE test_rollback_patient;

-- ----------------------------------------------------------------------------
-- Test 7 — Taux d'assurance > 100 → ERROR 3819 (CHECK)
-- ----------------------------------------------------------------------------
INSERT INTO assurance (libelle, taux_remboursement)
VALUES ('Test CHECK', 150.00);

-- ----------------------------------------------------------------------------
-- Vérification finale : aucun test n'a modifié les données
-- ----------------------------------------------------------------------------
SELECT COUNT(*) AS patients_apres_tests FROM patient;             -- 10
SELECT COUNT(*) AS dossiers_apres_tests FROM dossier_medical;     -- 10

SELECT patient_id, COUNT(*) AS nb
FROM dossier_medical
GROUP BY patient_id
HAVING COUNT(*) > 1;                                              -- 0 ligne