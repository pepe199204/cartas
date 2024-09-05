import java.util.List;
import java.util.Map;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.JPanel;
public class Jugador {
    
    private final int TOTAL_CARTAS = 10;  // Número total de cartas por jugador
    private final int MARGEN = 10;        // Margen utilizado para mostrar cartas en la interfaz
    private final int DISTANCIA = 30;     // Distancia entre las cartas mostradas
    private int puntaje = 0;              // Variable que almacena el puntaje del jugador

    private Carta[] cartas = new Carta[TOTAL_CARTAS];  // Arreglo que guarda las cartas del jugador
    private Random r = new Random();  // Generador de números aleatorios para repartir cartas

    // Método para repartir cartas al jugador
    public void repartir() {
        int i = 0;
        for (Carta c : cartas) {
            cartas[i++] = new Carta(r);  // Se asigna una nueva carta aleatoria en cada iteración
        }
    }

    // Método para mostrar las cartas en un panel (interfaz gráfica)
    public void mostrar(JPanel pnl) {
        pnl.removeAll();  // Limpia el panel antes de agregar nuevas cartas

        int p = 0;
        for (Carta c : cartas) {
            c.mostrar(pnl, MARGEN + TOTAL_CARTAS * DISTANCIA - p++ * DISTANCIA, MARGEN);  // Muestra cada carta con una separación entre ellas
        }

        pnl.repaint();  // Actualiza el panel con las cartas nuevas
    }

    // Método que agrupa las cartas por su valor y pinta, detecta grupos y calcula el puntaje
    public String getGrupos() {

        puntaje = 0; // Limpia el puntaje antes de calcular nuevamente

        String mensaje = "No se encontraron grupos\n";  // Mensaje por defecto

        int[] contadores = new int[NombreCarta.values().length];  // Contador para los nombres de cartas
        int[] contadoresPinta = new int[Pinta.values().length];   // Contador para las pintas

        // Lista que guarda las cartas como pares (nombre, pinta)
        List<Map.Entry<NombreCarta, Pinta>> listaOrdenadaDeCartas = new ArrayList<>();
        // Mapa para agrupar las cartas por su pinta
        Map<Pinta, List<NombreCarta>> grupoPorPinta = new HashMap<>();

        // Rellenar los contadores y la lista de cartas
        for (Carta c : cartas) {
            if (c != null) {
                listaOrdenadaDeCartas.add(new AbstractMap.SimpleEntry<>(c.getNombre(), c.getPinta()));  // Añadir la carta a la lista
                contadores[c.getNombre().ordinal()]++;  // Contar el número de veces que aparece cada carta
                contadoresPinta[c.getPinta().ordinal()]++;  // Contar el número de cartas por pinta
            }
        }

        boolean hayGrupos = false;  // Indica si se encontraron grupos
        Set<NombreCarta> grupoCartas = new HashSet<>();  // Conjunto que almacena cartas que forman grupos

        // Detectar grupos de cartas con el mismo nombre
        for (int i = 0; i < contadores.length; i++) {
            if (contadores[i] >= 2) {  // Si hay al menos 2 cartas del mismo nombre, hay un grupo
                if (!hayGrupos) {
                    hayGrupos = true;
                    mensaje = "Se encontraron los siguientes grupos:\n";  // Actualiza el mensaje si se detectan grupos
                }
                mensaje += Grupo.values()[contadores[i]] + " de " + NombreCarta.values()[i] + "\n";  // Agrega información del grupo al mensaje
                grupoCartas.add(NombreCarta.values()[i]);  // Añadir la carta al conjunto de grupos
            }
        }

        // Ordenar las cartas por su nombre
        Collections.sort(listaOrdenadaDeCartas, Comparator.comparing(Map.Entry::getKey));
        System.out.println("Lista de cartas ordenadas: " + listaOrdenadaDeCartas);

        // Agrupar las cartas por pinta
        for (Map.Entry<NombreCarta, Pinta> carta : listaOrdenadaDeCartas) {
            Pinta pinta = carta.getValue();
            NombreCarta nombre = carta.getKey();

            grupoPorPinta.putIfAbsent(pinta, new ArrayList<>());  // Crear una nueva lista si la pinta aún no existe
            grupoPorPinta.get(pinta).add(nombre);  // Añadir la carta a la lista correspondiente a su pinta
        }

        Set<NombreCarta> escaleraCartas = new HashSet<>();  // Conjunto que almacena cartas que forman escaleras

        // Detectar escaleras (secuencias) de cartas dentro de cada pinta
        for (Map.Entry<Pinta, List<NombreCarta>> carta : grupoPorPinta.entrySet()) {
            System.out.println("Pinta: " + carta.getKey() + " -> cartas: " + carta.getValue());
            Pinta pinta = carta.getKey();
            List<NombreCarta> nombreCarta = carta.getValue();
            List<List<NombreCarta>> escaleras = buscarEscalera(nombreCarta);  // Buscar escaleras en las cartas de la misma pinta

            // Agregar las cartas que forman escaleras al conjunto
            for (List<NombreCarta> escalera : escaleras) {
                escaleraCartas.addAll(escalera);  // Añadir todas las cartas que forman parte de una escalera
            }

            // Mostrar las escaleras encontradas
            if (!escaleras.isEmpty()) {
                System.out.println("Pinta: " + pinta + " tiene la siguiente escalera:");
                mensaje += pinta + " tiene la siguiente escalera:\n";
                for (List<NombreCarta> escalera : escaleras) {
                    String stringEscalera = String.join(", ", escalera.toString().replace("[", "").replace("]", ""));
                    System.out.println("-> " + stringEscalera);

                    mensaje += stringEscalera + "\n";
                }
            } else {
                System.out.println("Pinta: " + pinta + " no tiene escalera.");
            }
        }

        // Calcular puntaje de las cartas que no están en grupos ni en escaleras
        for (Carta c : cartas) {
            if (c != null && !grupoCartas.contains(c.getNombre()) && !escaleraCartas.contains(c.getNombre())) {
                System.out.println("Carta para sumar al puntaje: " + c.getNombre());
                puntaje += obtenerPuntaje(c.getNombre());  // Sumar el puntaje de las cartas que no están en grupos ni escaleras
            }
        }

        System.out.println("Puntaje total de cartas sueltas (no en grupos ni escaleras): " + puntaje);
        mensaje += "Puntaje total de cartas sueltas (no en grupos ni escaleras): " + puntaje + "\n";

        return mensaje;
    }

