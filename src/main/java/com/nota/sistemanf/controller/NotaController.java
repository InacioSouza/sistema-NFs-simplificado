package com.nota.sistemanf.controller;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.nota.sistemanf.entidades.Cliente;
import com.nota.sistemanf.entidades.Item;
import com.nota.sistemanf.entidades.Nota;
import com.nota.sistemanf.entidades.Produto;
import com.nota.sistemanf.repository.ClienteRepository;
import com.nota.sistemanf.repository.ItemRepository;
import com.nota.sistemanf.repository.NotaRepository;
import com.nota.sistemanf.repository.ProdutoRepository;
import com.nota.sistemanf.services.Cadastra;
import com.nota.sistemanf.services.StatusRegistro;
import com.nota.sistemanf.services.Valida;

@RestController
@RequestMapping("/snf/notas")
public class NotaController {

	private NotaRepository notaRepo;
	private ClienteRepository clienteRepo;
	private ItemRepository itemRepo;
	private ProdutoRepository produtoRepo;
	private Valida valida;

	public NotaController() {

	}

	@Autowired
	public NotaController(NotaRepository notaRepo, ClienteRepository clienteRepo, ItemRepository itemRepo,
			Valida valida, Cadastra cadastra, ProdutoRepository produtoRepo) {
		this.notaRepo = notaRepo;
		this.clienteRepo = clienteRepo;
		this.itemRepo = itemRepo;
		this.produtoRepo = produtoRepo;
		this.valida = valida;
	}

	@PostMapping
	String cadastraNota(@RequestBody Nota nota) {

		if (nota.getItens() == null || nota.getItens().size() == 0) {
			return "! : Deve haver ao menos 1 item na nota";
		}

		if (nota.getCliente() == null || nota.getCliente().getNome() == null) {
			return "! : Nenhum cliente adicionado";
		} else {
			Cliente clienteNota = clienteRepo.findByNomeIgnoreCase(nota.getCliente().getNome());

			if (clienteNota == null) {
				clienteNota = clienteRepo.save(nota.getCliente());
			}

			nota.setCliente(clienteNota);
		}

		try {
			Nota notaComId = notaRepo.save(nota);

			for (Item itemNota : nota.getItens()) {

				itemNota.setNota(notaComId);

				Produto produtoItem = produtoRepo.findByNomeIgnoreCase(itemNota.getProduto().getNome());

				if (produtoItem == null) {
					produtoItem = produtoRepo.save(itemNota.getProduto());
				}

				itemNota.setProduto(produtoItem);

				itemRepo.save(itemNota);
			}

			String numeroNota = notaComId.getCliente().getId() + "." + notaComId.getItens().get(0).getId();

			notaComId.setNumero(numeroNota);

			notaRepo.save(notaComId);

		} catch (Exception e) {
			notaRepo.deleteById(nota.getId());
			return "! : Erro ao cadastrar a nota";
		}

		return "ok : nota cadastrada";
	}

	@GetMapping
	List<Nota> listaNotas() {
		return (List<Nota>) notaRepo.findAll();
	}

	@GetMapping("/{id}")
	ResponseEntity<Nota> buscaNotaPorId(@PathVariable Integer id) {

		Nota nota = notaRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota não encontrada"));

		return ResponseEntity.ok(nota);
	}

	@GetMapping("/buscar")
	Nota buscaNotaPorNumero(@RequestParam String numero) {
		Nota nota = notaRepo.findByNumero(numero);

		if (nota == null) {
			return null;
		}

		return nota;
	}

	@GetMapping("/cliente/{id}")
	List<Nota> buscarNotasPorIdCliente(@PathVariable Integer id) {
		List<Nota> notas = notaRepo.findByClienteId(id);

		return notas;
	}

	@GetMapping("/cliente")
	List<Nota> buscarNotasPorNomeCliente(@RequestParam String nome) {
		Cliente cliente = clienteRepo.findByNomeIgnoreCase(nome);

		if (cliente == null) {
			return null;
		}

		List<Nota> notas = notaRepo.findByClienteId(cliente.getId());

		if (notas == null) {
			return new ArrayList<Nota>();
		}

		return notas;
	}

