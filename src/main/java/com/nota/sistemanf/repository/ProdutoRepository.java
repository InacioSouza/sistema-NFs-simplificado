package com.nota.sistemanf.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.nota.sistemanf.entidades.Produto;

public interface ProdutoRepository extends PagingAndSortingRepository<Produto, Integer> {

	Produto findByNomeIgnoreCase(String nome);

}
