SET NAMES utf8mb4;
USE dentalsoftbd;

-- ----------------------------------------------------------------------------
-- 1) Assurances de référence
-- ----------------------------------------------------------------------------
INSERT INTO assurance (libelle, taux_remboursement)
VALUES
    ('CNSS / AMO', 70.00),
    ('CNOPS', 70.00),
    ('Assurance privée', 80.00)
    ON DUPLICATE KEY UPDATE libelle = libelle;

-- ----------------------------------------------------------------------------
-- 2) Table temporaire de travail (pas une table métier)
-- ----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS seed_patients;

CREATE TEMPORARY TABLE seed_patients (
    cin            VARCHAR(20)    NOT NULL,
    nom            VARCHAR(80)    NOT NULL,
    prenom         VARCHAR(80)    NOT NULL,
    date_naissance DATE           NOT NULL,
    sexe           ENUM('M','F')  NOT NULL,
    telephone      VARCHAR(20)    NOT NULL,
    email          VARCHAR(120)   NULL,
    adresse        VARCHAR(255)   NULL,
    assurance      VARCHAR(80)    NULL
) ENGINE = InnoDB;

INSERT INTO seed_patients
(cin, nom, prenom, date_naissance, sexe, telephone, email, adresse, assurance)
VALUES
    ('AB123456', 'El Amrani', 'Youssef', '1982-03-14', 'M', '+212661248018', 'y.elamrani@example.ma',   '12 rue Oued Fès, Rabat',              'CNSS / AMO'),
    ('BK932401', 'Benjelloun','Salma',   '1992-07-05', 'F', '+212662110943', 's.benjelloun@example.ma', '45 avenue Mohammed V, Rabat',         'CNOPS'),
    ('C123458',  'Tazi',      'Karim',   '1975-11-23', 'M', '+212663881290', 'k.tazi@example.ma',       '8 boulevard Zerktouni, Casablanca',   'Assurance privée'),
    ('BE654327', 'Berrada',   'Nadia',   '1988-02-17', 'F', '+212661908123', 'n.berrada@example.ma',    '23 rue Ibn Sina, Salé',               'CNSS / AMO'),
    ('AA847192', 'Alaoui',    'Omar',    '1995-09-02', 'M', '+212664341890', 'o.alaoui@example.ma',     '6 rue Al Majd, Témara',               'CNOPS'),
    ('IC835081', 'Chraibi',   'Imane',   '1999-12-11', 'F', '+212660765432', 'i.chraibi@example.ma',    '31 avenue Hassan II, Rabat',          'CNSS / AMO'),
    ('MF455104', 'Fassi',     'Mehdi',   '1983-06-28', 'M', '+212661554321', 'm.fassi@example.ma',      '17 rue Ibn Battouta, Casablanca',     'Assurance privée'),
    ('LS742817', 'Sekkat',    'Leila',   '1970-04-19', 'F', '+212662901844', 'l.sekkat@example.ma',     '9 rue Oum Rabia, Rabat',              'CNSS / AMO'),
    ('RB949421', 'Benali',    'Rachid',  '1965-08-30', 'M', '+212661189023', 'r.benali@example.ma',     '28 avenue des FAR, Salé',             'CNOPS'),
    ('HI730298', 'Idrissi',   'Hajar',   '2001-01-25', 'F', '+212663456789', 'h.idrissi@example.ma',    '14 rue Al Andalous, Rabat',           NULL);

-- ----------------------------------------------------------------------------
-- 3) Patients absents (clé naturelle = CIN), avec leur assurance
-- ----------------------------------------------------------------------------
INSERT INTO patient
(nom, prenom, date_naissance, sexe, cin, telephone, email, adresse,
 id_assurance, archive, cree_par)
SELECT s.nom, s.prenom, s.date_naissance, s.sexe, s.cin, s.telephone,
       s.email, s.adresse, a.id, FALSE, 1
FROM seed_patients s
         LEFT JOIN assurance a ON a.libelle = s.assurance
         LEFT JOIN patient p   ON p.cin = s.cin
WHERE p.id IS NULL;

-- ----------------------------------------------------------------------------
-- 4) Un dossier médical par patient (RG-01), sans doublon
-- ----------------------------------------------------------------------------
INSERT INTO dossier_medical (patient_id, cree_par)
SELECT p.id, 1
FROM patient p
         JOIN seed_patients s      ON s.cin = p.cin
         LEFT JOIN dossier_medical d ON d.patient_id = p.id
WHERE d.id IS NULL;

DROP TEMPORARY TABLE seed_patients;