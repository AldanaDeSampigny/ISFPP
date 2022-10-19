package modelo;
/* hola sofia*/
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import controlador.Controlador;
import net.datastructures.AdjacencyMapGraph;
import net.datastructures.Edge;
import net.datastructures.Graph;
import net.datastructures.GraphAlgorithms;
import net.datastructures.PositionalList;
import net.datastructures.Vertex;


public class Modelo {
	private Graph<Usuario, Relacion> grafo;
	private Map<String, Vertex<Usuario>> vertices;
	private Controlador controlador;

	/**
	 * El constructor crea el grafo y el mapa que se van a necesitar.
	 * 
	 * @param usuarios
	 * @param relaciones
	 */
	public Modelo(Map<String, Usuario> usuarios, List<Relacion> relaciones) {
		this.grafo = new AdjacencyMapGraph<>(false);
		this.vertices = new TreeMap<String, Vertex<Usuario>>();
		for (Entry<String, Usuario> elem : usuarios.entrySet()) {
			Vertex<Usuario> vert = this.grafo.insertVertex(elem.getValue());
			this.vertices.put(elem.getKey(), vert);
		}
		for (Relacion relacion : relaciones)
			if (relacion.getUsuario1() != null && relacion.getUsuario2() != null) {
				String idU1 = relacion.getUsuario1().getId();
				String idU2 = relacion.getUsuario2().getId();
				grafo.insertEdge(this.vertices.get(idU1), this.vertices.get(idU2), relacion);
			}
	}

	private Vertex<Usuario> buscarVerticeUsuario(Usuario usuario) {
		for (Vertex<Usuario> vertice : this.grafo.vertices()) {
			if (vertice.getElement().equals(usuario)) {
				return vertice;
			}
		}
		return null;
	}

	public Vertex<Usuario> buscarVerticeUsuario2(String nombreUsuario) {
		for (Vertex<Usuario> vertice : this.grafo.vertices()) {
			if (vertice.getElement().getNombre().equals(nombreUsuario)) {
				return vertice;
			}
		}
		return null;
	}

	/**
	 * Calcula el promedio de arcos entre cada nodo
	 * 
	 * @return cant de arcos salientes de cada usauario dividido la cantidad de
	 *         vertices totales.
	 */
	public double calcularPromedio() {
		int numVertices = grafo.numVertices();
		int arcosxVert = 0;

		for (Vertex<Usuario> vert : grafo.vertices()) {
			arcosxVert += this.grafo.outDegree(vert);
		}
		if (numVertices == 0)
			return 0;
		return arcosxVert / numVertices;
	}

	/**
	 * Crea un Mao con los usuarios ordenados segun su centralidad, de mayor a
	 * menor.
	 * 
	 * @return el mapa con el valor de la centralidad, como clave y una lista de
	 *         usuarios con esa centralidad.
	 */
	public Map<Integer, List<Usuario>> calcularCentralidad() { 
		Map<Integer, List<Usuario>> usuarios = new TreeMap<Integer, List<Usuario>>(Collections.reverseOrder());
		int result;
		List<Usuario> listaux;
		for (Vertex<Usuario> vert : grafo.vertices()) {
			result = grafo.outDegree(vert);
			listaux = usuarios.get(result);
			if (listaux == null) {
				List<Usuario> lista = new ArrayList<Usuario>();
				lista.add(lista.size(), vert.getElement());
				usuarios.put(result, lista);
			} else {
				listaux.add(listaux.size(), vert.getElement());
			}
		}
		return usuarios;
	}

	/**
	 * Recibe 2 strings que representan a los usuarios y calcula el camino mas corto
	 * entre ambos.
	 * 
	 * @param usuario1
	 * @param usuario2
	 * @return una lista de los usuarios que forman el camino mas corto
	 */
	public List<Usuario> calcularAntiguedad(Usuario usuario1, Usuario usuario2) {
		// Crea en grafo con vertices Usuario y con arcos con el tiempoAmistad como
		// peso.
		Graph<Usuario, Integer> rapido = new AdjacencyMapGraph<>(false);
		Map<Usuario, Vertex<Usuario>> res = new HashMap<>();

		Vertex<Usuario> v1 = this.vertices.get(usuario1.getId());
		Vertex<Usuario> v2 = this.vertices.get(usuario2.getId());

		if (this.grafo.outDegree(v1) == 0 || this.grafo.outDegree(v2) == 0)
			return null;
		for (Vertex<Usuario> result : grafo.vertices())
			res.put(result.getElement(), rapido.insertVertex(result.getElement()));

		Vertex<Usuario>[] vert;

		for (Edge<Relacion> result : grafo.edges()) {
			vert = grafo.endVertices(result);
			rapido.insertEdge(res.get(vert[0].getElement()), res.get(vert[1].getElement()),
					result.getElement().getTiempoAmistad());
		}

		PositionalList<Vertex<Usuario>> lista = GraphAlgorithms.shortestPathList(rapido, res.get(usuario1),
				res.get(usuario2));

		List<Usuario> usuarios = new ArrayList<Usuario>();

		for (Vertex<Usuario> elem : lista) {
			usuarios.add(usuarios.size(), elem.getElement());
		}
		return usuarios; 
	}

