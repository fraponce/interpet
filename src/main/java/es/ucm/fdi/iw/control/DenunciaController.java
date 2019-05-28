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
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Denuncia;
import es.ucm.fdi.iw.model.Oferta;
import es.ucm.fdi.iw.model.Usuario;

@Controller
public class DenunciaController {
	
	private static final Logger log = LogManager.getLogger(DenunciaController.class);
	
	
	@Autowired 
	private EntityManager entityManager;	
	

	
	
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

}
