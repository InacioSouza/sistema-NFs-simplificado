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

import com.nota.sistemanf.entidades.Cliente;
import com.nota.sistemanf.repository.ClienteRepository;
import com.nota.sistemanf.services.StatusRegistro;
import com.nota.sistemanf.services.Valida;

@RestController
@RequestMapping("/snf/clientes")
public class ClienteController {

	Valida valida;
	ClienteRepository clienteRepo;

	public ClienteController(Valida valida, ClienteRepository clienteRepo) {
		this.valida = valida;
		this.clienteRepo = clienteRepo;
	}

	@PostMapping
	public List<String> cadastraCliente(@RequestBody List<Cliente> clientes) {

		List<String> statusCadastro = new ArrayList<String>();

		clientes.forEach(cliente -> {
			String statusRegistro = "";

			StatusRegistro status = valida.cliente(cliente);

			if (status == StatusRegistro.OK) {
				clienteRepo.save(cliente);
				statusRegistro += cliente.getNome() + " : ok";

			} else {
				if (status == StatusRegistro.PRESENTE_NO_BD) {
					statusRegistro += cliente.getNome() + " : presente no BD";

				} else {
					statusRegistro += "null : nao cadastrado";
				}
			}

			statusCadastro.add(statusRegistro);
		});

		return statusCadastro;
	}

	@GetMapping
	public List<Cliente> listaClientes() {
		return (List<Cliente>) clienteRepo.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Cliente> buscarClientePorId(@PathVariable Integer id) {
		Cliente cliente = clienteRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
		return ResponseEntity.ok(cliente);
	}

	@GetMapping("/buscar")
	public ResponseEntity<Cliente> buscarClientesPorNome(@RequestParam String nome) {
		Cliente cliente = clienteRepo.findByNomeIgnoreCase(nome);

		if (cliente == null) {
			return ResponseEntity.noContent().build();
		}

		return ResponseEntity.ok(cliente);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Cliente> atualizaCliente(@PathVariable Integer id, @RequestBody Cliente clienteAtualizado) {

		Cliente clienteExistente = clienteRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));

		if (clienteAtualizado.getNome() != null) {
			clienteExistente.setNome(clienteAtualizado.getNome());

		}

		return ResponseEntity.ok(clienteRepo.save(clienteExistente));
	}

	@DeleteMapping("/{id}")
	public Cliente deletaClientePorId(@PathVariable Integer id) {
		Cliente cliente = clienteRepo.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
		clienteRepo.deleteById(id);

		return cliente;
	}
}
