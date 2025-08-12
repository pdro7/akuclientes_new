package com.akumaya.clientes24.web;

import com.akumaya.clientes24.search.ClienteDoc;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteSearchController {

    private final ElasticsearchOperations esOps;

    public ClienteSearchController(ElasticsearchOperations esOps) {
        this.esOps = esOps;
    }

    @GetMapping("/search")
    public SearchResponse<ClienteDoc> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) Integer edadMin,
            @RequestParam(required = false) Integer edadMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort
    ) {
        Criteria criteria = new Criteria(); // empieza vacío

        // full‑text: matches en varias fields
        if (q != null && !q.isBlank()) {
            Criteria texto = new Criteria("nombreTutor").matches(q)
                    .or(new Criteria("nombreHijo").matches(q))
                    .or(new Criteria("comoNosConocio").matches(q));
            criteria = criteria.and(texto);
        }

        // filtros exactos
        if (ciudad != null && !ciudad.isBlank()) {
            criteria = criteria.and(new Criteria("ciudad.keyword").is(ciudad));
        }
        if (departamento != null && !departamento.isBlank()) {
            criteria = criteria.and(new Criteria("departamento.keyword").is(departamento));
        }

        // rangos
        if (edadMin != null) {
            criteria = criteria.and(new Criteria("edadHijo").greaterThanEqual(edadMin));
        }
        if (edadMax != null) {
            criteria = criteria.and(new Criteria("edadHijo").lessThanEqual(edadMax));
        }

        Sort sortObj = resolveSort(sort);
        Pageable pageable = (sortObj == null)
                ? PageRequest.of(page, size, Sort.by(Sort.Order.desc("horaRegistro"))) // por defecto: más recientes
                : PageRequest.of(page, size, sortObj);

        Query query = new CriteriaQuery(criteria);
        query.setPageable(pageable);

        // 3) Ejecutar búsqueda
        SearchHits<ClienteDoc> hits = esOps.search(query, ClienteDoc.class);
        List<ClienteDoc> items = hits.stream().map(SearchHit::getContent).toList();
        long total = hits.getTotalHits();

        return new SearchResponse<ClienteDoc>(items, page, size, total);

    }

    // Permitir ordenar por campos seguros (mapeados en ClienteDoc)
    private static final Map<String, String> ALLOWED_SORTS = Map.of(
            "horaRegistro", "horaRegistro",
            "edadHijo", "edadHijo",
            "ciudad", "ciudad",               // ya es Keyword en el documento
            "departamento", "departamento"    // Keyword
    );

    private Sort resolveSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) return null;
        String[] parts = sortParam.split(",", 2);
        String field = parts[0].trim();
        String dir = (parts.length > 1 ? parts[1].trim().toLowerCase() : "asc");

        String mapped = ALLOWED_SORTS.get(field);
        if (mapped == null) return null; // ignora campos no permitidos

        return "desc".equals(dir)
                ? Sort.by(Sort.Order.desc(mapped))
                : Sort.by(Sort.Order.asc(mapped));
    }


}
