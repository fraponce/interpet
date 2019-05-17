package es.ucm.fdi.iw.control;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.ucm.fdi.iw.model.Usuario;

@RestController
@RequestMapping("chat")
public class ApiController {
	
	private static final Logger log = LogManager.getLogger(ApiController.class);
	
	@Autowired 
	private EntityManager entityManager;	
	
	@Autowired
	private IwSocketHandler iwSocketHandler;

	@GetMapping("/{id}")
	@ResponseBody
	public String mensajesViejos( HttpSession session, @PathVariable long id) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		// verifico que este usuario tiene voz en el chat con ese id
		// acaba de conectarse un usuario:
		// devuelvele, en texto plano, lo que tiene que mostrar para empezar
		return "{\"text\" : \"mensajes viejos\"}";
	}
	
	@PostMapping("/{id}")
	@Transactional
	@ResponseBody
	public String enviarMensaje(HttpSession session, @PathVariable long id, String texto) {
		// verifico que este usuario tiene voz en el chat con ese id
		// añado el texto al chat activo
		// aviso al interlocutor, si está conectado, vía
		String elOtroDelChat = "a";
		iwSocketHandler.sendText(elOtroDelChat, texto);		
		return "ok";
	}
}
