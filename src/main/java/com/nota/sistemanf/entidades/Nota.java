package com.nota.sistemanf.entidades;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "notas")
public class Nota {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private static Integer numero = 0;
	private Date data_emissao = new Date(System.currentTimeMillis());

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private Cliente cliente;

	@OneToMany
	private List<Item> itens = new ArrayList<Item>();

	private BigDecimal valorTotal = new BigDecimal(0);

	public Nota() {
		this.numero++;
	}

	public Nota(Cliente cliente, List<Item> itens) {
		this.cliente = cliente;
		this.itens = itens;

		this.numero++;
		calcValorTotalNota();
	}

	private BigDecimal calcValorTotalNota() {
		BigDecimal valorTot = new BigDecimal(0);

		if (!itens.isEmpty()) {
			this.itens.forEach(item -> valorTot.add(item.getValorTotal()));
		}

		return valorTot;
	}

	public Integer getNumero() {
		return numero;
	}

	public Date getData_emissao() {
		return data_emissao;
	}

	public void setData_emissao(Date data_emissao) {
		this.data_emissao = data_emissao;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<Item> getItens() {
		return itens;
	}

	public void setItens(List<Item> itens) {
		this.itens = itens;

		calcValorTotalNota();
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public Integer getId() {
		return id;
	}

}