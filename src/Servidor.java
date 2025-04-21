import Model.Fase;
import Model.Player;
import Model.Tiro;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Servidor {
    private static DatagramSocket serverSocket;
    private static InetAddress clientAddress;
    private static int clientPort;
    private static final int PORT = 12345;
    private static long lastPacketTime = 0;
    private static final long TIMEOUT = 5000; // 5 seconds timeout
    private static final int SEND_RATE = 50; // Enviar atualizações a cada 50ms (~20fps)
    private static long lastSendTime = 0;

    public static void main(String[] args) {
        try {
            serverSocket = new DatagramSocket(PORT);
            System.out.println("Servidor UDP iniciado na porta " + PORT);
            
            Jogo jogo = new Jogo(true);
            final Player player1 = jogo.getPlayer1();
            final Player player2 = jogo.getPlayer2();
            
            // Thread para receber dados do cliente
            new Thread(() -> {
                try {
                    byte[] receiveData = new byte[1024];
                    
                    while (true) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        
                        // Guardar o endereço e porta do cliente para enviar respostas
                        clientAddress = receivePacket.getAddress();
                        clientPort = receivePacket.getPort();
                        lastPacketTime = System.currentTimeMillis();
                        
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        String tipo = tipoMsg(message);
                        
                        if (tipo.equals("POS")) {
                            int[] posData = extrairMsg(message);
                            player2.setX(posData[0]);
                            player2.setY(posData[1]);
                        } else if (tipo.equals("TIR")) {
                            player2.tiro();
                        } else if (tipo.equals("HBT") || tipo.equals("CON")) {
                            // Heartbeat ou conexão inicial, apenas atualiza o tempo do último pacote
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
            // Thread para enviar dados ao cliente
            AtomicInteger xAnterior = new AtomicInteger(0);
            AtomicInteger yAnterior = new AtomicInteger(0);
            AtomicInteger qtdAnterior = new AtomicInteger(0);
            AtomicInteger hp1Anterior = new AtomicInteger(5);
            AtomicInteger hp2Anterior = new AtomicInteger(5);
            
            new Thread(() -> {
                try {
                    while (true) {
                        long currentTime = System.currentTimeMillis();
                        
                        if (clientAddress != null) {
                            // Verificação de timeout
                            if (currentTime - lastPacketTime > TIMEOUT) {
                                System.out.println("Cliente desconectado (timeout)");
                                clientAddress = null;
                                continue;
                            }
                            
                            // Controle de taxa de envio
                            if (currentTime - lastSendTime >= SEND_RATE) {
                                lastSendTime = currentTime;
                                
                                // Enviar posição do player1 para o cliente
                                int x = player1.getX();
                                int y = player1.getY();
                                if (x != xAnterior.get() || y != yAnterior.get()) {
                                    enviarMensagem("POS:" + x + "," + y);
                                    xAnterior.set(x);
                                    yAnterior.set(y);
                                }
                                
                                // Enviar estado de HP periodicamente
                                if (hp1Anterior.get() != player1.getHp()) {
                                    enviarMensagem("HP1:" + player1.getHp());
                                    hp1Anterior.set(player1.getHp());
                                }
                                
                                if (hp2Anterior.get() != player2.getHp()) {
                                    enviarMensagem("HP2:" + player2.getHp());
                                    hp2Anterior.set(player2.getHp());
                                }
                            }
                            
                            // Enviar informação de tiro imediatamente (não esperar pelo controle de taxa)
                            int qtdTiros = player1.getQtdTiros();
                            if (qtdTiros > qtdAnterior.get()) {
                                enviarMensagem("TIR:");
                                qtdAnterior.set(qtdTiros);
                            }
                        }
                        
                        Thread.sleep(5); // Verificação frequente
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
    
    private static void enviarMensagem(String message) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                sendData, 
                sendData.length, 
                clientAddress, 
                clientPort
            );
            serverSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String tipoMsg(String msg) {
        return msg.substring(0, 3);
    }
    
    public static int[] extrairMsg(String msg) {
        String valores;
        String tipo = msg.substring(0, 3);
        int[] resultado = new int[0];
        
        valores = msg.substring(4);
        
        if (tipo.equals("POS")) {
            String[] sub = valores.split(",");
            resultado = new int[sub.length];
            for (int i = 0; i < sub.length; i++) {
                resultado[i] = Integer.parseInt(sub[i]);
            }
        } else if (tipo.equals("HP1") || tipo.equals("HP2")) {
            resultado = new int[1];
            resultado[0] = Integer.parseInt(msg.substring(4));
        }
        
        return resultado;
    }
}