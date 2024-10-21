package com.nota.sistemanf.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nota.sistemanf.entidades.Item;
import com.nota.sistemanf.entidades.Produto;
import com.nota.sistemanf.repository.ItemRepository;
import com.nota.sistemanf.repository.ProdutoRepository;

@Service
public class Cadastra {

	private Valida valida;
	private ItemRepository itemRepo;
	private ProdutoRepository produtoRepo;

	@Autowired
	public Cadastra(Valida valida, ItemRepository itemRepo, ProdutoRepository produtoRepo) {
		this.valida = valida;
		this.itemRepo = itemRepo;
		this.produtoRepo = produtoRepo;
	}

	public void itens(List<Item> itens) {

		itens.forEach(item -> {
			if (valida.item(item) == StatusRegistro.OK) {

				StatusRegistro statusProduto = valida.produto(item.getProduto());

				Produto produto = null;

				if (statusProduto == StatusRegistro.PRESENTE_NO_BD) {
					produto = produtoRepo.findByNomeIgnoreCase(item.getProduto().getNome());

					item.setProduto(produto);

				} else if (statusProduto == StatusRegistro.OK) {
					item.setProduto(produtoRepo.save(item.getProduto()));
				}

				itemRepo.save(item);
			}
		});
	}
}
