package ma.dentalsoft.domain;

import java.time.LocalDate;

/**
 * Patient : identité administrative uniquement.
 * Les données médicales (antécédents, observations) ne sont pas ici :
 * elles relèvent du dossier médical et des consultations (lots M5/M6).
 */
public class Patient extends BaseEntity {

    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private Sexe sexe;
    private String cin;
    private String telephone;
    private String email;
    private String adresse;
    private Long idAssurance;
    private boolean archive;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public Sexe getSexe() {
        return sexe;
    }

    public void setSexe(Sexe sexe) {
        this.sexe = sexe;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Long getIdAssurance() {
        return idAssurance;
    }

    public void setIdAssurance(Long idAssurance) {
        this.idAssurance = idAssurance;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }
}