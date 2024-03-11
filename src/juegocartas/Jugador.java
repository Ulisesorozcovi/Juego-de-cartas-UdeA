package juegocartas;

import java.util.Random;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Jugador {

    private int TOTAL_CARTAS = 10;
    private int MARGEN = 20;
    private int DISTANCIA = 50;

    private Carta[] cartas = new Carta[TOTAL_CARTAS];

    private Random r = new Random();

    // Creo un arrayList donde ir quitando las cartas que hacen parte de una
    // escalera o forman parte de un grupo
    private ArrayList<Carta> cartasDisponibles = new ArrayList<>();

    public void repartir() {
        for (int i = 0; i < TOTAL_CARTAS; i++) {
            cartas[i] = new Carta(r);
        }
    }

    public void mostrar(JPanel pnl) {
        pnl.removeAll();
        int x = MARGEN;
        //recorrido objetual por una lista de objetos
        for (Carta c : cartas) {
            c.mostrar(pnl, x, MARGEN);
            x += DISTANCIA;
        }

        pnl.repaint();
    }

    public String getEscaleras() {
        // Inicializo el mensaje como vacío
        String mensaje = "";

        // El HasMap me permite guardar elemento de la forma key/value
        // En este caso me es muy útil porque quiero guardar las cartas por 
        // pinta para armar las escaleras haciendo el filtro por pinta
        Map<Pinta, ArrayList<Carta>> cartasPorPinta = new HashMap<>();
        Map<Pinta, List<List<Carta>>> escalerasPorPinta = new HashMap<>();

        // Este ciclo va iterando sobre los diversos valores de Pinta
        // Corazon, trebol..etc
        // Al final lo que hace es asociar cada pinta con un arrayList
        for (Pinta pinta : Pinta.values()) {
            cartasPorPinta.put(pinta, new ArrayList<>());
            escalerasPorPinta.put(pinta, new ArrayList<>());
        }

        // Ya con cada cada ArrayList por pinta creado debo asociarlos
        // Entonces itero sobre las cartas. Con el metodo getPinta obtengo
        // La pinta de la carta y dependiendo de esta la agrego al ArrayList
        // Que le corresponde. 
        // Adicional a esto, agrego todas las cartas al ArrayList de cartas
        // disponibles para removerlas posteriormente si hacen parte de una escalera
        for (Carta c : cartas) {
            cartasPorPinta.get(c.getPinta()).add(c);
            cartasDisponibles.add(c);
        }

        // Itero sobre cada una de las pintas: Diamante, Corazon, etc..
        for (Pinta pinta : Pinta.values()) {
            // En esta variable estoy almacenando el ArrayList de cartas
            //Asociada a la pinta en la que estoy actualmente
            ArrayList<Carta> cartasPintaActual = cartasPorPinta.get(pinta);

            // Ya que estoy usando un ArrayList puedo usar una de las 
            // utilidades de Collections que es el sort. 
            // Al sort se le pasan dos argumentos. Uno es el ArrayList
            // y el otro es un comparador, para este usamos la interface de 
            // Comparator y su método comparingInt que nos permite ordenar el
            // ArrayList por orden numerico. Como estamos usando los ordinales,
            // ordenaria por ejemplo 0,1,2,5,7,9
            Collections.sort(cartasPintaActual, Comparator.comparingInt(c -> c.getNombre().ordinal()));

            // En esta variable iré guardando las escaleras hasta que las 
            // agregue al arrayList
            ArrayList<Carta> tempEscalera = new ArrayList<>();

            // Empiezo a iterar sobre cada una de las cartas
            // del array list de la pinta
            for (int i = 0; i < cartasPintaActual.size() - 1; i++) {
                // Defino un current y un next
                // Si la diferencia entre los dos es 1, significa que son 
                // consiguientes, por lo que se hace escalera
                Carta current = cartasPintaActual.get(i);
                Carta next = cartasPintaActual.get(i + 1);

                if (next.getNombre().ordinal() - current.getNombre().ordinal() == 1) {
                    // Agrego el current y next a la escalera temporal
                    tempEscalera.add(current);
                    tempEscalera.add(next);
                    
                    // Ya que hacen parte de una escalera, los remuevo de la 
                    // lista de cartas disponibles
                    cartasDisponibles.remove(current);
                    cartasDisponibles.remove(next);
                } else {
                    // Como la diferencia entre current y next es mayor que 1,
                    // Significa que la escalera se rompe. Entonces, si la escalera
                    // Actual tiene al menos 2 elementos, la agrego a escalerasPorPinta
                    // Caso contrario solo limpio la escalera temporal
                    if (tempEscalera.size() >= 2) {
                        escalerasPorPinta.get(pinta).add(new ArrayList<>(tempEscalera));
                    }
                    tempEscalera.clear();
                }
            }
               
            // Aqui verifico si la escalera temporal tiene mas de 2 elementos y
            // la agrego al array list de escaleras de la pinta
            if (tempEscalera.size() >= 2) {
                escalerasPorPinta.get(pinta).add(new ArrayList<>(tempEscalera));
            }
        }
        
        // Aqui itero sobre las pintas y sobre las escaleras
        // asociadas a cada pinta
        for (Pinta pinta : Pinta.values()) {
            for (List<Carta> escalera : escalerasPorPinta.get(pinta)) {
                mensaje += "Se encontró una escalera de " + escalera.get(0).getNombre() + " a " + escalera.get(escalera.size() - 1).getNombre() + " de " + escalera.get(0).getPinta() + "\n";
            }
        }

        if (mensaje.isEmpty()) {
            mensaje = "No se encontraron escaleras";
        } else {
            // Esto es para remover la ultima linea en blanco agregada
            return mensaje.substring(0, mensaje.length() - 1);
        }

        return mensaje;
    }

    public String getGrupos() {
        String mensaje = "No se encontraron grupos";
        int[] contadores = new int[NombreCarta.values().length];
        // En lugar de obtener los grupos del total de cartas
        // Lo hago de las cartas disponibles después de armar las escaleras
        for (Carta c : cartasDisponibles) {
            contadores[c.getNombre().ordinal()]++;
        }

        int totalGrupos = 0;
        for (int c : contadores) {
            if (c >= 2) {
                totalGrupos++;
            }
        }
        if (totalGrupos > 0) {
            mensaje = "Los grupos encontrados fueron:\n";
            for (int i = 0; i < contadores.length; i++) {
                if (contadores[i] >= 2) {
                    mensaje += Grupo.values()[contadores[i]] + " de " + NombreCarta.values()[i] + "\n";
                    // Las cartas que hagan parte de un grupo deben ser removidad
                    // de cartas disponibles para calcular el puntaje después
                    NombreCarta nombreCarta = NombreCarta.values()[i];
                    cartasDisponibles.removeIf(carta -> carta.getNombre() == nombreCarta);
                }
            }
        }
        return mensaje;
    }
    
     public int getPuntaje() {
        int puntaje = 0;
        for (Carta c : cartasDisponibles) {
            // Realizo la condición de puntaje para las cartas de letra
            if (c.getNombre() == NombreCarta.AS || c.getNombre() == NombreCarta.JACK || c.getNombre() == NombreCarta.QUEEN || c.getNombre() == NombreCarta.KING) {
                puntaje += 10;
            } else {
                puntaje += c.getNombre().ordinal() + 1;
            }
        }
        
        // Después de obtener el puntaje, para evitar errores reinicio 
        // el array list de cartas disponibles
        cartasDisponibles.clear();
        return puntaje;
    }
    
    // Obtengo las escaleras, grupos y puntaje del jugador
     public String getMessage() {
        return getEscaleras() + "\n" + getGrupos() + "\n" + "Puntaje: " + getPuntaje();
    }
}
