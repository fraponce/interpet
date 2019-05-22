package es.ucm.fdi.iw;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import es.ucm.fdi.iw.control.RootController;
import es.ucm.fdi.iw.model.Chat;
import es.ucm.fdi.iw.model.Denuncia;
import es.ucm.fdi.iw.model.Mascota;
import es.ucm.fdi.iw.model.Oferta;
import es.ucm.fdi.iw.model.Usuario;

/**
 * This code will execute when the application first starts.
 * 
 * @author mfreire
 */
@Component
public class StartupConfig {
	
	private static final Logger log = LogManager.getLogger(RootController.class);
	
	@Autowired
	private Environment env;

	@Autowired
	private ServletContext context;
	
    @Autowired
    private EntityManager entityManager;    
    	
	@Autowired
	private PasswordEncoder passwordEncoder;		  
    
	public class SqlDateFormatter {
		public String format(Date date) {
			if (date == null) return "???";
			LocalDate localDate = date.toLocalDate();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
			return localDate.format(formatter);		}
	}
	
	@EventListener(ContextRefreshedEvent.class)
	@Transactional
	public void contextRefreshedEvent() {
		String debugProperty = env.getProperty("es.ucm.fdi.debug");
		context.setAttribute("debug", debugProperty != null 
				&& Boolean.parseBoolean(debugProperty.toLowerCase()));
		log.info("Setting global debug property to {}", 
				context.getAttribute("debug"));
		
		// see http://www.ecma-international.org/ecma-262/5.1/#sec-15.9.1.15
		// and https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html
		context.setAttribute("dateFormatter", new SqlDateFormatter());
		
		if (context.getAttribute("debug").toString().equalsIgnoreCase("true")) {
			
			Usuario u = new Usuario();
			u.setAbierto(true);
			u.setDescripcion("añsdjfalsdf");
			u.setMascotas(null);
			u.setPassword(passwordEncoder.encode("u"));
			u.setRoles("USER");
			u.setLogin("u");
			entityManager.persist(u);
			Usuario d = new Usuario();
			d.setAbierto(true);
			d.setDescripcion("añsdjfalsdf");
			d.setMascotas(null);
			d.setPassword(passwordEncoder.encode("d"));
			d.setRoles("USER");
			d.setLogin("d");
			entityManager.persist(d);
			Usuario admin = new Usuario();
			admin.setAbierto(true);
			admin.setDescripcion("manda más");
			admin.setMascotas(null);
			admin.setPassword(passwordEncoder.encode("a"));
			admin.setRoles("ADMIN");
			admin.setLogin("a");
			entityManager.persist(admin);
			Mascota m = new Mascota();
			m.setEspecie("Bicho");
			m.setNombre("Arf");
			m.setPropietario(u);
			m.setRaza("Dinosaurio ninja zombie nasi");
			m.setDescripcion("En primer lugar, háganos saber diez");
			entityManager.persist(m);
			Mascota m2 = new Mascota();
			m2.setEspecie("Roca");
			m2.setNombre("Si llevo la contraria");
			m2.setPropietario(u);
			m2.setRaza("Dinosaurio ninja zombie comunista (que listillo)");
			m2.setDescripcion("...xz");
			entityManager.persist(m2);
			Mascota m3 = new Mascota();
			m3.setEspecie("Tortuga");
			m3.setNombre("Donattelo");
			m3.setPropietario(admin);
			m3.setRaza("teenage mutant ninja turtle");
			m3.setDescripcion("en serio...");
			entityManager.persist(m3);
			Mascota m4 = new Mascota();
			m4.setEspecie("Llama");
			m4.setNombre("Cuzco");
			m4.setPropietario(u);
			m4.setRaza("Emperador");
			m4.setDescripcion("Y sus locuras con Pacha");
			entityManager.persist(m4);
			Oferta o = new Oferta();
			o.setSolicitante(admin);
			o.setNombre("Traer pizza a mi mascota");
			o.setPrecio(BigDecimal.valueOf(15));
			o.setEnabled(false);
			o.setZona("Alcantarillas");
			o.setElegido(u);
			o.setMascota(m3);
			o.setDescripcion("aviso a navegantes: tortuga enorme");
			entityManager.persist(o);
			Oferta o2 = new Oferta();
			o2.setSolicitante(u);
			o2.setNombre("Acompañar a Arf a las reuniones de las juventudes");
			o2.setPrecio(BigDecimal.valueOf(15));
			o2.setEnabled(false);
			o2.setZona("ALEMANIA");
			o2.setElegido(admin);
			o2.setMascota(m);
			o2.setDescripcion("cuidado con las ideas progresistas");
			entityManager.persist(o2);
			Oferta o3 = new Oferta();
			o3.setSolicitante(u);
			o3.setNombre("Conseguir poción humanizadora");
			o3.setPrecio(BigDecimal.valueOf(15));
			o3.setEnabled(false);
			o3.setZona("Pirámide Maya");
			o3.setElegido(d);
			o3.setMascota(m4);
			o3.setDescripcion("Llama parlante");
			entityManager.persist(o3);
			Oferta o4 = new Oferta();
			o4.setSolicitante(u);
			o4.setNombre("asdf");
			o4.setPrecio(BigDecimal.valueOf(15));
			o4.setEnabled(true);
			o4.setZona("asdf");
			o4.setMascota(m4);
			o4.setDescripcion("asdf");
			entityManager.persist(o4);
			Oferta o5 = new Oferta();
			o5.setSolicitante(admin);
			o5.setNombre("ñlkj");
			o5.setPrecio(BigDecimal.valueOf(15));
			o5.setEnabled(true);
			o5.setZona("ñlkj");
			o5.setMascota(m);
			o5.setDescripcion("ñlkj");
			entityManager.persist(o5);
			Denuncia d1= new Denuncia();
			d1.setDenunciado(d);
			d1.setDenunciante(u);
			d1.setDescripcion("Ese wey profanó el honor de mi llama y ya no la puedo casar");
			d1.setOferta(o3);
			entityManager.persist(d1);
			Chat c = new Chat();
			c.setCliente(admin);
			c.setOferta(o4);
			c.setConversacion("Por aqui andam... nos ataca un robot tiranosauros Rex salvese quien pueda...");
		}
	}
}