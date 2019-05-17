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
public class Mascota {
	private long id;
	private Usuario propietario;
	private String nombre;
	private String especie;
	private String raza;
	private String descripcion;
	private List<Oferta> ofertas; 
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public long getId() {
	return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}	


	public String getNombre() {
		return nombre;
	}
	
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEspecie() {
		return especie;
	}

	public void setEspecie(String especie) {
		this.especie = especie;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	@ManyToOne(targetEntity=Usuario.class)
	public Usuario getPropietario() {
		return propietario;
	}

	public void setPropietario(Usuario propietario) {
		this.propietario = propietario;
	}
	
	@OneToMany(targetEntity=Oferta.class)
	@JoinColumn(name="mascota_id")
	public List<Oferta> getOfertas() {
		return ofertas;
	}

	public void setOfertas(List<Oferta> ofertas) {
		this.ofertas = ofertas;
	}

	public String getRaza() {
		return raza;
	}

	public void setRaza(String raza) {
		this.raza = raza;
	}
}
