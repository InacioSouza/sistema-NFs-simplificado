package com.nota.sistemanf.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nota.sistemanf.entidades.Cliente;
import com.nota.sistemanf.entidades.Item;
import com.nota.sistemanf.entidades.Produto;
import com.nota.sistemanf.repository.ClienteRepository;
import com.nota.sistemanf.repository.ProdutoRepository;

@Service
public class Valida {
	@Autowired
	ClienteRepository clienteRepo;

	@Autowired
	ProdutoRepository produtoRepo;

	public StatusRegistro cliente(Cliente c1) {

		if (c1.getNome() == null) {
			return StatusRegistro.NULL;
		}
		if (clienteRepo.findByNomeIgnoreCase(c1.getNome()) != null) {
			return StatusRegistro.PRESENTE_NO_BD;
		}
		return StatusRegistro.OK;
	}

	public StatusRegistro produto(Produto p) {

		if (p.getNome() == null && p.getPreco() == null) {
			return StatusRegistro.NULL;

		}
		if (p.getNome() == null || p.getPreco() == null) {
			return StatusRegistro.ATRIBUTOS_INVALIDOS;

		}
		if (produtoRepo.findByNomeIgnoreCase(p.getNome()) != null) {
			return StatusRegistro.PRESENTE_NO_BD;

		}
		return StatusRegistro.OK;
	}

	public StatusRegistro item(Item item) {
		StatusRegistro statusProduto = produto(item.getProduto());

		if (item.getQuantidade() == 0 && statusProduto == StatusRegistro.NULL) {
			return StatusRegistro.NULL;

		} else if (item.getQuantidade() == 0) {
			return StatusRegistro.ATRIBUTOS_INVALIDOS;

		} else if (statusProduto == StatusRegistro.ATRIBUTOS_INVALIDOS) {
			return StatusRegistro.DEPENDENCIA_INVALIDA;

		}
		return StatusRegistro.OK;
	}
}
