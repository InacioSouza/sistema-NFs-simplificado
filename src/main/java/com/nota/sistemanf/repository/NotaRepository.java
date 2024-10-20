package com.nota.sistemanf.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.nota.sistemanf.entidades.Nota;

public interface NotaRepository extends PagingAndSortingRepository<Nota, Integer> {

}
