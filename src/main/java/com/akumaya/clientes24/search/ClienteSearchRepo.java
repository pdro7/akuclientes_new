package com.akumaya.clientes24.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.UUID;

public interface ClienteSearchRepo extends ElasticsearchRepository<ClienteDoc, UUID> {}
