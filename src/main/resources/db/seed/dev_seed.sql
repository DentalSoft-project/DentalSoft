-- ============================================================================
-- dev_seed.sql — données fictives de développement uniquement
-- À exécuter APRÈS install.sql, une seule fois sur une installation neuve.
-- Les trois hashes PBKDF2 préexistants sont conservés.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

INSERT INTO cabinet_medical (nom) VALUES ('YOU SMILE');

INSERT INTO roles (code, libelle) VALUES
    ('ADMIN', 'Administrateur'),
    ('MEDECIN', 'Médecin'),
    ('SECRETAIRE', 'Secrétaire');

INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_UTILISATEURS' FROM roles WHERE code = 'ADMIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_ROLES' FROM roles WHERE code = 'ADMIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_PATIENTS' FROM roles WHERE code = 'MEDECIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_CONSULTATIONS' FROM roles WHERE code = 'MEDECIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_AGENDA' FROM roles WHERE code = 'MEDECIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_CAISSE' FROM roles WHERE code = 'MEDECIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_PATIENTS' FROM roles WHERE code = 'SECRETAIRE';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_AGENDA' FROM roles WHERE code = 'SECRETAIRE';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_CAISSE' FROM roles WHERE code = 'SECRETAIRE';

-- Comptes fictifs existants ; un rôle obligatoire par utilisateur.
INSERT INTO utilisateurs
    (nom, prenom, login, mot_de_passe, actif, id_role, cabinet_id, specialite, num_cnss, commission)
SELECT 'Admin', 'DentalSoft', 'admin', 'pbkdf2$120000$uFMkWAiuouY80VW378rMvQ==$X0DBhH+NycaHi8NOscSm6FDl9qU28F1vumHuZTjDjcY=', TRUE, r.id_role, c.id, NULL, NULL, NULL
FROM cabinet_medical c CROSS JOIN roles r
WHERE c.nom = 'YOU SMILE' AND r.code = 'ADMIN';

INSERT INTO utilisateurs
    (nom, prenom, login, mot_de_passe, actif, id_role, cabinet_id, specialite, num_cnss, commission)
SELECT 'Alami', 'Sara', 'dr.alami', 'pbkdf2$120000$KS/a9QjaIO3D8Oradzsz0w==$8zo9oxpsIPuARhG+4BpgcqvH3A6kK6D/M26xySU/0Sk=', TRUE, r.id_role, c.id, 'Orthodontie', NULL, NULL
FROM cabinet_medical c CROSS JOIN roles r
WHERE c.nom = 'YOU SMILE' AND r.code = 'MEDECIN';

INSERT INTO utilisateurs
    (nom, prenom, login, mot_de_passe, actif, id_role, cabinet_id, specialite, num_cnss, commission)
SELECT 'Bennani', 'Imane', 'secretaire', 'pbkdf2$120000$kF0f4xfy0LX8AHblIs/pag==$+L72rTIcNc81HfoDfUG8d+vkCx8i1H3i8rPk6As/y7Q=', TRUE, r.id_role, c.id, NULL, '1234567', '5.00'
FROM cabinet_medical c CROSS JOIN roles r
WHERE c.nom = 'YOU SMILE' AND r.code = 'SECRETAIRE';
