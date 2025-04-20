import Model.Fase;
import Model.Player;
import Model.Tiro;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {
    private static List<Socket> clientes = new ArrayList<>();

    public static void main(String[] args){

        try{
            ServerSocket servidor = new ServerSocket(12345);
            System.out.println("Servidor iniciado na porta 12345");
            while(true){
                Socket cliente = servidor.accept();
                clientes.add(cliente);
                Jogo jogo = new Jogo(true);
                final Player player1 = jogo.getPlayer1();
                final Player player2 = jogo.getPlayer2();
                new Thread(()->{
                    try {
                        BufferedReader entrada = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                        String msg;
                        String tipo;
                        while((msg = entrada.readLine()) != null){
                                tipo = tipoMsg(msg);
                                System.out.println("Cliente disse: " + msg);
                                if(tipo.equals("POS")){
                                    int[] e = extrairMsg(msg);
                                    player2.setX(e[0]);
                                    player2.setY(e[1]);
                                }
                                if(tipo.equals("TIR")){
                                    player2.tiro();
                                }
//                            for(Socket c:clientes){
//                                if(c!=cliente){
//                                    PrintStream saida = new PrintStream(c.getOutputStream(), true);
//                                    saida.println(msg);
//                                }
//                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                AtomicInteger xAnterior = new AtomicInteger(0);
                AtomicInteger yAnterior = new AtomicInteger(0);
                AtomicInteger qtdAnterior = new AtomicInteger(0);
                AtomicInteger hp1Anterior = new AtomicInteger(0);
                AtomicInteger hp2Anterior = new AtomicInteger(0);
                new Thread(() -> {
                    try {
                        PrintWriter saida = new PrintWriter(cliente.getOutputStream(), true);
                        while (true) {
                            int x = player1.getX();
                            int y = player1.getY();
                            if(x != xAnterior.get() || y != yAnterior.get()){
                                saida.println("POS:" + x + "," + y);
                                xAnterior.set(x);
                                yAnterior.set(y);
                            }
                            int qtdTiros = player1.getQtdTiros();
                            if(qtdTiros>qtdAnterior.get()){
                                saida.println("TIR:");
                                qtdAnterior.set(qtdTiros);
                            }
                            if(hp1Anterior.get()!=player1.getHp()){
                                saida.println("HP1:"+player1.getHp());
                                hp1Anterior.set(player1.getHp());
                            }
                            if(hp2Anterior.get()!=player2.getHp()){
                                saida.println("HP2:"+player2.getHp());
                                hp2Anterior.set(player2.getHp());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }catch (Exception e){
            System.out.println("Erro: " + e.getMessage());
        }
    }
    public static String tipoMsg(String msg){
        return msg.substring(0,3);
    }
    public static int[] extrairMsg(String msg){
        String tipo, valores;
        int[] resultado = new int[0];
        tipo = msg.substring(0,3);
        valores = msg.substring(4);
        if(tipo.equals("POS")){
            String[] sub = valores.split(",");
            resultado = new int[sub.length];
            for (int i = 0; i < sub.length; i++) {
                resultado[i] = Integer.parseInt(sub[i]);
            }
        }
        if(tipo.equals("HP1") || tipo.equals("HP2")){
            resultado = new int[1];
            resultado[0] = Integer.parseInt(msg.substring(4));
            return resultado;
        }
        return resultado;
    }
}