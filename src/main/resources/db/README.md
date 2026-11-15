# Schéma MySQL DentalSoft

Schéma d'installation neuve : **26 tables et une vue**, avec un rôle obligatoire par utilisateur. MySQL **8.0.16+**, InnoDB, utf8mb4_unicode_ci. Le schéma doit être relu par les responsables des modules avant intégration ; les écarts restant à arbitrer sont listés ci-dessous.

## Installation

Depuis la **racine du dépôt**, avec le client MySQL :

```powershell
mysql --default-character-set=utf8mb4 -u <utilisateur> -p -e "SOURCE src/main/resources/db/install.sql;"
```

Le point d'entrée exécute les fichiers ci-dessous dans l'ordre. `SOURCE` est une commande du client MySQL ; ce fichier ne peut pas être envoyé tel quel à JDBC. Le mode `-e` s'arrête sur erreur ; ne pas utiliser `--force`.

La base cible est `dentalsoftbd`. L'installation ne supprime aucune table et n'insère aucun compte. Elle échoue si les tables existent déjà : ce n'est ni une migration ni un script de réinitialisation. Aucun script de migration d'une base existante n'est fourni dans ce lot.

Les données fictives de développement sont optionnelles, une seule fois après l'installation :

```powershell
mysql --default-character-set=utf8mb4 -u <utilisateur> -p -e "SOURCE src/main/resources/db/seed/dev_seed.sql;"
```

Les trois comptes fictifs et hashes PBKDF2 préexistants sont conservés ; leur rôle est référencé par `utilisateurs.id_role`, comme dans le code Java existant. Le seed crée le cabinet fictif YOU SMILE. Aucune configuration de connexion personnelle n'est nécessaire dans le dépôt.

Le catalogue fictif est indépendant et optionnel, une seule fois après l'installation :

```powershell
mysql --default-character-set=utf8mb4 -u <utilisateur> -p -e "SOURCE src/main/resources/db/seed/dev_catalogues_seed.sql;"
```

Il contient **3 actes, 3 antécédents inventés et 3 médicaments entièrement fictifs**, tous préfixés DEMO. Les prix servent uniquement à la démonstration ; aucune posologie ni valeur tarifaire de référence. Les types/formes de médicaments non définis restent NULL. Les identifiants sont auto-générés et les neuf insertions forment une transaction. Ne pas relancer ces seeds sur une base déjà peuplée : les libellés de catalogue ne sont pas uniques.

Ordre de développement : `install.sql`, puis `seed/dev_seed.sql`, puis éventuellement `seed/dev_catalogues_seed.sql`. Aucun de ces seeds n'est exécuté automatiquement par l'installateur. Le catalogue ne crée ni patient, ni consultation, ni facture/paiement : le scénario de démonstration complet reste séparé.

## Ordre et correspondance UML / SQL

| Script | Tables / classes couvertes |
| --- | --- |
| `00_base_cabinet.sql` | création de la base dentalsoftbd et cabinet_medical |
| `01_referentiels.sql` | assurance, acte, antecedent, medicament |
| `02_utilisateurs_roles.sql` | roles, role_permissions, utilisateurs ; héritage Utilisateur/Staff/Médecin/Secrétaire dans une table, profil déterminé par le rôle unique |
| `03_patients_dossiers.sql` | patient, dossier_medical, affectation_antecedent |
| `04_agenda_rdv.sql` | agenda_mensuel, jour_indisponible, rdv |
| `05_consultations_interventions.sql` | consultation, intervention (InterventionMédecin) |
| `06_documents_medicaux.sql` | ordonnance, prescription, certificat, document_medical |
| `07_factures.sql` | situation_financiere, facture |
| `08_paiements.sql` | paiement |
| `09_caisse.sql` | charge, revenu |
| `10_notifications.sql` | notification |
| `11_vues_financieres.sql` | v_facture_reglement : total payé, reste et statut de règlement calculés |

Les définitions sont uniquement dans les douze fichiers `schema/*.sql` ; `install.sql` ne les duplique pas. Le script 00 crée la base et le cabinet avant les catalogues et utilisateurs ; l'ordre garantit aussi la création des consultations avant les factures.

## Provenance du modèle

Le diagramme blanc fournit la structure de référence. Le diagramme sombre fournit uniquement ses compléments représentés ; il ne constitue pas une seconde autorité concurrente. Les noms SQL, clés étrangères, index et tables d'association traduisent le modèle sans imposer une table par classe UML.

