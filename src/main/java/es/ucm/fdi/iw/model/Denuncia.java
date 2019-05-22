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
 * A user.
 * 
 * Note that, in this particular application, we will automatically be creating
 * users for students. Those users will have the group password as their "password", 
 * but will be generally unable to actually log in without the group password.  
 * 
 * @author mfreire
 */
@Entity
public class Denuncia {
	private long id;
	private Usuario denunciante;
	private Usuario denunciado;
	private Oferta oferta;
	private String descripcion;
	private boolean enabled;

	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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
	public Usuario getDenunciante() {
		return denunciante;
	}

	public void setDenunciante(Usuario Denunciante) {
		this.denunciante = Denunciante;
	}
	@ManyToOne(targetEntity=Usuario.class)
	public Usuario getDenunciado() {
		return denunciado;
	}
	
	public void setDenunciado(Usuario Denunciado) {
		this.denunciado = Denunciado;
	}
	@ManyToOne(targetEntity=Oferta.class)
	public Oferta getOferta() {
		return oferta;
	}
	
	public void setOferta(Oferta Oferta) {
		this.oferta = Oferta;
	}
	
	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}
