USE dentalsoft_db;

-- Insertion de patients fictifs
INSERT INTO patient (nom, date_naissance, sexe, telephone, assurance) VALUES
                                                                          ('Benali Ahmed', '1985-05-12', 'Homme', '0612345678', 'CNSS'),
                                                                          ('Alaoui Sara', '1992-11-23', 'Femme', '0698765432', 'CNOPS'),
                                                                          ('Tazi Karim', '2001-03-08', 'Homme', '0655555555', 'Privée');

-- Création de leurs dossiers médicaux
INSERT INTO dossier_medical (patient_id, notes) VALUES
                                                    (1, 'Patient allergique à la pénicilline.'),
                                                    (2, 'Aucun antécédent particulier.'),
                                                    (3, 'Suivi orthodontique en cours.');