| Structures SQL | Origine / traduction |
| --- | --- |
| cabinet_medical, utilisateurs, roles et attributs Staff | Diagramme blanc ; héritage Utilisateur/Staff/Médecin/Secrétaire dans utilisateurs, profil Admin issu du sombre |
| patient, dossier_medical, antecedent, affectation_antecedent | Diagramme blanc ; table d'association pour Patient/Antécédent |
| acte, intervention, medicament, ordonnance, prescription, certificat | Diagramme blanc |
| situation_financiere, facture, charge, revenu | Diagramme blanc ; SF persistée |
| paiement, document_medical, notification | Compléments présents dans le diagramme sombre ; notification.objet correspond à l'attribut dessiné |
| assurance, role_permissions et champs applicatifs préexistants | Structure existante conservée |
| agenda_mensuel, jour_indisponible | Décomposition de l'attribut agendaMensuel ; détail à valider |
| v_facture_reglement | Lecture des attributs payé/reste de Facture, sans nouvelle entité métier |
| date_creation, date_derniere_modification, cree_par, modifie_par | Attributs hérités d'Entité et conventions existantes |

## Décisions de structure

- PK `id` pour les nouvelles tables ; `roles.id_role` et `utilisateurs.id_utilisateur` conservés. Les noms existants de patient/dossier/assurance restent compatibles avec leurs repositories.
- Un seul rôle obligatoire par utilisateur, via `id_role BIGINT NOT NULL` et sa FK. Le rôle sélectionne le profil Java ; l'héritage des permissions médecin/secrétaire n'exige pas deux rôles ni un second discriminateur de profil.
- Le service vérifie l'appartenance au cabinet selon le rôle. Un CHECK SQL ne peut pas lire roles.code et aucun identifiant numérique de rôle n'est supposé. La FK cabinet utilise ON UPDATE CASCADE comme les autres liens.
- Dossier unique par patient et SF unique par dossier. Leur existence immédiate, lorsqu'elle est requise, dépend de la transaction de création côté service : une FK seule ne crée pas l'enfant.
- `consultation.rdv_id` est facultatif et unique ; `facture.consultation_id` est obligatoire et unique. Aucune association directe facture/intervention.
- États de consultation EN_COURS/TERMINEE/ANNULEE ; TERMINEE représente la validation permettant la facturation, suivant la décision approuvée pour ce schéma. Cela ne crée pas l'opération de clôture dans le service existant.
- Statuts documentaires facture BROUILLON/VALIDEE/ANNULEE. Le statut de règlement et les montants payé/reste sont exposés par `v_facture_reglement`, sans les stocker une deuxième fois dans facture. Le service ne doit pas accepter de paiement sur une facture inéligible.
- SF persistée : `total_actes` désigne le total des factures VALIDEE, `total_paye` les paiements associés et `credit` le reste dû. Le CHECK impose la cohérence arithmétique de la ligne, pas sa correspondance avec les autres tables.
- Les codes métier non définis par les diagrammes sont stockés provisoirement en VARCHAR(30), sans liste ou défaut inventés. Leurs valeurs applicatives restent à fixer avec les responsables concernés ; les valeurs TEST du script de vérification ne sont pas des codes métier.
- Les colonnes d'audit communes traduisent Entité dans le diagramme blanc. `cree_par` et `modifie_par` restent des références logiques ; les auteurs métier (paiement, document, charge, revenu) ont des FK réelles, dont la présence reste à confirmer lors de la revue stricte des diagrammes.
- Les montants sont DECIMAL(12,2). Les prix des interventions sont des prix appliqués conservés avec les libellés ; aucun champ de remise séparé ni convention montant/pourcentage supplémentaire n'a été inventé.

## Adaptations Java à coordonner

**Le mapping du rôle unique est compatible avec le repository d'utilisateurs existant. Les incompatibilités cliniques suivantes restent à traiter. La logique Java n'a pas été modifiée ; seul le message d'installation de JdbcConnectionFactory mentionne désormais install.sql.**

