package com.nota.sistemanf.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nota.sistemanf.entidades.Item;

public interface ItemRepository extends PagingAndSortingRepository<Item, Integer> {

	@Modifying
	@Transactional
	@Query("DELETE FROM Item i WHERE i.nota.id = :notaId")
	void deleteItemsByNotaId(@Param("notaId") Integer notaId);

	List<Item> findByNotaId(Integer id);

}
