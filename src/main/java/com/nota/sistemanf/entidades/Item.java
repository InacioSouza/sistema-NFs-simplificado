package com.nota.sistemanf.entidades;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "itens")
public class Item {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@OneToOne
	private Produto produto;
	private Integer quantidade = 0;
	private BigDecimal valorTotal = BigDecimal.ZERO;

	@ManyToOne
	@JoinColumn(name = "nota_id", nullable = false)
	@JsonIgnore
	private Nota nota;

	public Item() {
		this.valorTotal = atualizaValorTotal();
	}

	public Item(Produto p, Integer quantidade) {

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

	public Nota getNota() {
		return nota;
	}

	public void setNota(Nota nota) {
		this.nota = nota;
	}

	@Override
	public String toString() {
		return "Item [id=" + id + ", produto=" + produto.getNome() + ", quantidade=" + quantidade + ", valorTotal="
				+ valorTotal + ", nota=" + nota + "]";
	}

}
