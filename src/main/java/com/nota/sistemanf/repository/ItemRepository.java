package com.nota.sistemanf.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.nota.sistemanf.entidades.Item;

public interface ItemRepository extends PagingAndSortingRepository<Item, Integer>{

}
