import Model.Fase;
import Model.Player;
import Model.Tiro;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Cliente {
    public static void main(String[] args) {
        try {
            Socket cliente = new Socket("localhost",12345);
            System.out.println("Conectado ao servidor!");
            Jogo jogo = new Jogo(false);
            final Player player2 = jogo.getPlayer2();
            final Player player1 = jogo.getPlayer1();
            new Thread(()->{
                try {
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                    String msg;
                    String tipo;
                    while((msg = entrada.readLine()) != null){
                        tipo = Servidor.tipoMsg(msg);
                        if(tipo.equals("POS")){
                            int[] e = Servidor.extrairMsg(msg);
                            player1.setX(e[0]);
                            player1.setY(e[1]);
                        }
                        if(tipo.equals("TIR")){
                            player1.tiro();
                        }
                        if(tipo.equals("HP1")){
                            int[] e = Servidor.extrairMsg(msg);
                            player1.setHp(e[0]);
                            if(player1.getHp()==0){
                                player1.setVisible(false);
                            }
                        }
                        if(tipo.equals("HP2")){
                            int[] e = Servidor.extrairMsg(msg);
                            player2.setHp(e[0]);
                            if(player2.getHp()==0){
                                player2.setVisible(false);
                            }
                        }
//                        System.out.println("Servidor disse: " + msg); // Mostra a mensagem recebida
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
            AtomicInteger xAnterior = new AtomicInteger(0);
            AtomicInteger yAnterior = new AtomicInteger(0);
            AtomicInteger qtdAnterior = new AtomicInteger(0);
            AtomicInteger hp = new AtomicInteger(0);
            new Thread(() -> {
                try {
                    PrintWriter saida = new PrintWriter(cliente.getOutputStream(), true);
                    while (true) {
                        int x = player2.getX();
                        int y = player2.getY();
                        if(x != xAnterior.get() || y != yAnterior.get()){
                            saida.println("POS:" + x + "," + y);
                            xAnterior.set(x);
                            yAnterior.set(y);
                        }
                        int qtdTiros = player2.getQtdTiros();
                        if(qtdTiros>qtdAnterior.get()){
                            saida.println("TIR:");
                            qtdAnterior.set(qtdTiros);
                        }

//                        Thread.sleep(10); // delay para evitar flood (ajustável)
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