	public boolean isAmigo(Usuario usuario, Usuario usuario2) {
		Vertex<Usuario> user = this.buscarVerticeUsuario(usuario);

		for (Edge<Relacion> amigo : grafo.incomingEdges(user)) {
			if (amigo.getElement().getUsuario2().equals(usuario2)) {
				return true;
			}
		}
		return false;
	}

	// sugerir nuevas amistades
	public List<Usuario> sugerencias(Usuario usuario) {
		Vertex<Usuario> usuario1 = this.buscarVerticeUsuario(usuario);
		List<Usuario> listaSugerencias = new ArrayList<Usuario>();

		for (Edge<Relacion> relacion : grafo.incomingEdges(usuario1)) {

			for (Edge<Relacion> amistad : grafo
					.incomingEdges(buscarVerticeUsuario(relacion.getElement().getUsuario2()))) {
				if (!isAmigo(relacion.getElement().getUsuario1(), amistad.getElement().getUsuario2())
						&& amistad.getElement().getCantLikes() > 5 && amistad.getElement().getTiempoAmistad() > 30) {

					if (!amistad.getElement().getUsuario1().equals(usuario)) {
						listaSugerencias.add(0, amistad.getElement().getUsuario1());
					} else {
						listaSugerencias.add(0, amistad.getElement().getUsuario2());
					}
				}
			}
		}
		// para eliminar los repetidos y evitar que se escriba dos veces el mismo amigo
		for (int indice = 0; indice < listaSugerencias.size(); indice++) {
			for (int index = 0; index < listaSugerencias.size() - 1; index++) {
				if (listaSugerencias.get(index).equals(listaSugerencias.get(index + 1))
						|| listaSugerencias.get(index).equals(usuario)) {
					listaSugerencias.remove(index); // .remove(listaSugerencias.get(index));
				}
			}
		}
		return listaSugerencias;
	}

	public List<Usuario> getAmigos(Usuario usuario) {
		List<Usuario> listaAmistades = new ArrayList<Usuario>();
		Vertex<Usuario> verticeUsuario = this.buscarVerticeUsuario(usuario);

		if (usuario != null) {
			for (Edge<Relacion> arco : grafo.incomingEdges(verticeUsuario)) {
				if (!arco.getElement().getUsuario1().equals(usuario)) {
					listaAmistades.add(0, arco.getElement().getUsuario1());
				} else {
					listaAmistades.add(0, arco.getElement().getUsuario2());
				}
			}
		}
		return listaAmistades;
	}
	
	public TreeMap<Integer, Usuario> tiempoAmistadYCantAmigos(Usuario usuario){
		TreeMap<Integer, Usuario> amigos = new TreeMap<Integer, Usuario>();
		Vertex<Usuario> verticeUsuario = this.buscarVerticeUsuario(usuario);
		
		if (usuario != null) {
			for (Edge<Relacion> arco : grafo.incomingEdges(verticeUsuario)) {
				if (!arco.getElement().getUsuario1().equals(usuario)) {
					amigos.put(arco.getElement().getTiempoAmistad(), arco.getElement().getUsuario1());
				} else {
					amigos.put(arco.getElement().getTiempoAmistad(), arco.getElement().getUsuario2());
				}
			}
		}
		return amigos;
	}

	public TreeMap<Integer, Usuario> usuariosMasDensos() {
		TreeMap<Integer, Usuario> usuariosMasDensos = new TreeMap<Integer, Usuario>(Collections.reverseOrder());

		for (Vertex<Usuario> verticeUsuario : grafo.vertices()) {
			int suma = 0;
			for (Edge<Relacion> edgeRelacion : grafo.incomingEdges(verticeUsuario)) {
				suma += edgeRelacion.getElement().getInteraccDiaria();
			}

			usuariosMasDensos.put(suma, verticeUsuario.getElement());
		}

		return usuariosMasDensos;
	}

	public ArrayList<Usuario> getUsuarios() {
		ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
		for (Vertex<Usuario> vertice : grafo.vertices())
			usuarios.add(0, vertice.getElement());

		return usuarios;
	}

	public void setControlador(Controlador controlador) {
		this.controlador = controlador;
	}
}
