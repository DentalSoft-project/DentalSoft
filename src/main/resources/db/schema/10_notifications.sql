-- ============================================================================
-- 10_notifications.sql
-- Notifications : complément présent dans le diagramme sombre
-- Cible : MySQL 8.0.16+ ; installation neuve uniquement.
-- Dépend de : 02_utilisateurs_roles.sql
-- Audit cree_par / modifie_par : références logiques vers utilisateurs.
-- Aucun nettoyage : les tables existantes ne sont pas supprimées.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- Notifications ; un destinataire utilisateur par notification
-- ----------------------------------------------------------------------------
CREATE TABLE notification (
    id                          BIGINT         NOT NULL AUTO_INCREMENT,
    utilisateur_id              BIGINT         NOT NULL,
    objet                       VARCHAR(120)   NULL,
    message                     TEXT           NOT NULL,
    type                        VARCHAR(30)    NOT NULL,
    priorite                    VARCHAR(30)    NULL,
    date_notification           DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_creation               DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_derniere_modification  DATETIME       NULL DEFAULT NULL,
    cree_par                    BIGINT         NULL,
    modifie_par                 BIGINT         NULL,
    PRIMARY KEY (id),
    KEY idx_notification_destinataire_date (utilisateur_id, date_notification),
    CONSTRAINT fk_notification_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs (id_utilisateur)
            ON UPDATE CASCADE
            ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
