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


import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteSearchController {

    private final ElasticsearchOperations esOps;

    public ClienteSearchController(ElasticsearchOperations esOps) {
        this.esOps = esOps;
    }

    @GetMapping("/search")
    public List<ClienteDoc> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String departamento,
            @RequestParam(required = false) Integer edadMin,
            @RequestParam(required = false) Integer edadMax
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

        Query query = new CriteriaQuery(criteria);
        query.setPageable(Pageable.ofSize(100));

        SearchHits<ClienteDoc> hits = esOps.search(query, ClienteDoc.class);
        return hits.stream().map(SearchHit::getContent).toList();
    }
}
