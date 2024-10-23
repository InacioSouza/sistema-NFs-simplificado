package com.nota.sistemanf.entidades;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "notas")
public class Nota {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String numero;

	private Date data_emissao = new Date(System.currentTimeMillis());

	@ManyToOne
	private Cliente cliente;

	@OneToMany(mappedBy = "nota")
	private List<Item> itens = new ArrayList<Item>();

	private BigDecimal valorTotal = new BigDecimal(0);

	public Nota() {
	}

	public Nota(Cliente cliente, List<Item> itens, String numero) {
		this.cliente = cliente;
		this.itens = itens;

		this.numero = numero;
		calcValorTotalNota();
	}

	public void calcValorTotalNota() {
		BigDecimal valorTot = BigDecimal.ZERO;

		if (!itens.isEmpty()) {

			for (Item item : itens) {
				valorTot = valorTot.add(item.getValorTotal());
			}
		}

		this.valorTotal = valorTot;

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

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getNumero() {
		return numero;
	}

}
