-- ============================================================================
-- dev_seed.sql
-- Données de référence et comptes de test (développement uniquement)
-- À exécuter APRÈS 01, 02 et 03.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Rôles
-- ----------------------------------------------------------------------------
INSERT INTO roles (code, libelle) VALUES
    ('ADMIN',      'Administrateur'),
    ('MEDECIN',    'Médecin'),
    ('SECRETAIRE', 'Secrétaire');

-- ----------------------------------------------------------------------------
-- Permissions par rôle (à ajuster selon la conception du groupe)
-- ----------------------------------------------------------------------------
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_UTILISATEURS'  FROM roles WHERE code = 'ADMIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_ROLES'         FROM roles WHERE code = 'ADMIN';

INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_PATIENTS'      FROM roles WHERE code = 'MEDECIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_CONSULTATIONS' FROM roles WHERE code = 'MEDECIN';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_AGENDA'        FROM roles WHERE code = 'MEDECIN';

INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_PATIENTS'      FROM roles WHERE code = 'SECRETAIRE';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_AGENDA'        FROM roles WHERE code = 'SECRETAIRE';
INSERT INTO role_permissions (id_role, permission)
SELECT id_role, 'GERER_CAISSE'        FROM roles WHERE code = 'SECRETAIRE';

-- ----------------------------------------------------------------------------
-- Comptes fictifs (mots de passe de test : admin123 / medecin123 / secretaire123)
-- ----------------------------------------------------------------------------
INSERT INTO utilisateurs
    (nom, prenom, login, mot_de_passe, actif, id_role, specialite, num_cnss, commission)
SELECT 'Admin', 'DentalSoft', 'admin',
       'pbkdf2$120000$uFMkWAiuouY80VW378rMvQ==$X0DBhH+NycaHi8NOscSm6FDl9qU28F1vumHuZTjDjcY=',
       TRUE, r.id_role, NULL, NULL, NULL
FROM roles r WHERE r.code = 'ADMIN';

INSERT INTO utilisateurs
    (nom, prenom, login, mot_de_passe, actif, id_role, specialite, num_cnss, commission)
SELECT 'Alami', 'Sara', 'dr.alami',
       'pbkdf2$120000$KS/a9QjaIO3D8Oradzsz0w==$8zo9oxpsIPuARhG+4BpgcqvH3A6kK6D/M26xySU/0Sk=',
       TRUE, r.id_role, 'Orthodontie', NULL, NULL
FROM roles r WHERE r.code = 'MEDECIN';

INSERT INTO utilisateurs
    (nom, prenom, login, mot_de_passe, actif, id_role, specialite, num_cnss, commission)
SELECT 'Bennani', 'Imane', 'secretaire',
       'pbkdf2$120000$kF0f4xfy0LX8AHblIs/pag==$+L72rTIcNc81HfoDfUG8d+vkCx8i1H3i8rPk6As/y7Q=',
       TRUE, r.id_role, NULL, '1234567', 5.00
FROM roles r WHERE r.code = 'SECRETAIRE';
