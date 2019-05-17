function muestraDialogoCreacionMascota(mostrar) {
	const f = document.getElementById("formularioCrearMascota");
	f.style = mostrar ? "display: block;" : "display: none;";
	return mostrar;
}
function botonDialogoCreacionMascota() {
	const b = document.getElementById("muestraFormularioCrearMascota");
	muestraDialogoCreacionMascota(false);
	let visible = false;
	b.onclick = () => {
		visible = muestraDialogoCreacionMascota(!visible);
		b.innerText = (visible ? "Ocultar" : "Mostar") + " formulario para añadir mascota";
	}
}