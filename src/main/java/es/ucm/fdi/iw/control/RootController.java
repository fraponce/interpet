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
	
	public boolean checkBaneado(HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		return !u.getAbierto();
	}
	
	@GetMapping("/")
	public String index(Model model) {
		model.addAttribute("xs", "uno dos tres cuatro cinco".split(" "));
		return "indice";
	}
		
	@GetMapping("/chat/{id}")
	public String chat(HttpSession session, Model model, @PathVariable long id) {
		// importante: peticion tiene que pasarnos, y chat tiene que tener, la id del chat de alguna forma
		model.addAttribute("chatId", ""+id);
		Chat c = (Chat)entityManager.find(Chat.class, id);
		if(!c.getOferta().getEnabled()) {
			return "listachats";
		}
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		model.addAttribute("yo", u.getLogin());
		if(u.getId()== c.getCliente().getId()) {
			model.addAttribute("cond", false);
			model.addAttribute("tu", c.getOferta().getSolicitante());
		}else{
			model.addAttribute("cond", true);
			model.addAttribute("tu", c.getCliente());
		}
		return "chat";
	}
	
	@PostMapping("/chat/{id}")
	@Transactional
	public String chats(Model model, HttpSession session,   
			@PathVariable long id) {
		Chat c = (Chat)entityManager.find(Chat.class, id);
		Oferta o = (Oferta)entityManager.find(Oferta.class, c.getOferta().getId());
		Usuario u = (Usuario)entityManager.find(Usuario.class, c.getCliente().getId());
		o.setEnabled(false);
		o.setElegido(u);
		entityManager.persist(o);
		return "listachats";
	}
	
		
	@GetMapping("/peticion")
	public String peticion(HttpSession session) {
		if(checkBaneado(session)) {return "baneado";}
		return "peticion";
	}
	@GetMapping("/baneado")
	public String baneado() {		
		return "baneado";
	}
	
	@GetMapping("/indice")
	public String indice(HttpSession session) {
		try {
		if(checkBaneado(session)) {return "baneado";}
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
		model.addAttribute("vars", entityManager
				.createQuery("SELECT v FROM Valoracion v WHERE v.premiado.id = :userId")
				.setParameter("userId", u.getId())
				.getResultList());
		return "perfil";
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
	
	protected void addChatsToModel(Model model, Usuario u) { //Permite usar las queries en una redirección
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
		addChatsToModel(model, u); //Le pasamos las querys de listachats
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
}
