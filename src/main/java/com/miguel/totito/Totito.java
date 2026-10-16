package com.miguel.totito;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;

/** Totito: jugador (X) contra computadora (O). */
public class Totito {
    // Se mantiene la idea original: matriz 3x3 con botones Swing.
    private final String[][] tablero = new String[3][3];
    private final JButton[][] botones = new JButton[3][3];
    private final JFrame ventana = new JFrame("Totito - Contra la computadora");
    private final JLabel estado = new JLabel("Tu turno: X", SwingConstants.CENTER);
    private final JLabel marcador = new JLabel("", SwingConstants.CENTER);
    private final java.util.List<String> historial = new ArrayList<>();
    private final Random azar = new Random();
    private final Path archivo = Paths.get(System.getProperty("user.home"), ".totito-jdk25-historial.txt");
    private int victorias, derrotas, empates;
    private boolean terminado, pensando;
    private int numeroPartida = 1;

    public Totito() {
        cargarHistorial();
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(510, 630);
        ventana.setMinimumSize(new Dimension(410, 510));
        ventana.setLocationRelativeTo(null);
        ventana.setLayout(new BorderLayout(10, 10));
        ventana.getContentPane().setBackground(new Color(240, 245, 252));

        JPanel arriba = new JPanel(new GridLayout(4, 1, 0, 5));
        arriba.setBackground(new Color(240, 245, 252));
        JLabel titulo = new JLabel("TOTITO", SwingConstants.CENTER);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 31));
        titulo.setForeground(new Color(30, 59, 100));
        JLabel subtitulo = new JLabel("Tú eres X  •  La computadora es O", SwingConstants.CENTER);
        estado.setFont(new Font("SansSerif", Font.BOLD, 17));
        marcador.setFont(new Font("SansSerif", Font.PLAIN, 15));
        arriba.add(titulo);
        arriba.add(subtitulo);
        arriba.add(estado);
        arriba.add(marcador);
        ventana.add(arriba, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(3, 3, 8, 8));
        centro.setBackground(new Color(240, 245, 252));
        centro.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        for (int f = 0; f < 3; f++) {
            for (int c = 0; c < 3; c++) {
                tablero[f][c] = "";
                JButton boton = new JButton("");
                boton.setFont(new Font("SansSerif", Font.BOLD, 57));
                boton.setBackground(Color.WHITE);
                boton.setFocusPainted(false);
                final int fila = f, columna = c;
                boton.addActionListener(e -> jugar(fila, columna));
                botones[f][c] = boton;
                centro.add(boton);
            }
        }
        ventana.add(centro, BorderLayout.CENTER);

        JPanel abajo = new JPanel(new GridLayout(2, 2, 8, 8));
        abajo.setBackground(new Color(240, 245, 252));
        abajo.setBorder(BorderFactory.createEmptyBorder(5, 24, 20, 24));
        JButton nueva = new JButton("Nueva partida");
        JButton ver = new JButton("Ver historial");
        JButton borrar = new JButton("Borrar historial");
        JButton ayuda = new JButton("Cómo jugar");
        nueva.addActionListener(e -> nuevaPartida());
        ver.addActionListener(e -> mostrarHistorial());
        borrar.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(ventana, "¿Borrar el historial y reiniciar el marcador?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opcion == JOptionPane.YES_OPTION) {
                historial.clear(); victorias = 0; derrotas = 0; empates = 0; numeroPartida = 1;
                guardarHistorial(); actualizarMarcador(); nuevaPartida();
            }
        });
        ayuda.addActionListener(e -> JOptionPane.showMessageDialog(ventana,
                "Haz clic en una casilla para colocar X.\nLa computadora responde con O.\nGana quien forme 3 fichas en línea.\nEl historial guarda las partidas terminadas."));
        abajo.add(nueva); abajo.add(ver); abajo.add(borrar); abajo.add(ayuda);
        ventana.add(abajo, BorderLayout.SOUTH);
        actualizarMarcador();
        ventana.setVisible(true);
    }

    private void jugar(int f, int c) {
        if (terminado || pensando || !tablero[f][c].isEmpty()) return;
        colocar(f, c, "X");
        if (evaluar("X")) return;
        pensando = true;
        estado.setText("La computadora está jugando...");
        // Un pequeño retraso permite ver claramente la respuesta de la máquina.
        TimerSwing.responder(400, () -> {
            if (!pensando || terminado) return;
            int[] mov = movimientoComputadora();
            if (mov != null) colocar(mov[0], mov[1], "O");
            pensando = false;
            if (!evaluar("O")) estado.setText("Tu turno: X");
        });
    }

    private void colocar(int f, int c, String ficha) {
        tablero[f][c] = ficha;
        botones[f][c].setText(ficha);
        botones[f][c].setForeground(ficha.equals("X") ? new Color(38, 100, 192) : new Color(210, 78, 82));
    }

    private boolean evaluar(String ficha) {
        String resultado = null;
        if (gano(ficha)) {
            resultado = ficha.equals("X") ? "¡Ganaste!" : "Ganó la computadora";
            if (ficha.equals("X")) victorias++; else derrotas++;
        } else if (lleno()) {
            resultado = "¡Empate!";
            empates++;
        }
        if (resultado == null) return false;
        terminado = true;
        pensando = false;
        estado.setText(resultado);
        historial.add("Partida " + numeroPartida++ + " | " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " | " + resultado);
        guardarHistorial();
        actualizarMarcador();
        JOptionPane.showMessageDialog(ventana, resultado + "\nPresiona Nueva partida para volver a jugar.");
        return true;
    }

    private int[] movimientoComputadora() {
        // Primero gana si puede; luego bloquea una victoria inmediata del jugador.
        int[] mov = buscarJugada("O");
        if (mov != null) return mov;
        mov = buscarJugada("X");
        if (mov != null) return mov;
        if (tablero[1][1].isEmpty()) return new int[]{1, 1};
        java.util.List<int[]> esquinas = new ArrayList<>();
        for (int[] p : new int[][]{{0,0},{0,2},{2,0},{2,2}}) if (tablero[p[0]][p[1]].isEmpty()) esquinas.add(p);
        if (!esquinas.isEmpty()) return esquinas.get(azar.nextInt(esquinas.size()));
        java.util.List<int[]> libres = new ArrayList<>();
        for (int f = 0; f < 3; f++) for (int c = 0; c < 3; c++) if (tablero[f][c].isEmpty()) libres.add(new int[]{f,c});
        return libres.isEmpty() ? null : libres.get(azar.nextInt(libres.size()));
    }

    private int[] buscarJugada(String ficha) {
        for (int f = 0; f < 3; f++) for (int c = 0; c < 3; c++) {
            if (!tablero[f][c].isEmpty()) continue;
            tablero[f][c] = ficha;
            boolean gana = gano(ficha);
            tablero[f][c] = "";
            if (gana) return new int[]{f, c};
        }
        return null;
    }

    private boolean gano(String ficha) {
        for (int i = 0; i < 3; i++) {
            if (tablero[i][0].equals(ficha) && tablero[i][1].equals(ficha) && tablero[i][2].equals(ficha)) return true;
            if (tablero[0][i].equals(ficha) && tablero[1][i].equals(ficha) && tablero[2][i].equals(ficha)) return true;
        }
        return (tablero[0][0].equals(ficha) && tablero[1][1].equals(ficha) && tablero[2][2].equals(ficha))
            || (tablero[0][2].equals(ficha) && tablero[1][1].equals(ficha) && tablero[2][0].equals(ficha));
    }

    private boolean lleno() {
        for (String[] fila : tablero) for (String celda : fila) if (celda.isEmpty()) return false;
        return true;
    }

    private void nuevaPartida() {
        pensando = false; terminado = false;
        estado.setText("Tu turno: X");
        for (int f = 0; f < 3; f++) for (int c = 0; c < 3; c++) {
            tablero[f][c] = "";
            botones[f][c].setText("");
            botones[f][c].setBackground(Color.WHITE);
        }
    }

    private void actualizarMarcador() {
        marcador.setText("Ganadas: " + victorias + "    Perdidas: " + derrotas + "    Empates: " + empates);
    }

    private void mostrarHistorial() {
        JTextArea area = new JTextArea(historial.isEmpty() ? "Todavía no hay partidas terminadas." : String.join("\n", historial));
        area.setEditable(false); area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(440, 280));
        JOptionPane.showMessageDialog(ventana, scroll, "Historial de partidas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cargarHistorial() {
        if (!Files.exists(archivo)) return;
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                if (linea.isBlank()) continue;
                historial.add(linea);
                if (linea.endsWith("¡Ganaste!")) victorias++;
                else if (linea.endsWith("Ganó la computadora")) derrotas++;
                else if (linea.endsWith("¡Empate!")) empates++;
            }
            numeroPartida = historial.size() + 1;
        } catch (IOException ex) { System.err.println("No se pudo leer el historial: " + ex.getMessage()); }
    }

    private void guardarHistorial() {
        try { Files.write(archivo, historial, StandardCharsets.UTF_8); }
        catch (IOException ex) { JOptionPane.showMessageDialog(ventana, "No se pudo guardar el historial: " + ex.getMessage()); }
    }

    private static class TimerSwing {
        static void responder(int milisegundos, Runnable accion) {
            javax.swing.Timer timer = new javax.swing.Timer(milisegundos, e -> accion.run());
            timer.setRepeats(false); timer.start();
        }
    }

    public static void main(String[] args) { SwingUtilities.invokeLater(Totito::new); }
}
