package es.ucm.fdi.iw.control;


import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.model.Oferta;
import es.ucm.fdi.iw.model.Usuario;
import es.ucm.fdi.iw.model.Valoracion;

@Controller
public class ValoracionController {
	
	private static final Logger log = LogManager.getLogger(ValoracionController.class);

	
	@Autowired 
	private EntityManager entityManager;	


	
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
