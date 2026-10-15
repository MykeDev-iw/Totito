

package com.miguel.totito;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.GridLayout;


public class Totito {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        
    
        
        JFrame ventana = new JFrame();
       
            ventana.setSize(400,400);
            ventana.setLayout(new GridLayout(3, 3));
            ventana.setTitle("Totito");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JButton[][] botones = new JButton [3][3];

                String [][] tablero = new String [3][3];
                for (int fila = 0; fila < 3; fila++){
                    for (int columna = 0; columna < 3; columna ++){
                        tablero [fila][columna] = "";
       
                        JButton boton = new JButton();
                      
                        botones [fila][columna] = boton;
                      
                        
                        ventana.add(boton);
                        final int filaBoton = fila;
                        final int columnaBoton  = columna;
                        
                        boton.addActionListener(e -> { //click
                            if (tablero [filaBoton][columnaBoton].isEmpty()){
                                 boton.setText("X");
                                  tablero [filaBoton][columnaBoton] = "X";  
                                  
                                  if (tablero[0][0].equals("X") &&  tablero[1][1].equals("X") && tablero [2][2].equals("X")) {
                                      System.out.println("¡GANASTE!");
                                   
                                  }
                                  
                                  boolean yaJugo = false;
                                  
                                  for (int filas = 0; filas < 3; filas++){
                                      for (int columnas = 0; columnas < 3; columnas++){
                                          if (tablero [filas][columnas].isEmpty() && yaJugo == false){
                                              tablero [filas][columnas] = "O";
                                              botones [filas][columnas].setText("O");
                                              yaJugo = true;
                                          }
                                      }
                                  }
                            }
                    });
                
                    }
                }
            
             ventana.setVisible(true);
           
   
      
            }
}
