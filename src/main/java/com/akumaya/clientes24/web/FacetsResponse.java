package com.akumaya.clientes24.web;

import java.util.List;

public class FacetsResponse {
    private List<FacetEntry> ciudades;
    private List<FacetEntry> departamentos;
    private List<FacetEntry> edades; // en rangos (7-9, 10-12, etc.)
    private long total;

    public FacetsResponse(List<FacetEntry> ciudades, List<FacetEntry> departamentos, List<FacetEntry> edades, long total) {
        this.ciudades = ciudades;
        this.departamentos = departamentos;
        this.edades = edades;
        this.total = total;
    }

    public List<FacetEntry> getCiudades() { return ciudades; }
    public List<FacetEntry> getDepartamentos() { return departamentos; }
    public List<FacetEntry> getEdades() { return edades; }
    public long getTotal() { return total; }
}
