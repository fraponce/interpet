package es.ucm.fdi.iw.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
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
@NamedQueries({
	@NamedQuery(name="Usuario.ByLogin",
			query="SELECT u FROM Usuario u "
					+ "WHERE u.login = :userLogin"),
	@NamedQuery(name="Usuario.HasLogin",
	query="SELECT COUNT(u) "
			+ "FROM Usuario u "
			+ "WHERE u.login = :userLogin")	
})
public class Usuario {
	private long id;
	private String login;
	private String password;
	private String descripcion;
	private String roles; // split by ',' to separate roles
	private boolean abierto;
	
	// application-specific fields
	private List<Mascota> mascotas; 
	private List<Oferta> ofrecidas;
	private List<Valoracion> valoraciones;
	private List<Chat> chats;
	private List<Oferta> trabajos;
	private List<Denuncia> enviadas; //lista de denuncias realizadas.
	private List<Denuncia> recibidas; //lista de denuncias recibidas.
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public long getId() {
	return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}	

	@Column(unique=true)
	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public boolean getAbierto() {
		return abierto;
	}

	public void setAbierto(boolean abierto) {
		this.abierto = abierto;
	}
	
	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	@OneToMany(targetEntity=Mascota.class)
	@JoinColumn(name="propietario_id")
	public List<Mascota> getMascotas() {
		return mascotas;
	}

	public void setMascotas(List<Mascota> mascotas) {
		this.mascotas = mascotas;
	}
	
	@OneToMany(targetEntity=Valoracion.class)
	@JoinColumn(name="premiado_id")
	public List<Valoracion> getValoraciones() {
		return valoraciones;
	}

	public void setValoraciones(List<Valoracion> mascotas) {
		this.valoraciones = mascotas;
	}

	@OneToMany(targetEntity=Oferta.class)
	@JoinColumn(name="solicitante_id")
	public List<Oferta> getOfrecidas() {
		return ofrecidas;
	}

	public void setOfrecidas(List<Oferta> ofertas) {
		this.ofrecidas = ofertas;
	}	
	
	@OneToMany(targetEntity=Chat.class)
	@JoinColumn(name="cliente_id")
	public List<Chat> getChats() {
		return chats;
	}
	public void setChats(List<Chat> chats) {
		this.chats = chats;
	}
	@OneToMany(targetEntity= Oferta.class)
	@JoinColumn(name = "elegido_id")
	public List<Oferta> getTrabajos() {
		return trabajos;
	}

	public void setTrabajos(List<Oferta> trabajos) {
		this.trabajos = trabajos;
	}
	@OneToMany(targetEntity= Denuncia.class)
	@JoinColumn(name = "denunciante_id")
	public List<Denuncia> getDenuncias() {
		return enviadas;
	}

	public void setDenuncias(List<Denuncia> denuncias) {
		this.enviadas = denuncias;
	}
	@OneToMany(targetEntity= Denuncia.class)
	@JoinColumn(name = "denunciado_id")
	public List<Denuncia> getFaltas() {
		return recibidas;
	}

	public void setFaltas(List<Denuncia> faltas) {
		this.recibidas = faltas;
	}
}