| Élément existant | Adaptation requise |
| --- | --- |
| `UtilisateurRepoImpl`, `Utilisateur` | Conserver le rôle unique actuel ; id_role, specialite, num_cnss et commission restent disponibles pour le mapper existant. |
| Session et autorisations | Conserver les contrôles côté service et l'héritage des permissions Médecin/Secrétaire. |
| `RowMappers.toConsultation` | Utiliser id, dossier_id, medecin_id et rdv_id, ou définir les alias correspondants dans la requête DAO. |
| `RowMappers.toIntervention` | Utiliser id, consultation_id et acte_id, ou des alias explicites. |
| `RowMappers.toDossierMedical` | Utiliser id/patient_id et définir la lecture clinique autorisée : pas de colonne observations_cliniques sur dossier_medical. Des alias seuls ne résolvent pas la provenance des observations. |
| `ConsultationServiceImpl` | Remplacer le stockage en mémoire par les DAO prévus et implémenter la validation approuvée. Une nouvelle instance ne partage pas les données de l'instance du panneau. |
| Services M4/M5/M6 | Contrôler droits, rôles, dossier/médecin communs entre RDV/consultation/documents/SF, périodes d'agenda et invariants entre tables. |

Les CHECK n'empêchent pas les chevauchements de RDV, le surpaiement concurrent ou une modification métier interdite. La transaction du service doit verrouiller les données utiles, mettre à jour paiement/revenu/SF de façon cohérente et gérer rollback/retry. Une référence de paiement ou un paiement lié à un revenu ne doit pas produire deux encaissements.

## Points à valider avant intégration

La couverture des classes est vérifiée, mais la conformité littérale de tous les champs et relations aux dessins n'est pas déclarée acquise. La revue doit trancher le maintien des éléments suivants, issus du code existant ou de corrections métier :

- fin de RDV, arrivee_at et détail de l'agenda mensuel/jours indisponibles ;
- quantité et libellé/prix figés des interventions, déjà présents dans le Java ;
- médecin responsable, liens document/consultation, paiement/facture et revenu/paiement, auteurs métier ;
- numéro de facture, motif d'annulation et référence idempotente du paiement ;
- corrections de cardinalité : au plus une facture par consultation et RDV facultatif unique ;
- valeurs des codes métier non définis par les diagrammes et convention de comptage des jours d'un certificat.

La durée du certificat sera calculée par M5 après fixation de cette convention. Les statistiques sont calculées à la lecture ; aucune table de KPI matérialisés. Le schéma n'ajoute pas de journal d'audit métier, sessions, sauvegardes, préférences ou liste d'attente distincte. Les champs d'audit hérités restent présents.

## Vérification reproductible

Sur une **instance MySQL jetable uniquement**, après l'installation et le seed :

```powershell
mysql --default-character-set=utf8mb4 -u <utilisateur> -p -e "SOURCE src/test/resources/db/schema_checks.sql;"
```

Le script exige une base fraîche avec le seed de comptes, avec ou sans le catalogue fictif : il crée ses propres fixtures dans une transaction, contrôle les erreurs MySQL attendues puis annule ces fixtures. Les catalogues préchargés sont conservés. Il crée et supprime deux procédures de test. Si un contrôle échoue, abandonner l'instance de test plutôt que continuer sur une base partagée.

Validation locale du 15/11/2026 : MySQL Community Server **8.4.13**, instance temporaire du build, configuration indépendante de celle de l'application, écoute limitée à 127.0.0.1. Installation du schéma et seed réussis : **26 tables, 35 FK, 1 vue, 3 utilisateurs avec rôle obligatoire**. Le script a réussi **36 contrôles** : rôle invalide/nul refusé, absence des structures exclues, parcours clinique/finance fictif, règlement partiel puis total, contraintes FK/UNIQUE/NOT NULL/CHECK et rollback des fixtures.

L'installation réorganisée en douze scripts et le seed de catalogues ont ensuite été exécutés dans l'instance jetable : **36 contrôles réussis avec les catalogues préchargés**. Après rollback des fixtures, les trois actes, trois antécédents et trois médicaments sont conservés, sans patient, consultation, facture ou paiement. Prix fictifs et valeurs NULL des codes de médicaments vérifiés ; serveur de test arrêté proprement. Aucun test Maven ni lancement applicatif effectué pour ces données SQL.

Cette preuve concerne le SQL sous MySQL 8.4.13. Elle ne prouve ni exécution sur toutes les versions 8.0, ni fonctionnement Swing/JDBC, ni concurrence, droits ou transactions des services. Aucun test Maven requis pour ces seuls fichiers SQL/documentaires ; l'application Java n'a pas été exécutée.
