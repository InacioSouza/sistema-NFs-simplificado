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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.nota.sistemanf.entidades.Cliente;
import com.nota.sistemanf.entidades.Item;
import com.nota.sistemanf.entidades.Nota;
import com.nota.sistemanf.repository.ClienteRepository;
import com.nota.sistemanf.repository.ItemRepository;
import com.nota.sistemanf.repository.NotaRepository;
import com.nota.sistemanf.services.Cadastra;
import com.nota.sistemanf.services.StatusRegistro;
import com.nota.sistemanf.services.Valida;

@RestController
@RequestMapping("/snf/notas")
public class NotaController {

	private NotaRepository notaRepo;
	private ClienteRepository clienteRepo;
	private ItemRepository itemRepo;
	private Valida valida;
	private Cadastra cadastra;

	public NotaController() {

	}

	@Autowired
	public NotaController(NotaRepository notaRepo, ClienteRepository clienteRepo, ItemRepository itemRepo,
			Valida valida, Cadastra cadastra) {
		this.notaRepo = notaRepo;
		this.clienteRepo = clienteRepo;
		this.itemRepo = itemRepo;
		this.valida = valida;
		this.cadastra = cadastra;
	}

	@PostMapping
	List<String> cadastraNota(@RequestBody List<Nota> notas) {

		List<String> statusCadastro = new ArrayList<String>();

		notas.forEach(nota -> {

			String statusRegistro = "";

			StatusRegistro status = valida.nota(nota);

			switch (status) {
			case OK:
				cadastra.itens(nota.getItens());

				StatusRegistro statusCliente = valida.cliente(nota.getCliente());

				Cliente cliente = null;

				if (statusCliente == StatusRegistro.PRESENTE_NO_BD) {
					cliente = clienteRepo.findByNomeIgnoreCase(nota.getCliente().getNome());

					nota.setCliente(cliente);

				} else if (statusCliente == StatusRegistro.OK) {
					clienteRepo.save(nota.getCliente());
				}

				notaRepo.save(nota);

				statusRegistro += "nota-n" + nota.getNumero() + "-cliente" + nota.getCliente().getNome() + "-"
						+ nota.getItens().size() + "-qtdItens : ok";
				break;

			case ATRIBUTOS_INVALIDOS:
				statusRegistro += "! : Atributo inválido";
				break;
			case PRESENTE_NO_BD:
				statusRegistro += "nota-n" + nota.getNumero() + "-cliente" + nota.getCliente().getNome() + "-"
						+ nota.getItens().size() + "-qtdItens : Presente no BD";
				break;

			}

			statusCadastro.add(statusRegistro);

		});

		return statusCadastro;
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

	@PutMapping("{id}")
	ResponseEntity<Nota> alteraNota(@PathVariable Integer id, @RequestBody Nota notaAtualizada) {

		Nota notaExistente = notaRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota não encontrada"));

		if (notaAtualizada.getNumero() != 0 && notaAtualizada.getNumero() != null) {
			if (notaExistente.getNumero() != notaAtualizada.getNumero()) {
				notaExistente.setNumero(notaAtualizada.getNumero());
			}
		}

		if (notaAtualizada.getItens().size() != 0) {

			List<Item> itensNtA = notaAtualizada.getItens();
			List<Item> itensValidados = new ArrayList<Item>();

			itensNtA.forEach(item -> {

				if (valida.item(item) == StatusRegistro.OK) {
					itensValidados.add(item);
				}

			});

			if (itensValidados.size() != 0) {
				List<Item> itensCadastradosBD = (List<Item>) itemRepo.saveAll(itensValidados);
				notaExistente.setItens(itensCadastradosBD);
			}
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

		return ResponseEntity.ok(notaRepo.save(notaExistente));
	}

	@DeleteMapping("/{id}")
	Nota deletaNotaPorId(@PathVariable Integer id) {

		Nota nota = notaRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nota não encontrada"));
		Hibernate.initialize(nota.getItens());

		notaRepo.deleteById(id);

		return nota;
	}
}
