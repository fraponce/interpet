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
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.model.Denuncia;
import es.ucm.fdi.iw.model.Mascota;
import es.ucm.fdi.iw.model.Oferta;
import es.ucm.fdi.iw.model.Usuario;

@Controller
public class UserController {
	
	private static final Logger log = LogManager.getLogger(UserController.class);
	
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
	
	@GetMapping("usuario/{id}")
	public String usuario(Model model, @PathVariable long id, HttpSession session) {
		Usuario u = (Usuario)entityManager.find(Usuario.class,  id);
		if(checkBaneado(session)) {return "baneado";}
		model.addAttribute("conectado", u);
		model.addAttribute("valoracion", entityManager
				.createQuery("SELECT AVG(o.puntuacion) FROM Valoracion o WHERE o.premiado.id = :userId")
				.setParameter("userId", id)
				.getResultList());
		iwSocketHandler.sendText("a", "acaba de entrar " + u.getLogin());
		
		return "usuario";
	}
	
	/*@GetMapping("oferta/{id}")
	public String oferta(Model model, @PathVariable long id, HttpSession session) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		Oferta o = (Oferta)entityManager.find(Oferta.class,  id);
		iwSocketHandler.sendText("a", "acaba de entrar " + u.getLogin());
		model.addAttribute("o", entityManager.find(Oferta.class,  id));
		return "oferta";
	}*/
	
	@GetMapping("mascota/{id}")
	public String mascota(Model model, @PathVariable long id, HttpSession session) {
		if(checkBaneado(session)) {return "baneado";}
		Mascota m = (Mascota)entityManager.find(Mascota.class,  id);
		log.info("Mascota es {}",  m.getNombre());
		model.addAttribute("mas", entityManager.find(Mascota.class,  id));
		return "mascota";
	}
	
	
}
