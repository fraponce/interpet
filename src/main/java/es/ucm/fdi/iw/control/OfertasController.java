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
public class OfertasController {
	
	private static final Logger log = LogManager.getLogger(OfertasController.class);
	
	@Autowired
	private Environment env;
	
	@Autowired 
	private EntityManager entityManager;	
	
	@Autowired
	private IwSocketHandler iwSocketHandler;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	

	
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
			c.setConversacion(u.getLogin() + " está interesado en " + o.getNombre());
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
	
	@GetMapping("oferta/{id}")
	public String oferta(Model model, @PathVariable long id, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		iwSocketHandler.sendText("a", "acaba de entrar " + u.getLogin());
		model.addAttribute("o", entityManager.find(Oferta.class,  id));
		return "oferta";
	}
}
