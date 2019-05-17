package es.ucm.fdi.iw.model;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Valoracion {
	private long id;
	private Usuario premiado;
	private Usuario valorador;
	private float puntuacion;
	public float getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(float valor) {
		this.puntuacion = valor;
	}

	private String descripcion;

	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public long getId() {
	return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}	
	@ManyToOne(targetEntity=Usuario.class)
	public Usuario getPremiado() {
		return premiado;
	}

	public void setPremiado(Usuario idDenunciante) {
		this.premiado = idDenunciante;
	}
	@ManyToOne(targetEntity=Usuario.class)
	public Usuario getvalorador() {
		return valorador;
	}
	
	public void setValorador(Usuario idDenunciado) {
		this.valorador = idDenunciado;
	}
	
	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}
