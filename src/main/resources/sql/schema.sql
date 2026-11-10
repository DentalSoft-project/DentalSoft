-- Création de la base de données
CREATE DATABASE IF NOT EXISTS dentalsoft_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dentalsoft_db;

-- Table Patient
CREATE TABLE patient (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         nom VARCHAR(100) NOT NULL,
                         date_naissance DATE NOT NULL,
                         sexe VARCHAR(10),
                         adresse VARCHAR(255),
                         telephone VARCHAR(20),
                         assurance VARCHAR(100),
                         date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table Dossier Médical (Relation 1 Patient <-> 1 Dossier)
CREATE TABLE dossier_medical (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 patient_id BIGINT NOT NULL UNIQUE,
                                 date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
                                 notes TEXT,
                                 CONSTRAINT fk_dossier_patient FOREIGN KEY (patient_id)
                                     REFERENCES patient(id) ON DELETE CASCADE
);