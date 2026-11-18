package ma.dentalsoft.ui.views;

import com.formdev.flatlaf.FlatClientProperties;
import ma.dentalsoft.domain.Patient;
import ma.dentalsoft.domain.Sexe;
import ma.dentalsoft.dto.AssuranceDTO;
import ma.dentalsoft.ui.components.CardPanel;
import ma.dentalsoft.ui.components.buttons.ButtonFactory;
import ma.dentalsoft.ui.components.buttons.ButtonStyles;
import ma.dentalsoft.ui.components.forms.FormFieldPanel;
import ma.dentalsoft.ui.components.tables.TableStyler;
import ma.dentalsoft.ui.controllers.PatientController;
import ma.dentalsoft.ui.models.PatientTableModel;
import ma.dentalsoft.ui.theme.UiColors;
import ma.dentalsoft.ui.theme.UiTypography;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Écran « Patients » (E-04, T05) : recherche paginée, tableau et formulaire de création.
 * La création enregistre le patient ET son dossier médical (RG-01) via PatientService.
 * Cette vue ne contient ni SQL ni règle métier : elle valide les formats simples,
 * délègue au contrôleur et affiche les messages près des champs concernés.
 */
public class PatientPanel extends JPanel {

    private static final int TAILLE_PAGE = 10;
    private static final DateTimeFormatter FORMAT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    private static final AssuranceDTO AUCUNE = new AssuranceDTO(0L, "Aucune");
    private static final Color COULEUR_SUCCES = new Color(0x2E8B72);
    private static final Color COULEUR_ERREUR = new Color(0xC62828);

    private final PatientController controller;
    private final PatientTableModel modele = new PatientTableModel();
    private final JTable tableau = new JTable(modele);

    // --- recherche et pagination ---
    private final JTextField champRecherche = new JTextField();
    private final JButton boutonRechercher = ButtonFactory.create("Rechercher", ButtonStyles.PRIMARY);
    private final JButton boutonPrecedent = ButtonFactory.create("Précédent", ButtonStyles.OUTLINED);
    private final JButton boutonSuivant = ButtonFactory.create("Suivant", ButtonStyles.OUTLINED);
    private final JLabel libelleTotal = new JLabel(" ");
    private final JLabel libellePage = new JLabel(" ");
    private int pageCourante = 1;
    private int totalPages = 1;
    private boolean rechercheEnCours;

    // --- formulaire ---
    private final JTextField champNom = new JTextField();
    private final JTextField champPrenom = new JTextField();
    private final JTextField champDate = new JTextField();
    private final JComboBox<Sexe> champSexe = new JComboBox<>(Sexe.values());
    private final JTextField champTelephone = new JTextField();
    private final JTextField champCin = new JTextField();
    private final JTextField champEmail = new JTextField();
    private final JTextField champAdresse = new JTextField();
    private final DefaultComboBoxModel<AssuranceDTO> modeleAssurance = new DefaultComboBoxModel<>();
    private final JComboBox<AssuranceDTO> champAssurance = new JComboBox<>(modeleAssurance);

    private final FormFieldPanel groupeNom = new FormFieldPanel("Nom", champNom, true);
    private final FormFieldPanel groupePrenom = new FormFieldPanel("Prénom", champPrenom, true);
    private final FormFieldPanel groupeDate = new FormFieldPanel("Date de naissance", champDate, true);
    private final FormFieldPanel groupeSexe = new FormFieldPanel("Sexe", champSexe, true);
    private final FormFieldPanel groupeTelephone = new FormFieldPanel("Téléphone", champTelephone, true);
    private final FormFieldPanel groupeCin = new FormFieldPanel("CIN", champCin, false);
    private final FormFieldPanel groupeEmail = new FormFieldPanel("E-mail", champEmail, false);
    private final FormFieldPanel groupeAdresse = new FormFieldPanel("Adresse", champAdresse, false);
    private final FormFieldPanel groupeAssurance = new FormFieldPanel("Assurance", champAssurance, false);

    private final JButton boutonEffacer = ButtonFactory.create("Effacer", ButtonStyles.OUTLINED);
    private final JButton boutonValider = ButtonFactory.create("Valider", ButtonStyles.PRIMARY);
    private final JLabel message = new JLabel(" ");

    public PatientPanel(PatientController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(20, 24, 20, 24));

        var titre = new JLabel("Liste des patients");
        titre.setFont(UiTypography.pageTitle());
        titre.setForeground(UiColors.PRIMARY);
        add(titre, BorderLayout.NORTH);
        add(creerCarteListe(), BorderLayout.CENTER);
        add(creerCarteFormulaire(), BorderLayout.SOUTH);

