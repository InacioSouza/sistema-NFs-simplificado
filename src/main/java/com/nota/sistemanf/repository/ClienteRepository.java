package com.nota.sistemanf.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.nota.sistemanf.entidades.Cliente;

public interface ClienteRepository extends PagingAndSortingRepository<Cliente, Integer> {

	Cliente findByNomeIgnoreCase(String nome);

}
