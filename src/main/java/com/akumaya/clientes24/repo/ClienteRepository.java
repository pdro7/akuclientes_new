package com.akumaya.clientes24.repo;

import com.akumaya.clientes24.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {}
