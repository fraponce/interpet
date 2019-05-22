package es.ucm.fdi.iw.control;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.sql.Date;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Chat;
import es.ucm.fdi.iw.model.Denuncia;
import es.ucm.fdi.iw.model.Mascota;
import es.ucm.fdi.iw.model.Oferta;
import es.ucm.fdi.iw.model.Usuario;
import es.ucm.fdi.iw.model.Valoracion;

@Controller
public class RootController {
	
	private static final Logger log = LogManager.getLogger(RootController.class);
	
	@Autowired
	private Environment env;
	
	@Autowired 
	private EntityManager entityManager;	
	
	@Autowired
	private IwSocketHandler iwSocketHandler;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	

	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("xs", "uno dos tres cuatro cinco".split(" "));
		return "indice";
	}
		
	@GetMapping("/chat")
	public String chat(HttpSession session, Model model, @RequestParam long id) {
		// importante: peticion tiene que pasarnos, y chat tiene que tener, la id del chat de alguna forma
		return "chat";
	}
		
	@GetMapping("/peticion")
	public String peticion(HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		return "peticion";
	}
	@GetMapping("/baneado")
	public String baneado() {		
		return "baneado";
	}
	
	@GetMapping("/indice")
	public String indice(HttpSession session) {
		try {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		return "indice";
		}catch (NullPointerException e) {
			return "indice";
		}
	}
	
	@GetMapping("/registro")
	public String registro() {
		return "registro";
	}
	
	@PostMapping("/registro")
	@Transactional
	public String procesarregistro(Model model, HttpSession session,   
			@RequestParam String nombreUsuario,
			@RequestParam String password) {
		Usuario u = new Usuario();
		
		u.setAbierto(true);
		u.setDescripcion("");
		u.setMascotas(null);
		u.setPassword(passwordEncoder.encode(password));
		u.setRoles("USER");
		u.setLogin(nombreUsuario);
		entityManager.persist(u);
		return "indice";
	}
	
	@GetMapping("/perfil")
	public String perfil(Model model, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		model.addAttribute("conectado", u);
		log.info("Usuario es {}",  u.getLogin());
		model.addAttribute("trabajos", entityManager
				.createQuery("SELECT o FROM Oferta o WHERE o.elegido.id = :userId")
				.setParameter("userId", u.getId())
				.getResultList());
		model.addAttribute("valoracion", entityManager
				.createQuery("SELECT AVG(o.puntuacion) FROM Valoracion o WHERE o.premiado.id = :userId")
//				.createQuery("SELECT NVL(AVG(puntuacion),0) \"v\" FROM Valoracion o WHERE o.premiado.id = :userId")
				.setParameter("userId", u.getId())
				.getResultList());
		return "perfil";
	}
	
	
	@GetMapping("/ofertas")
	public String ofertas(Model model, HttpSession session) {
		try{
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		addOfertasToModel(model, u);
		return "ofertas";
		}catch (NullPointerException e) {
			model.addAttribute("ofertas", entityManager
					.createQuery("SELECT o FROM Oferta o WHERE o.enabled = true")
					.getResultList());;
			return "ofertas";
			
		}
	}
	
	@PostMapping("/ofertas/{idOferta}")
	@Transactional
	public String ofertas(Model model, HttpSession session,   
			 @PathVariable long idOferta) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class, u.getId());
		if (!u.getAbierto()) {
			return "baneado";
		}
		
		Oferta o = (Oferta)entityManager.find(Oferta.class, idOferta);
		Chat previo = getChat(o.getId(), u.getId());
		if (previo == null) {
			Chat c = new Chat();
			c.setCliente(u);
			c.setOferta(o);
			c.setConversacion("cuidado una cabra salida");
			entityManager.persist(c);
			entityManager.flush();
			addChatsToModel(model, u);
			return "listachats";
		} else {
			addChatsToModel(model, u);
			return "listachats";
		}
	}
	
	protected void addOfertasToModel(Model model, Usuario u) { /* Permite usar las queries en una redirección */
		log.info("Usuario en ListaChats es {}",  u.getLogin());
		model.addAttribute("ofertas", entityManager
				.createQuery("SELECT o FROM Oferta o WHERE o.enabled = true and o.solicitante.id <> :userId")
				.setParameter("userId", u.getId())
				.getResultList());
	}
	
	@SuppressWarnings("unchecked")
	public Chat getChat(long idOferta, long idUsuario) { /*Metodo para devolver solo uno o NULL*/
		List<Chat> chats = entityManager.createQuery("SELECT o FROM Chat o WHERE o.oferta.enabled = true and o.oferta.id = :ofertaId and o.cliente.id = :userId")
				.setParameter("ofertaId", idOferta)
				.setParameter("userId", idUsuario)
				.getResultList();
		if (chats.size() > 1) {
			throw new IllegalArgumentException("Dos chats para el mismo!!!");
		}
		return chats.isEmpty() ? null : chats.get(0);
	}
	
	@GetMapping("/crearofertas")
	public String crearofertas(Model model, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		model.addAttribute("conectado", u);
		return "crearofertas";
	}
	
	private java.sql.Date isoDateStringToSqlDate(String iso) throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date parsed = format.parse(iso);
        java.sql.Date sql = new java.sql.Date(parsed.getTime());
		return sql;
	}
	
	@PostMapping("/crearofertas")
	@Transactional
	public String procesarofertas(Model model, HttpSession session, 
			@RequestParam String mascotaid, 
			@RequestParam String zona, 
			@RequestParam String fechaI, 
			@RequestParam String fechaF,
			@RequestParam BigDecimal precio,
			@RequestParam String nombreTrabajo) {
		
		
		Oferta f = new Oferta();
		f.setMascota((Mascota)entityManager.find(Mascota.class, Long.parseLong(mascotaid)));
		try {
			f.setInicio(isoDateStringToSqlDate(fechaI));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		f.setZona(zona);
		try {
			f.setFin(isoDateStringToSqlDate(fechaF));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		f.setEnabled(true);
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		List<Oferta> fs = u.getOfrecidas();
		f.setSolicitante(u);
		f.setPrecio(precio);
		f.setNombre(nombreTrabajo);
		fs.add(f);
		u.setOfrecidas(fs);
		entityManager.persist(f);
		
		return misofertas(model, session);
	}
	
	@PostMapping("/crearMascota")
	@Transactional
	public String procesarmascota(Model model, HttpSession session, @RequestParam String nombre, 
			@RequestParam String especie, 
			@RequestParam String raza, 
			@RequestParam String descripcion) {
		Mascota m = new Mascota();
		m.setNombre(nombre);
		m.setEspecie(especie);
		m.setRaza(raza);
		m.setDescripcion(descripcion);
		Usuario u = (Usuario)session.getAttribute("u");
		if(!u.getAbierto()) {return "baneado";}
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		List<Mascota> ms = u.getMascotas();
		m.setPropietario(u);
		ms.add(m);
		u.setMascotas(ms);
		entityManager.persist(m);
		
		return perfil(model, session);
	}
	
	@GetMapping("/misofertas")
	public String misofertas(Model model, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		log.info("Usuario es {}",  u.getLogin());
		model.addAttribute("misofertas", entityManager
				.createQuery("SELECT o FROM Oferta o WHERE o.solicitante.id = :userId")
				.setParameter("userId", u.getId())
				.getResultList());
		return "misofertas";
	}
	
	protected void addChatsToModel(Model model, Usuario u) { /* Permite usar las queries en una redirección */
		log.info("Usuario en ListaChats es {}",  u.getLogin());
		model.addAttribute("poseedorchats", entityManager
				.createQuery("SELECT o FROM Chat o WHERE o.oferta.solicitante.id = :userIda and o.oferta.enabled = true")
				.setParameter("userIda", u.getId())
				.getResultList());
		model.addAttribute("aspirantechats", entityManager
				.createQuery("SELECT o FROM Chat o WHERE o.cliente.id = :userId and o.oferta.enabled = true")
				.setParameter("userId", u.getId())
				.getResultList());
	}
	
	@GetMapping("/listachats")
	public String listachats(Model model, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		addChatsToModel(model, u); /*Le pasamos las querys de listachats*/
		return "listachats";
	}
	
	@PostMapping("/listachats/{id}")
	@Transactional
	@ResponseBody
	public String listachats(Model model, HttpSession session,   
			@PathVariable long id) {
		Chat c = (Chat)entityManager.find(Chat.class, id);
		Oferta o = (Oferta)entityManager.find(Oferta.class, c.getOferta().getId());
		Usuario u = (Usuario)entityManager.find(Usuario.class, c.getCliente().getId());
		o.setEnabled(false);
		o.setElegido(u);
		entityManager.persist(o);
		return "exaltao";
	}
	
	@GetMapping("/creardenuncia")
	public String creardenuncia(Model model, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		model.addAttribute("conectado", u);
		return "creardenuncia";
	}
	
	@PostMapping("/creardenuncia")
	@Transactional
	public String procesarofertas(Model model, HttpSession session,   
			@RequestParam Long ofertaId,
			@RequestParam String razonInci) {
		
		Usuario u = (Usuario)session.getAttribute("u");
		Denuncia d = new Denuncia();
		d.setDenunciante(u);
		Usuario den;
		
		Oferta oferta = (Oferta)entityManager.find(Oferta.class, ofertaId);
		if(oferta.getElegido() != u)
			den = oferta.getElegido();
		else 
			den = oferta.getSolicitante();
		
		d.setDenunciado(den);
		d.setOferta(oferta);
		d.setDescripcion(razonInci);
		d.setEnabled(true);
		entityManager.persist(d);
		return "indice";
	}
	
	@GetMapping("/segar")
	public String segar(Model model, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		if(!u.getRoles().contains("ADMIN")) {
			return "indice";
		}
		log.info("Usuario es {}",  u.getLogin());
		model.addAttribute("denuncias", entityManager
				.createQuery("SELECT d FROM Denuncia d where (select abierto from Usuario u where u.id = d.denunciado ) = true")
				.getResultList());
		return "segar";
	}
	
	@PostMapping("/segar/{id}")
	@Transactional
	@ResponseBody
	public String procesardenuncias(Model model, HttpSession session,   
			@PathVariable long id) {
		
		Usuario u = (Usuario)entityManager.find(Usuario.class, id);
		u.setAbierto(false);
		entityManager.persist(u);
		return "eliminao";
	}
	
	
	
	
	
	@GetMapping("/valorar")
	public String valorar(Model model, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		log.info("Usuario es {}",  u.getLogin());
		model.addAttribute("valorar", entityManager
				.createQuery("SELECT o FROM Oferta o WHERE ((o.solicitante.id = :userId and o.solicitanteV = false) or (o.elegido = :userId and o.elegidoV = false)) and o.enabled = false")
				.setParameter("userId", u.getId())
				.getResultList());
		return "valorar";
	}
	
	@GetMapping("/valorando/{id}")
	public String valorando(Model model, @PathVariable long id, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		Oferta o = (Oferta)entityManager.find(Oferta.class,  id);
		model.addAttribute("conectado", u);
		model.addAttribute("oferta", o);
		return "valorando";
	}
	
	@PostMapping("/valorando/{id}")
	@Transactional
	public String valorando(Model model, HttpSession session,
			@PathVariable long id,
			@RequestParam float valor,
			@RequestParam String descripcion) {
		
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		Valoracion v = new Valoracion();
		v.setValorador(u);
		Usuario val;
		
		Oferta oferta = (Oferta)entityManager.find(Oferta.class, id);
		if(oferta.getElegido().getId() != u.getId() 
				&& oferta.getSolicitante().getId() == u.getId()) {
			val = oferta.getElegido();
			oferta.setSolicitanteV(true);
		} else if (oferta.getElegido().getId() == u.getId() && oferta.getSolicitante().getId() != u.getId()) {
			val = oferta.getSolicitante();
			oferta.setElegidoV(true);
		} else {
			return "indice";
		} 
		
		v.setPremiado(val);
		v.setPuntuacion(valor);
		v.setDescripcion(descripcion);
		entityManager.persist(v);
		return valorar(model, session);
	}
}