    // Método que busca secuencias (escaleras) dentro de una lista de cartas
    public static List<List<NombreCarta>> buscarEscalera(List<NombreCarta> cartas) {
        List<List<NombreCarta>> escaleras = new ArrayList<>();
        List<NombreCarta> escaleraActual = new ArrayList<>();

        // Recorre las cartas buscando secuencias consecutivas
        for (int i = 0; i < cartas.size(); i++) {
            if (i == 0 || cartas.get(i).ordinal() == cartas.get(i - 1).ordinal() + 1) {
                escaleraActual.add(cartas.get(i));  // Si la carta es consecutiva, se añade a la escalera actual
            } else {
                if (escaleraActual.size() >= 3) {
                    escaleras.add(new ArrayList<>(escaleraActual));  // Si la escalera tiene 3 o más cartas, se añade a la lista de escaleras
                }
                escaleraActual.clear();
                escaleraActual.add(cartas.get(i));  // Reiniciar la escalera actual
            }
        }

        if (escaleraActual.size() >= 3) {
            escaleras.add(escaleraActual);  // Añadir la última escalera si es válida
        }

        return escaleras;  // Retornar la lista de escaleras encontradas
    }

    // Método para obtener el puntaje de una carta
    public static int obtenerPuntaje(NombreCarta carta) {
        switch (carta) {
            case AS:
            case JACK:
            case QUEEN:
            case KING:
                return 10;  // Las cartas especiales valen 10 puntos
            default:
                return carta.ordinal() + 1;  // El puntaje de las cartas numéricas es su valor ordinal + 1
        }
    }
}

