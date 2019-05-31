package es.ucm.fdi.iw.model;


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
	private String descripcion;
	private Oferta oferta;
	
	
	public float getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(float valor) {
		this.puntuacion = valor;
	}
	
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
	public Usuario getValorador() {
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

	@ManyToOne(targetEntity=Oferta.class)
	public Oferta getOferta() {
		return oferta;
	}

	public void setOferta(Oferta oferta) {
		this.oferta = oferta;
	}
}
