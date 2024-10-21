package com.nota.sistemanf.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nota.sistemanf.entidades.Item;
import com.nota.sistemanf.entidades.Produto;
import com.nota.sistemanf.repository.ItemRepository;
import com.nota.sistemanf.repository.ProdutoRepository;
import com.nota.sistemanf.services.StatusRegistro;
import com.nota.sistemanf.services.Valida;

@RestController
@RequestMapping("snf/itens")
public class ItemController {

	private ItemRepository itemRepo;
	private ProdutoRepository produtoRepo;
	private Valida valida;

	public ItemController(ItemRepository itemRepo, ProdutoRepository produtoRepo, Valida valida) {
		this.itemRepo = itemRepo;
		this.produtoRepo = produtoRepo;
		this.valida = valida;
	}

	@PostMapping
	public List<String> cadastraItens(@RequestBody List<Item> itens) {
		List<String> statusCadastro = new ArrayList<String>();

		itens.forEach(item -> {
			String statusRegistro = "";
			StatusRegistro statusItem = valida.item(item);
			StatusRegistro statusProduto = valida.produto(item.getProduto());

			switch (statusItem) {
			case OK:
				Produto produto = null;

				if (statusProduto != StatusRegistro.PRESENTE_NO_BD) {
					produto = produtoRepo.save(item.getProduto());
					item.setProduto(produto);

				} else {
					produto = produtoRepo.findByNomeIgnoreCase(item.getProduto().getNome());

					if (produto != null) {
						item.setProduto(produto);
					}
				}

				itemRepo.save(item);
				statusRegistro += "item-" + item.getProduto().getNome() + "-qtd" + item.getQuantidade() + " : ok";
				break;

			case NULL:
				statusRegistro += "null : nao cadastrado";
				break;

			case DEPENDENCIA_INVALIDA:
				statusRegistro += "! : Erro no produto";
				break;

			case ATRIBUTOS_INVALIDOS:
				statusRegistro += "! : Atributo inválido";
				break;
			}

			statusCadastro.add(statusRegistro);
		});

		return statusCadastro;
	}

	@GetMapping
	List<Item> listaItens() {
		return (List<Item>) itemRepo.findAll();
	}

	@GetMapping("/{id}")
	ResponseEntity<Item> buscaItemPorId(@PathVariable Integer id) {

		Item item = itemRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));

		return ResponseEntity.ok(item);
	}

	@PutMapping("/{id}")
	ResponseEntity<Item> alteraItem(@PathVariable Integer id, @RequestBody Item itemAtualizado) {
		Item itemExistente = itemRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));

		if (itemExistente.getNumero() != itemAtualizado.getNumero() && itemAtualizado.getNumero() != null) {

			itemExistente.setNumero(itemAtualizado.getNumero());
		}

		if (itemExistente.getQuantidade() != itemAtualizado.getQuantidade() && itemAtualizado.getQuantidade() != 0) {

			itemExistente.setQuantidade((itemAtualizado.getQuantidade()));
		}

		if (!itemExistente.getProduto().equals(itemAtualizado.getProduto())) {

			StatusRegistro statusProdutoAtualizado = valida.produto(itemAtualizado.getProduto());

			if (statusProdutoAtualizado == StatusRegistro.PRESENTE_NO_BD) {

				Produto p = produtoRepo.findByNomeIgnoreCase(itemAtualizado.getProduto().getNome());

				itemExistente.setProduto(p);

			} else if (statusProdutoAtualizado == StatusRegistro.OK) {

				itemExistente.setProduto(produtoRepo.save(itemAtualizado.getProduto()));

			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro no produto");

			}
		}

		return ResponseEntity.ok(itemRepo.save(itemExistente));
	}

	@DeleteMapping("{id}")
	ResponseEntity<Item> deletaItem(@PathVariable Integer id) {

		Item item = itemRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));

		itemRepo.deleteById(id);

		return ResponseEntity.ok(item);
	}
}
