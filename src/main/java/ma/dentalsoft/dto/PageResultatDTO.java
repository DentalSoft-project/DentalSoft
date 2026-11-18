package ma.dentalsoft.dto;

import java.util.List;

public class PageResultatDTO<T> {
    private List<T> elements;
    private int totalElements;
    private int pageActuelle;
    private int totalPages;

    public PageResultatDTO(List<T> elements, int totalElements, int pageActuelle, int taillePage) {
        this.elements = elements;
        this.totalElements = totalElements;
        this.pageActuelle = pageActuelle;
        this.totalPages = (int) Math.ceil((double) totalElements / taillePage);
    }

    // Getters
    public List<T> getElements() { return elements; }
    public int getTotalElements() { return totalElements; }
    public int getPageActuelle() { return pageActuelle; }
    public int getTotalPages() { return totalPages; }
}