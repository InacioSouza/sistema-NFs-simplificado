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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nota.sistemanf.entidades.Produto;
import com.nota.sistemanf.repository.ProdutoRepository;
import com.nota.sistemanf.services.StatusRegistro;
import com.nota.sistemanf.services.Valida;

@RestController
@RequestMapping("snf/produtos")
public class ProdutoController {
	private ProdutoRepository produtoRepo;
	private Valida valida;

	public ProdutoController(ProdutoRepository produtoRepo, Valida valida) {
		this.produtoRepo = produtoRepo;
		this.valida = valida;
	}

	@PostMapping
	public List<String> criaProduto(@RequestBody List<Produto> produtos) {

		List<String> statusCadastro = new ArrayList<String>();

		produtos.forEach(produto -> {
			String statusRegistro = "";

			StatusRegistro status = valida.produto(produto);

			switch (status) {
			case OK:
				produtoRepo.save(produto);
				statusRegistro += produto.getNome() + " : ok";
				break;

			case NULL:
				statusRegistro += "null : nao cadastrado";
				break;

			case PRESENTE_NO_BD:
				statusRegistro += produto.getNome() + " : presente no BD";
				break;

			case ATRIBUTOS_INVALIDOS:
				statusRegistro += " ! : contem atributos invalidos";
				break;
			}

			statusCadastro.add(statusRegistro);
		});

		return statusCadastro;
	}

	@GetMapping
	public List<Produto> listaProdutos() {
		return (List<Produto>) produtoRepo.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Produto> buscarProdutoPorId(@PathVariable Integer id) {
		Produto produto = produtoRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

		return ResponseEntity.ok(produto);
	}

	@GetMapping("/buscar")
	public ResponseEntity<Produto> buscarProdutoPorNome(@RequestParam String nome) {

		Produto produto = produtoRepo.findByNomeIgnoreCase(nome);

		if (produto == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(produto);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Produto> atualizaProduto(@PathVariable Integer id, @RequestBody Produto produtoAtualizado) {
		Produto produtoExistente = produtoRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "produto não encontrado"));

		if (produtoAtualizado.getNome() != null) {
			produtoExistente.setNome(produtoAtualizado.getNome());
		}

		if (produtoAtualizado.getPreco() != null) {
			produtoExistente.setPreco(produtoAtualizado.getPreco());
		}
		return ResponseEntity.ok(produtoRepo.save(produtoExistente));
	}

	@DeleteMapping("/{id}")
	public Produto deletaProduto(@PathVariable Integer id) {

		Produto produto = produtoRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

		produtoRepo.deleteById(id);
		return produto;
	}
}
