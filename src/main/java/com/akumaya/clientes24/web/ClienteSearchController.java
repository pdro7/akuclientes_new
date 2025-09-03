package com.akumaya.clientes24.web;

import com.akumaya.clientes24.search.ClienteDoc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
public class ClienteSearchController {

    @Autowired
    private ElasticsearchOperations esOps;

    // Campos permitidos para ordenar
    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "horaRegistro", "horaRegistro",
            "edadHijo", "edadHijo",
            "ciudad", "ciudad",
            "departamento", "departamento"
    );

    // --------------------------
    // Construcción de Criteria
    // --------------------------
    private Criteria buildCriteria(String q, String ciudad, String departamento,
                                   Integer edadMin, Integer edadMax,
                                   String fechaDesde, String fechaHasta) {
        Criteria criteria = new Criteria();

        if (q != null && !q.isBlank()) {
            // Para facetas, usar contains en lugar de fuzzy para mejor rendimiento
            Criteria texto = new Criteria("nombreTutor").contains(q)
                    .or(new Criteria("nombreHijo").contains(q))
                    .or(new Criteria("comoNosConocio").contains(q));
            criteria = criteria.and(texto);
        }
        if (ciudad != null && !ciudad.isBlank()) {
            criteria = criteria.and(new Criteria("ciudad").is(ciudad));
        }
        if (departamento != null && !departamento.isBlank()) {
            criteria = criteria.and(new Criteria("departamento").is(departamento));
        }
        if (edadMin != null) {
            criteria = criteria.and(new Criteria("edadHijo").greaterThanEqual(edadMin));
        }
        if (edadMax != null) {
            criteria = criteria.and(new Criteria("edadHijo").lessThanEqual(edadMax));
        }

        if (fechaDesde != null && !fechaDesde.isBlank()) {
            var from = LocalDate.parse(fechaDesde).atStartOfDay().atOffset(ZoneOffset.UTC);
            criteria = criteria.and(new Criteria("horaRegistro").greaterThanEqual(from));
        }
        if (fechaHasta != null && !fechaHasta.isBlank()) {
            var to = LocalDate.parse(fechaHasta).plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
            criteria = criteria.and(new Criteria("horaRegistro").lessThan(to));
        }

        return criteria;
    }

    private Sort resolveSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return null;
        var parts = sortParam.split(",", 2);
        var field = parts[0].trim();
        var dir = (parts.length > 1 ? parts[1].trim().toLowerCase() : "asc");
        var mapped = ALLOWED_SORTS.get(field);
        if (mapped == null) return null;
        return "desc".equals(dir) ? Sort.by(Sort.Order.desc(mapped)) : Sort.by(Sort.Order.asc(mapped));
    }

    // --------------------------
    // Endpoint de búsqueda con facets opcionales
    // --------------------------
    @GetMapping("/search")
    public SearchResponse<ClienteDoc> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) Integer edadMin,
            @RequestParam(required = false) Integer edadMax,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "false") boolean includeFacets
    ) {
        var sortObj = resolveSort(sort);
        Pageable pageable = (sortObj == null)
                ? PageRequest.of(page, size, Sort.by(Sort.Order.desc("horaRegistro")))
                : PageRequest.of(page, size, sortObj);

        SearchHits<ClienteDoc> hits;
        
        if (q != null && !q.isBlank()) {
            // Usar StringQuery para búsqueda de texto simple
            String queryJson = String.format("""
                {
                  "multi_match": {
                    "query": "%s",
                    "fields": ["nombreTutor", "nombreHijo", "comoNosConocio"]
                  }
                }
                """, q.replace("\"", "\\\""));
                
            var query = new StringQuery(queryJson);
            query.setPageable(pageable);
            hits = esOps.search(query, ClienteDoc.class);
        } else {
            // Usar consulta de criteria para filtros sin texto
            var criteria = buildCriteria(q, ciudad, departamento, edadMin, edadMax, fechaDesde, fechaHasta);
            var query = new CriteriaQuery(criteria);
            query.setPageable(pageable);
            hits = esOps.search(query, ClienteDoc.class);
        }
        var items = hits.stream().map(SearchHit::getContent).toList();
        var total = hits.getTotalHits();

        if (!includeFacets) {
            return new SearchResponse<>(items, page, size, total);
        }

        // facets contextuales: construir criteria para facetas
        var criteriaForFacets = buildCriteria(q, ciudad, departamento, edadMin, edadMax, fechaDesde, fechaHasta);
        var qFacets = new CriteriaQuery(criteriaForFacets);
        qFacets.setPageable(PageRequest.of(0, 10_000));
        var all = esOps.search(qFacets, ClienteDoc.class).stream()
                .map(SearchHit::getContent).toList();

        var facets = computeFacets(all, total);
        return new SearchResponse<>(items, page, size, total, facets);
    }

    // --------------------------
    // Endpoint solo de facets contextuales
    // --------------------------
    @GetMapping("/facets")
    public FacetsResponse facets(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) Integer edadMin,
            @RequestParam(required = false) Integer edadMax,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta
    ) {
        var criteria = buildCriteria(q, ciudad, departamento, edadMin, edadMax, fechaDesde, fechaHasta);
        var qy = new CriteriaQuery(criteria);
        qy.setPageable(PageRequest.of(0, 10_000));

        var hits = esOps.search(qy, ClienteDoc.class);
        var docs = hits.stream().map(SearchHit::getContent).toList();
        return computeFacets(docs, hits.getTotalHits());
    }

    // --------------------------
    // Cálculo de facets
    // --------------------------
    private FacetsResponse computeFacets(List<ClienteDoc> docs, long total) {
        var byCiudad = docs.stream().map(ClienteDoc::getCiudad)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        var byDepto = docs.stream().map(ClienteDoc::getDepartamento)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        var byEdadBucket = docs.stream().map(ClienteDoc::getEdadHijo)
                .filter(e -> e != null && e >= 0)
                .map(this::bucketEdad)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        var ciudades = byCiudad.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .map(e -> new FacetEntry(e.getKey(), e.getValue()))
                .toList();

        var departamentos = byDepto.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(20)
                .map(e -> new FacetEntry(e.getKey(), e.getValue()))
                .toList();

        var ordenEdades = List.of("0-6", "7-9", "10-12", "13-17", "18+");
        var edades = ordenEdades.stream()
                .map(label -> new FacetEntry(label, byEdadBucket.getOrDefault(label, 0L)))
                .toList();

        return new FacetsResponse(ciudades, departamentos, edades, total);
    }

    private String bucketEdad(Integer e) {
        if (e == null) return null;
        if (e <= 6) return "0-6";
        if (e <= 9) return "7-9";
        if (e <= 12) return "10-12";
        if (e <= 17) return "13-17";
        return "18+";
    }
}
