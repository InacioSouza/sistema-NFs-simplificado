package com.nota.sistemanf.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nota.sistemanf.entidades.Item;
import com.nota.sistemanf.entidades.Nota;
import com.nota.sistemanf.entidades.Produto;
import com.nota.sistemanf.repository.ItemRepository;
import com.nota.sistemanf.repository.NotaRepository;
import com.nota.sistemanf.repository.ProdutoRepository;
import com.nota.sistemanf.services.StatusRegistro;
import com.nota.sistemanf.services.Valida;

@RestController
@RequestMapping("snf/itens")
public class ItemController {

	private ItemRepository itemRepo;
	private ProdutoRepository produtoRepo;
	private NotaRepository notaRepo;
	private Valida valida;

	public ItemController(ItemRepository itemRepo, ProdutoRepository produtoRepo, NotaRepository notaRepo,
			Valida valida) {
		this.itemRepo = itemRepo;
		this.produtoRepo = produtoRepo;
		this.notaRepo = notaRepo;
		this.valida = valida;
	}

	@PostMapping
	public List<String> cadastraItensEaddNotaPorNumero(@RequestParam String numeroNota, @RequestBody List<Item> itens) {
		List<String> statusCadastro = new ArrayList<String>();

		Nota nota = notaRepo.findByNumero(numeroNota);

		if (nota == null) {
			statusCadastro.add(" ! : A nota de id : " + numeroNota + " não foi encontrada");

		} else {

			for (Item item : itens) {

				item.setNota(nota);

				String statusRegistro = "";
				StatusRegistro statusItem = valida.item(item);
				StatusRegistro statusProduto = valida.produto(item.getProduto());

				switch (statusItem) {
				case NOTA_NAO_CADASTRADA_BD:
					statusRegistro += "! : Nota não existe";
					break;

				case OK:

					Produto produto = null;

					if (statusProduto != StatusRegistro.PRESENTE_NO_BD) {
						produto = produtoRepo.save(item.getProduto());

					} else {
						produto = produtoRepo.findByNomeIgnoreCase(item.getProduto().getNome());

						if (produto != null) {
							item.setProduto(produto);
						} else {
							statusRegistro += "item-" + item.getProduto().getNome() + "-qtd" + item.getQuantidade()
									+ " : Erro ao cadastrar produto";
							statusCadastro.add(statusRegistro);
							continue;
						}
					}

					item.setProduto(produto);

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
			}
		}

		return statusCadastro;
	}

	@PostMapping("/nota/{id}")
	public List<String> cadastraItens(@PathVariable Integer id, @RequestBody List<Item> itens) {
		List<String> statusCadastro = new ArrayList<String>();

		Optional<Nota> notaOpt = notaRepo.findById(id);

		if (!notaOpt.isPresent()) {
			statusCadastro.add(" ! : A nota de id : " + id + " não foi encontrada");

		} else {

			Nota nota = notaOpt.get();

			for (Item item : itens) {

				item.setNota(nota);

				String statusRegistro = "";
				StatusRegistro statusItem = valida.item(item);
				StatusRegistro statusProduto = valida.produto(item.getProduto());

				switch (statusItem) {
				case NOTA_NAO_CADASTRADA_BD:
					statusRegistro += "! : Nota não existe";
					break;

				case OK:

					Produto produto = null;

					if (statusProduto != StatusRegistro.PRESENTE_NO_BD) {
						produto = produtoRepo.save(item.getProduto());

					} else {
						produto = produtoRepo.findByNomeIgnoreCase(item.getProduto().getNome());

						if (produto != null) {
							item.setProduto(produto);
						} else {
							statusRegistro += "item-" + item.getProduto().getNome() + "-qtd" + item.getQuantidade()
									+ " : Erro ao cadastrar produto";
							statusCadastro.add(statusRegistro);
							continue;
						}
					}

					item.setProduto(produto);

					itemRepo.save(item);

					nota.getItens().add(item);

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
			}

			nota.calcValorTotalNota();
			notaRepo.save(nota);
		}

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

	@GetMapping("/nota/{id}")
	List<Item> buscaItensPorIdNota(@PathVariable Integer id) {
		Nota nota = notaRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota não encontrada"));

		List<Item> itens = itemRepo.findByNotaId(nota.getId());

		return itens;
	}

	@GetMapping("/nota")
	List<Item> buscaItensPorNumeroNota(@RequestParam String numero) {
		Nota nota = notaRepo.findByNumero(numero);

		if (nota == null) {
			return null;
		}

		List<Item> itens = itemRepo.findByNotaId(nota.getId());

		return itens;
	}

	@PutMapping("/{id}")
	ResponseEntity<Item> alteraItem(@PathVariable Integer id, @RequestBody Item itemAtualizado) {
		Item itemExistente = itemRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));

		Optional<Nota> notaOpt = notaRepo.findById(itemExistente.getNota().getId());

		if (!notaOpt.isPresent()) {
			return null;
		}

		boolean produtoFoiModificado = false;

		if (itemAtualizado.getProduto() != null) {
			produtoFoiModificado = !itemExistente.getProduto().equals(itemAtualizado.getProduto());
		}

		if (produtoFoiModificado == true) {

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

		boolean qtdFoiModificada = itemExistente.getQuantidade() != itemAtualizado.getQuantidade()
				&& itemAtualizado.getQuantidade() != 0;

		if (qtdFoiModificada == true) {
			itemExistente.setQuantidade((itemAtualizado.getQuantidade()));
		}

		Item itemSalvo = itemRepo.save(itemExistente);

		if (itemSalvo != null) {
			Nota nota = notaOpt.get();

			nota.getItens().add(itemSalvo);
			nota.calcValorTotalNota();

			notaRepo.save(nota);
		}

		return ResponseEntity.ok(itemSalvo);
	}

	@DeleteMapping("{id}")
	ResponseEntity<Item> deletaItem(@PathVariable Integer id) {

		Item item = itemRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));

		itemRepo.deleteById(id);

		Optional<Nota> notaOpt = notaRepo.findById(item.getNota().getId());

		Nota nota = notaOpt.get();

		nota.calcValorTotalNota();

		notaRepo.save(nota);

		return ResponseEntity.ok(item);
	}
}
