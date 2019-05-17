package es.ucm.fdi.iw.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@NamedQueries({
	@NamedQuery(name="Oferta.ByLogin",
			query="SELECT u FROM Usuario u "
					+ "WHERE u.login = :userLogin"),
})
public class Oferta {
	private long id;
	private String nombre;
	private Usuario solicitante;
	private boolean solicitanteV;
	private Mascota mascota;
	private Usuario elegido;
	private boolean elegidoV;
	private BigDecimal precio;
	private String zona;
	private Date inicio;
	private Date fin;
	private boolean enabled;
	private String descripcion;
	
	// application-specific fields
	private List<Chat> chats;
	private List<Denuncia> denuncias;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	public long getId() {
	return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}	
	@ManyToOne(targetEntity=Usuario.class)
	public Usuario getSolicitante() {
		return solicitante;
	}

	public void setSolicitante(Usuario login) {
		this.solicitante = login;
	}
	@ManyToOne(targetEntity=Mascota.class)
	public Mascota getMascota() {
		return mascota;
	}
	
	public void setMascota(Mascota Mascota) {
		this.mascota = Mascota;
	}
	@ManyToOne(targetEntity=Usuario.class)
	public Usuario getElegido() {
		return elegido;
	}

	public void setElegido(Usuario Elegido) {
		this.elegido = Elegido;
	}
	
	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public String getZona() {
		return zona;
	}

	public void setZona(String zona) {
		this.zona = zona;
	}
	
	public Date getInicio() {
		return inicio;
	}

	public void setInicio(Date inicio) {
		this.inicio = inicio;
	}
	
	public Date getFin() {
		return fin;
	}

	public void setFin(Date fin) {
		this.fin = fin;
	}
	
	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean getSolicitanteV() {
		return solicitanteV;
	}

	public void setSolicitanteV(boolean enabled) {
		this.solicitanteV = enabled;
	}
	
	public boolean getElegidoV() {
		return elegidoV;
	}

	public void setElegidoV(boolean enabled) {
		this.elegidoV = enabled;
	}

	@OneToMany(targetEntity=Chat.class)
	@JoinColumn(name="oferta_id")
	public List<Chat> getChats() {
		return chats;
	}

	public void setChats(List<Chat> questions) {
		this.chats = questions;
	}	
	
	@OneToMany(targetEntity=Denuncia.class)
	@JoinColumn(name="oferta_id")
	public List<Denuncia> getDenuncias() {
		return denuncias;
	}
	public void setDenuncias(List<Denuncia> denuncias) {
		this.denuncias = denuncias;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

}
