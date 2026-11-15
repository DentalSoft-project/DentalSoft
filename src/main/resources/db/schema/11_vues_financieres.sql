-- ============================================================================
-- 11_vues_financieres.sql
-- M6 : attributs de règlement calculés, sans duplication dans facture
-- Cible : MySQL 8.0.16+ ; installation neuve uniquement.
-- Dépend de : 07_factures.sql, 08_paiements.sql
-- Audit cree_par / modifie_par : références logiques vers utilisateurs.
-- Aucun nettoyage : les tables existantes ne sont pas supprimées.
-- ============================================================================

USE dentalsoftbd;

SET NAMES utf8mb4;

-- Les statuts ci-dessous décrivent le règlement, pas le cycle documentaire.
-- Un solde négatif révèle une violation métier : aucun CHECK ne borne SUM(paiement).
CREATE VIEW v_facture_reglement AS
SELECT f.id, f.consultation_id, f.id_sf, f.numero, f.date_facture,
       f.total, f.statut, f.motif_annulation,
       COALESCE(p.total_paye, 0.00) AS total_paye,
       f.total - COALESCE(p.total_paye, 0.00) AS reste,
       CASE
           WHEN COALESCE(p.total_paye, 0.00) = f.total THEN 'PAYEE'
           WHEN COALESCE(p.total_paye, 0.00) = 0.00 THEN 'NON_PAYEE'
           ELSE 'PARTIELLEMENT_PAYEE'
       END AS statut_reglement
FROM facture f
LEFT JOIN (
    SELECT facture_id, SUM(montant) AS total_paye
    FROM paiement
    GROUP BY facture_id
) p ON p.facture_id = f.id;
