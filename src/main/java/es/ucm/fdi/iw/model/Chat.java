package es.ucm.fdi.iw.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;


/**
 * FIXME
 * 
 * @author mfreire
 */
@Entity
public class Chat {
	private long id;
	private Usuario cliente;
	private Oferta oferta;
	private String conversacion;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	@ManyToOne(targetEntity=Usuario.class)
	public Usuario getCliente() {
		return cliente;
	}
	public void setCliente(Usuario Cliente) {
		this.cliente = Cliente;
	}
	@ManyToOne(targetEntity=Oferta.class)
	public Oferta getOferta() {
		return oferta;
	}
	public void setOferta(Oferta Oferta) {
		this.oferta = Oferta;
	}
	@Column(length=16000)
	public String getConversacion() {
		return conversacion;
	}
	public void setConversacion(String conversacion) {
		this.conversacion = conversacion;
	}
	
	
}
