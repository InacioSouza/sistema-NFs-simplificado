package com.nota.sistemanf.services;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nota.sistemanf.entidades.Item;
import com.nota.sistemanf.entidades.Nota;
import com.nota.sistemanf.entidades.Produto;
import com.nota.sistemanf.repository.ItemRepository;
import com.nota.sistemanf.repository.ProdutoRepository;

@Service
public class Cadastra {

	private Valida valida;
	private ItemRepository itemRepo;
	private ProdutoRepository produtoRepo;
	private EntityManager entManager;

	@Autowired
	public Cadastra(Valida valida, ItemRepository itemRepo, ProdutoRepository produtoRepo, EntityManager entManager) {
		this.valida = valida;
		this.itemRepo = itemRepo;
		this.produtoRepo = produtoRepo;
		this.entManager = entManager;
	}

	public void itensEaddNaNota(Nota nota) {

		if (nota != null) {

			List<Item> itens = nota.getItens();

			itens.forEach(item -> {
				
				produtoRepo.save(item.getProduto());
				
				
				itemRepo.save(item);
			});
		}
	}

	public String numeroNota(Nota nota) {

		if (nota != null) {

			int idItem = nota.getItens().get(0).getId();

			int idCliente = nota.getCliente().getId();

			return idItem + "." + idCliente;
		}

		return "not";
	}
}
