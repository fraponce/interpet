package es.ucm.fdi.iw.control;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import es.ucm.fdi.iw.model.Chat;
import es.ucm.fdi.iw.model.Usuario;

@RestController
@RequestMapping("api")
public class ApiController {
	
	private static final Logger log = LogManager.getLogger(ApiController.class);
	
	@Autowired 
	private EntityManager entityManager;	
	
	@Autowired
	private IwSocketHandler iwSocketHandler;

	@GetMapping("/{id}")
	@ResponseBody
	public String mensajesViejos( HttpSession session, @PathVariable long id, Model model ) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		Chat c = (Chat)entityManager.find(Chat.class,  id);
		// verifico que este usuario tiene voz en el chat con ese id
		if(c.getCliente().getId() != u.getId() && c.getOferta().getSolicitante().getId() != u.getId()) {
			return "tu madre";
		}
		// acaba de conectarse un usuario:
		// devuelvele, en texto plano, lo que tiene que mostrar para empezar
		model.addAttribute("chate", c);
		return c.getConversacion();
		//return "{\"text\" : \""+ c.getConversacion() +"\"}";
		
	}
	
	private static final class LineaTexto {
		public String texto;
	}
	
	@PostMapping("/{id}")
	@Transactional
	@ResponseBody
	public String enviarMensaje(HttpSession session, @PathVariable long id, @RequestBody LineaTexto texto) {
		Usuario u = (Usuario)session.getAttribute("u");
		u = (Usuario)entityManager.find(Usuario.class,  u.getId());
		if(!u.getAbierto()) {return "baneado";}
		Chat c = (Chat)entityManager.find(Chat.class,  id);
		// verifico que este usuario tiene voz en el chat con ese id
		boolean cliente = c.getCliente().getId() == u.getId();
		boolean solicitante = c.getOferta().getSolicitante().getId() == u.getId();
		if(!cliente && !solicitante) {
			return "tu madre";
		}
		
		// añado el texto al chat activo
		c.setConversacion(c.getConversacion() +"\n"+ u.getLogin() +": "+ texto.texto);
		// aviso al interlocutor, si está conectado, vía
		String elOtroDelChat;
		if(cliente) {
			elOtroDelChat = c.getOferta().getSolicitante().getLogin();
		}else {
			elOtroDelChat = c.getCliente().getLogin();
		}
		if(c.getOferta().getEnabled()) {
			String mensaje = u.getLogin() + ": " + texto.texto;
			log.info("trying to talk to {}, saying {}",  elOtroDelChat,  mensaje);
			iwSocketHandler.sendText(elOtroDelChat, mensaje);		
			iwSocketHandler.sendText(u.getLogin(), mensaje);
		}
		return "ok";
	}
	
	@PostMapping("/fin/{id}")
	@Transactional
	@ResponseBody
	public void terminarMensaje(HttpSession session, @PathVariable long id) {
		LineaTexto fin = new LineaTexto();
		fin.texto = "Oferta finalizada";
		enviarMensaje(session, id, fin);
	}
}