        champSexe.setSelectedIndex(-1); // aucun choix par défaut : le sexe est obligatoire
        champDate.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "jj/mm/aaaa");
        champRecherche.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT,
                "Nom, prénom, téléphone ou identifiant");
        modeleAssurance.addElement(AUCUNE);

        boutonRechercher.addActionListener(e -> charger(1));
        champRecherche.addActionListener(e -> charger(1));
        boutonPrecedent.addActionListener(e -> charger(pageCourante - 1));
        boutonSuivant.addActionListener(e -> charger(pageCourante + 1));
        boutonValider.addActionListener(e -> valider());
        boutonEffacer.addActionListener(e -> {
            reinitialiser();
            afficherMessage(" ", false);
        });

        majPagination();
        chargerAssurances();
        charger(1);
    }

    // =====================================================================
    // Construction de l'interface
    // =====================================================================

    private JPanel creerCarteListe() {
        var carte = new CardPanel(new BorderLayout(0, 10));

        var barre = new JPanel(new BorderLayout(10, 0));
        barre.setOpaque(false);
        barre.add(champRecherche, BorderLayout.CENTER);
        barre.add(boutonRechercher, BorderLayout.EAST);

        TableStyler.applyTo(tableau);
        var defilement = new JScrollPane(tableau);
        defilement.setBorder(BorderFactory.createLineBorder(UiColors.BORDER));
        defilement.getViewport().setBackground(UiColors.SURFACE);

        libelleTotal.setFont(UiTypography.label());
        libelleTotal.setForeground(UiColors.SECONDARY);
        libellePage.setFont(UiTypography.label());
        libellePage.setForeground(UiColors.SECONDARY);
        var navigation = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navigation.setOpaque(false);
        navigation.add(boutonPrecedent);
        navigation.add(libellePage);
        navigation.add(boutonSuivant);
        var pied = new JPanel(new BorderLayout());
        pied.setOpaque(false);
        pied.add(libelleTotal, BorderLayout.WEST);
        pied.add(navigation, BorderLayout.EAST);

        carte.add(barre, BorderLayout.NORTH);
        carte.add(defilement, BorderLayout.CENTER);
        carte.add(pied, BorderLayout.SOUTH);
        return carte;
    }

    private JPanel creerCarteFormulaire() {
        var carte = new CardPanel(new BorderLayout(0, 10));

        var titre = new JLabel("Formulaire patient");
        titre.setFont(UiTypography.sectionTitle());
        titre.setForeground(UiColors.TEXT);

        var grille = new JPanel(new GridLayout(3, 3, 16, 6));
        grille.setOpaque(false);
        grille.add(groupeNom);
        grille.add(groupePrenom);
        grille.add(groupeDate);
        grille.add(groupeSexe);
        grille.add(groupeTelephone);
        grille.add(groupeCin);
        grille.add(groupeEmail);
        grille.add(groupeAdresse);
        grille.add(groupeAssurance);

        message.setFont(UiTypography.label());
        var boutons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        boutons.setOpaque(false);
        boutons.add(boutonEffacer);
        boutons.add(boutonValider);
        var bas = new JPanel(new BorderLayout(12, 0));
        bas.setOpaque(false);
        bas.add(message, BorderLayout.CENTER);
        bas.add(boutons, BorderLayout.EAST);

        carte.add(titre, BorderLayout.NORTH);
        carte.add(grille, BorderLayout.CENTER);
        carte.add(bas, BorderLayout.SOUTH);
        return carte;
    }

    // =====================================================================
    // Recherche et pagination
    // =====================================================================

    private void chargerAssurances() {
        controller.chargerAssurances(liste -> {
            modeleAssurance.removeAllElements();
            modeleAssurance.addElement(AUCUNE);
            liste.forEach(modeleAssurance::addElement);
        }, texte -> afficherMessage(texte, true));
    }

    private void charger(int page) {
        setRechercheEnCours(true);
        controller.rechercher(champRecherche.getText().trim(), Math.max(1, page), TAILLE_PAGE, resultat -> {
            if (resultat.getElements().isEmpty() && page > 1 && resultat.getTotalElements() > 0) {
                charger(resultat.getTotalPages()); // la page demandée n'existe plus
                return;
            }
            pageCourante = resultat.getPageActuelle();
            totalPages = Math.max(1, resultat.getTotalPages());
            modele.setRows(resultat.getElements());
            int total = resultat.getTotalElements();
            libelleTotal.setText(total == 0 ? "Aucun patient trouvé."
                    : total + (total == 1 ? " patient" : " patients"));
            libellePage.setText("Page " + pageCourante + " / " + totalPages);
            setRechercheEnCours(false);
        }, texte -> {
            setRechercheEnCours(false);
            afficherMessage(texte, true);
        });
    }

    private void setRechercheEnCours(boolean enCours) {
        rechercheEnCours = enCours;
        majPagination();
    }

    private void majPagination() {
        boutonRechercher.setEnabled(!rechercheEnCours);
        boutonPrecedent.setEnabled(!rechercheEnCours && pageCourante > 1);
        boutonSuivant.setEnabled(!rechercheEnCours && pageCourante < totalPages);
    }

    // =====================================================================
    // Création d'un patient
    // =====================================================================

    private void valider() {
        effacerErreurs();
        boolean valide = true;

        if (champNom.getText().isBlank()) {
            groupeNom.setError("Le nom est obligatoire.");
            valide = false;
        }
        if (champPrenom.getText().isBlank()) {
            groupePrenom.setError("Le prénom est obligatoire.");
            valide = false;
        }

        LocalDate naissance = null;
        var texteDate = champDate.getText().trim();
        if (texteDate.isEmpty()) {
            groupeDate.setError("La date de naissance est obligatoire.");
            valide = false;
        } else {
            try {
                naissance = LocalDate.parse(texteDate, FORMAT_DATE);
                if (naissance.isAfter(LocalDate.now())) {
                    groupeDate.setError("La date ne peut pas être dans le futur.");
                    valide = false;
                }
            } catch (DateTimeParseException e) {
                groupeDate.setError("Date invalide (format jj/mm/aaaa).");
                valide = false;
            }
        }

        if (champSexe.getSelectedItem() == null) {
            groupeSexe.setError("Le sexe est obligatoire.");
            valide = false;
        }
        if (champTelephone.getText().isBlank()) {
            groupeTelephone.setError("Le téléphone est obligatoire.");
            valide = false;
        }

        if (!valide) {
            afficherMessage("Veuillez corriger les champs signalés.", true);
            return;
        }

        var patient = new Patient();
        patient.setNom(champNom.getText());
        patient.setPrenom(champPrenom.getText());
        patient.setDateNaissance(naissance);
        patient.setSexe((Sexe) champSexe.getSelectedItem());
        patient.setTelephone(champTelephone.getText());
        patient.setCin(champCin.getText());
        patient.setEmail(champEmail.getText());
        patient.setAdresse(champAdresse.getText());
        var assurance = (AssuranceDTO) champAssurance.getSelectedItem();
        patient.setIdAssurance(assurance == null || assurance.id() == 0L ? null : assurance.id());

        enregistrer(patient);
    }

    private void enregistrer(Patient patient) {
        setEnregistrement(true);
        afficherMessage("Enregistrement en cours...", false);
        controller.creerPatient(patient, cree -> {
            setEnregistrement(false);
            reinitialiser();
            afficherMessage("Patient créé avec son dossier médical : "
                    + cree.getPrenom() + " " + cree.getNom() + ".", COULEUR_SUCCES);
            champRecherche.setText(cree.getNom()); // le nouveau patient apparaît dans la liste
            charger(1);
        }, texte -> {
            setEnregistrement(false);
            afficherErreurService(texte);
        });
    }

    private void setEnregistrement(boolean enCours) {
        boutonValider.setEnabled(!enCours);
        boutonEffacer.setEnabled(!enCours);
    }

    /** Rattache le message du service au champ concerné ; sinon l'affiche en bas du formulaire. */
    private void afficherErreurService(String texte) {
        effacerErreurs();
        if (texte.startsWith("Le prénom")) {
            groupePrenom.setError(texte);
        } else if (texte.startsWith("Le nom")) {
            groupeNom.setError(texte);
        } else if (texte.contains("date de naissance")) {
            groupeDate.setError(texte);
        } else if (texte.contains("sexe")) {
            groupeSexe.setError(texte);
        } else if (texte.toLowerCase().contains("téléphone")) {
            groupeTelephone.setError(texte);
        } else if (texte.contains("CIN")) {
            groupeCin.setError(texte);
        } else if (texte.contains("e-mail")) {
            groupeEmail.setError(texte);
        }
        afficherMessage(texte, true);
    }

    private void effacerErreurs() {
        groupeNom.clearError();
        groupePrenom.clearError();
        groupeDate.clearError();
        groupeSexe.clearError();
        groupeTelephone.clearError();
        groupeCin.clearError();
        groupeEmail.clearError();
        groupeAdresse.clearError();
        groupeAssurance.clearError();
    }

    private void reinitialiser() {
        effacerErreurs();
        champNom.setText("");
        champPrenom.setText("");
        champDate.setText("");
        champSexe.setSelectedIndex(-1);
        champTelephone.setText("");
        champCin.setText("");
        champEmail.setText("");
        champAdresse.setText("");
        champAssurance.setSelectedItem(AUCUNE);
    }

    private void afficherMessage(String texte, boolean erreur) {
        afficherMessage(texte, erreur ? COULEUR_ERREUR : UiColors.SECONDARY);
    }

    private void afficherMessage(String texte, Color couleur) {
        message.setText(texte == null || texte.isBlank() ? " " : texte);
        message.setForeground(couleur);
    }
}
