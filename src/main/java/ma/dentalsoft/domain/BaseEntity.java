package ma.dentalsoft.domain;

import java.time.LocalDateTime;

/**
 * SOCLE TEMPORAIRE M3 - à remplacer par la version de Zineb (EN-04).
 * Colonnes d'audit communes (RG6) : id, date_creation,
 * date_derniere_modification, cree_par, modifie_par.
 */
public abstract class BaseEntity {

    private Long id;
    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereModification;
    private Long creePar;
    private Long modifiePar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateDerniereModification() {
        return dateDerniereModification;
    }

    public void setDateDerniereModification(LocalDateTime dateDerniereModification) {
        this.dateDerniereModification = dateDerniereModification;
    }

    public Long getCreePar() {
        return creePar;
    }

    public void setCreePar(Long creePar) {
        this.creePar = creePar;
    }

    public Long getModifiePar() {
        return modifiePar;
    }

    public void setModifiePar(Long modifiePar) {
        this.modifiePar = modifiePar;
    }
}