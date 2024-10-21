package com.nota.sistemanf.entidades;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "itens")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Item {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer numero;

	@OneToOne
	private Produto produto;
	private Integer quantidade = 0;
	private BigDecimal valorTotal = BigDecimal.ZERO;

	public Item() {
		this.valorTotal = atualizaValorTotal();
	}

	public Item(Integer numero, Produto p, Integer quantidade) {
		this.numero = numero;

		this.produto = p;

		this.quantidade = quantidade;

		this.valorTotal = atualizaValorTotal();
	}

	private BigDecimal atualizaValorTotal() {
		if (produto != null) {
			return this.produto.getPreco().multiply(new BigDecimal(this.quantidade));
		}
		return BigDecimal.ZERO;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto p) {

		this.produto = p;

		this.valorTotal = atualizaValorTotal();
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;

		this.valorTotal = atualizaValorTotal();
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public Integer getId() {
		return id;
	}

}