	@PutMapping("{id}")
	ResponseEntity<Nota> alteraNota(@PathVariable Integer id, @RequestBody Nota notaAtualizada) {

		int qtdItensNotaAtualizada = notaAtualizada.getItens().size();

		if (qtdItensNotaAtualizada == 0) {
			return null;
		}

		Nota notaExistente = notaRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota não encontrada"));

		List<Item> itensValidados = new ArrayList<Item>();

		notaExistente.getItens().forEach(item -> {
			itemRepo.deleteById(item.getId());
		});

		notaAtualizada.getItens().forEach(item -> {

			item.setNota(notaExistente);

			StatusRegistro statusItem = valida.item(item);

			System.out.println("\n\n Qual é o status do item ? " + statusItem + "\n\n");
			if (statusItem == StatusRegistro.OK) {

				StatusRegistro statusproduto = valida.produto(item.getProduto());

				Produto produto = null;

				if (statusproduto == StatusRegistro.PRESENTE_NO_BD) {
					produto = produtoRepo.findByNomeIgnoreCase(item.getProduto().getNome());
				} else if (statusproduto == StatusRegistro.OK) {
					produto = produtoRepo.save(item.getProduto());
				} else {
					return;
				}

				item.setProduto(produto);

				itensValidados.add(item);
			}

		});

		if (itensValidados.size() != 0) {
			notaExistente.setItens((List<Item>) itemRepo.saveAll(itensValidados));
			notaExistente.calcValorTotalNota();
		} else {
			return null;
		}

		if (notaAtualizada.getCliente() != null) {
			if (notaExistente.getCliente().getNome() != notaAtualizada.getCliente().getNome()
					&& notaAtualizada.getCliente().getNome() != null) {

				StatusRegistro statusCliente = valida.cliente(notaAtualizada.getCliente());
				Cliente clienteFinal = null;

				if (statusCliente == StatusRegistro.PRESENTE_NO_BD) {
					clienteFinal = clienteRepo.findByNomeIgnoreCase(notaAtualizada.getCliente().getNome());
				} else {
					clienteFinal = clienteRepo.save(notaAtualizada.getCliente());
				}

				notaExistente.setCliente(clienteFinal);
			}
		}

		notaExistente.setNumero(notaExistente.getCliente().getId() + "." + notaExistente.getItens().get(0).getId());

		return ResponseEntity.ok(notaRepo.save(notaExistente));
	}

	@PutMapping("/buscar")
	ResponseEntity<Nota> alteraNotaPorNumero(@RequestParam String numero, @RequestBody Nota notaAtualizada) {

		int qtdItensNotaAtualizada = notaAtualizada.getItens().size();

		if (qtdItensNotaAtualizada == 0) {
			return null;
		}

		Nota notaExistente = notaRepo.findByNumero(numero);

		if (notaExistente == null) {
			return null;
		}

		List<Item> itensValidados = new ArrayList<Item>();

		notaExistente.getItens().forEach(item -> {
			itemRepo.deleteById(item.getId());
		});

		notaAtualizada.getItens().forEach(item -> {

			item.setNota(notaExistente);

			StatusRegistro statusItem = valida.item(item);

			System.out.println("\n\n Qual é o status do item ? " + statusItem + "\n\n");
			if (statusItem == StatusRegistro.OK) {

				StatusRegistro statusproduto = valida.produto(item.getProduto());

				Produto produto = null;

				if (statusproduto == StatusRegistro.PRESENTE_NO_BD) {
					produto = produtoRepo.findByNomeIgnoreCase(item.getProduto().getNome());
				} else if (statusproduto == StatusRegistro.OK) {
					produto = produtoRepo.save(item.getProduto());
				} else {
					return;
				}

				item.setProduto(produto);

				itensValidados.add(item);
			}

		});

		if (itensValidados.size() != 0) {
			notaExistente.setItens((List<Item>) itemRepo.saveAll(itensValidados));
			notaExistente.calcValorTotalNota();
		} else {
			return null;
		}

		if (notaAtualizada.getCliente() != null) {
			if (notaExistente.getCliente().getNome() != notaAtualizada.getCliente().getNome()
					&& notaAtualizada.getCliente().getNome() != null) {

				StatusRegistro statusCliente = valida.cliente(notaAtualizada.getCliente());
				Cliente clienteFinal = null;

				if (statusCliente == StatusRegistro.PRESENTE_NO_BD) {
					clienteFinal = clienteRepo.findByNomeIgnoreCase(notaAtualizada.getCliente().getNome());
				} else {
					clienteFinal = clienteRepo.save(notaAtualizada.getCliente());
				}

				notaExistente.setCliente(clienteFinal);
			}
		}

		notaExistente.setNumero(notaExistente.getCliente().getId() + "." + notaExistente.getItens().get(0).getId());

		return ResponseEntity.ok(notaRepo.save(notaExistente));
	}

	@DeleteMapping("/{id}")
	Nota deletaNotaPorId(@PathVariable Integer id) {

		Nota nota = notaRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota não encontrada"));
		Hibernate.initialize(nota.getItens());

		nota.getItens().forEach(item -> {
			itemRepo.deleteById(item.getId());
		});

		notaRepo.deleteById(id);

		return nota;
	}
}
