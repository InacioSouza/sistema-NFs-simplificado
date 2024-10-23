package com.nota.sistemanf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import com.nota.sistemanf.entidades.Nota;

public interface NotaRepository extends PagingAndSortingRepository<Nota, Integer> {

	Nota findByNumero(String numero);

	@Modifying
	@Transactional
	Nota deleteByNumero(String numero);

	List<Nota> findByClienteId(Integer id);


}
