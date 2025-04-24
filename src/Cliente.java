import Model.Fase;
import Model.Player;
import Model.Tiro;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Cliente {
    private static DatagramSocket clientSocket;
    private static InetAddress serverAddress;
    private static final int SERVER_PORT = 12345;
    private static final float EXTRAPOLATION_FACTOR = 0.3f;
    private static long lastUpdateTime = 0;
    private static long lastMovementTime = 0;
    
    public static void main(String[] args) {
        try {
            clientSocket = new DatagramSocket();
            serverAddress = InetAddress.getByName("localhost");
            System.out.println("Cliente iniciado, conectando ao servidor...");
            
            Jogo jogo = new Jogo(false);
            final Player player1 = jogo.getPlayer1(); // Servidor player
            final Player player2 = jogo.getPlayer2(); // Cliente player
            
            // Posição anterior para extrapolação
            final int[] lastPos = {player1.getX(), player1.getY()};
            final float[] velocity = {0, 0}; // Usando float para permitir valores fracionários
            final boolean[] isMoving = {false};
            
            // Thread para receber dados do servidor
            new Thread(() -> {
                Timestamp timestamp_recebido;
                try {
                    byte[] receiveData = new byte[1024];
                    
                    while (true) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        clientSocket.receive(receivePacket);
                        timestamp_recebido = new Timestamp(System.currentTimeMillis());
                        System.out.println("Input recebido: "+timestamp_recebido);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        String tipo = Servidor.tipoMsg(message);
                        
                        if (tipo.equals("POS")) {
                            int[] pos = Servidor.extrairMsg(message);
                            
                            // Verificar se houve movimento real
                            if (pos[0] != lastPos[0] || pos[1] != lastPos[1]) {
                                // Calcular velocidade para extrapolação
                                velocity[0] = (pos[0] - lastPos[0]) * EXTRAPOLATION_FACTOR; // Aplicar fator de extrapolação
                                velocity[1] = (pos[1] - lastPos[1]) * EXTRAPOLATION_FACTOR; // Aplicar fator de extrapolação
                                
                                // Se houver movimento significativo, marcar como movendo
                                if (Math.abs(velocity[0]) > 0 || Math.abs(velocity[1]) > 0) {
                                    isMoving[0] = true;
                                    lastMovementTime = System.currentTimeMillis();
                                }
                            } else {
                                // Se a posição não mudou, começar a diminuir a velocidade gradualmente
                                velocity[0] *= 0.8f; // Reduz a velocidade em 20% a cada atualização
                                velocity[1] *= 0.8f;
                                
                                // Se a velocidade for muito pequena, zerar
                                if (Math.abs(velocity[0]) < 0.1f && Math.abs(velocity[1]) < 0.1f) {
                                    velocity[0] = 0;
                                    velocity[1] = 0;
                                    isMoving[0] = false;
                                }
                            }
                            
                            // Atualizar diretamente para a posição real sem extrapolação
                            // A extrapolação será aplicada apenas entre atualizações
                            player1.setX(pos[0]);
                            player1.setY(pos[1]);
                            
                            // Armazenar a última posição real
                            lastPos[0] = pos[0];
                            lastPos[1] = pos[1];
                            lastUpdateTime = System.currentTimeMillis();
                        } else if (tipo.equals("TIR")) {
                            player1.tiro();
                        } else if (tipo.equals("HP1")) {
                            int[] hp = Servidor.extrairMsg(message);
                            player1.setHp(hp[0]);
                            if (player1.getHp() <= 0) {
                                player1.setVisible(false);
                            }
                        } else if (tipo.equals("HP2")) {
                            int[] hp = Servidor.extrairMsg(message);
                            player2.setHp(hp[0]);
                            if (player2.getHp() <= 0) {
                                player2.setVisible(false);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
            // Thread para continuar extrapolação entre pacotes
            new Thread(() -> {
                try {
                    while (true) {
                        // Se faz tempo que não recebemos uma atualização, mas não muito tempo
                        long currentTime = System.currentTimeMillis();
                        long timeSinceUpdate = currentTime - lastUpdateTime;
                        
                        if (timeSinceUpdate > 50 && timeSinceUpdate < 200 && isMoving[0]) {
                            // Aplicar extrapolação suavizada
                            // Usar valores mais conservadores para distâncias maiores
                            float attenuationFactor = Math.min(1.0f, 100.0f / timeSinceUpdate); // Diminui extrapolação com o tempo
                            
                            int newX = player1.getX() + (int)(velocity[0] * attenuationFactor);
                            int newY = player1.getY() + (int)(velocity[1] * attenuationFactor);
                            
                            player1.setX(newX);
                            player1.setY(newY);
                        }
                        
                        // Se não recebemos movimento por um tempo, diminuir gradualmente a velocidade
                        if (currentTime - lastMovementTime > 200) {
                            velocity[0] *= 0.9f; // Redução mais gradual
                            velocity[1] *= 0.9f;
                            
                            // Parar completamente após algum tempo
                            if (currentTime - lastMovementTime > 300 || 
                                (Math.abs(velocity[0]) < 0.1f && Math.abs(velocity[1]) < 0.1f)) {
                                velocity[0] = 0;
                                velocity[1] = 0;
                                isMoving[0] = false;
                            }
                        }
                        
                        Thread.sleep(16); // ~60fps
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
            // Thread para enviar dados ao servidor
            AtomicInteger xAnterior = new AtomicInteger(0);
            AtomicInteger yAnterior = new AtomicInteger(0);
            AtomicInteger qtdAnterior = new AtomicInteger(0);
            
            new Thread(() -> {
                Timestamp timestamp_enviado;
                try {
                    // Enviar mensagem inicial para estabelecer conexão
                    enviarMensagem("CON:");
                    
                    while (true) {
                        int x = player2.getX();
                        int y = player2.getY();
                        
                        // Enviar posição se mudou
                        if (x != xAnterior.get() || y != yAnterior.get()) {
                            timestamp_enviado = new Timestamp(System.currentTimeMillis());
                            enviarMensagem("POS:" + x + "," + y);
                            System.out.println("Input enviado(MOVIMENTAÇÃO):"+timestamp_enviado);
                            xAnterior.set(x);
                            yAnterior.set(y);
                            System.out.println("Mensagem enviada para servidor.");
                        }
                        
                        // Enviar informação de tiro
                        int qtdTiros = player2.getQtdTiros();
                        if (qtdTiros > qtdAnterior.get()) {
                            timestamp_enviado = new Timestamp(System.currentTimeMillis());
                            enviarMensagem("TIR:");
                            System.out.println("Input enviado(MOVIMENTAÇÃO):"+timestamp_enviado);
                            qtdAnterior.set(qtdTiros);
                            System.out.println("Mensagem enviada para servidor.");
                        }
                        
                        // Enviar heartbeat periódico para manter a conexão
                        if (System.currentTimeMillis() % 1000 < 20) {
                            enviarMensagem("HBT:");
                            System.out.println("Mensagem enviada para servidor.");
                        }
                        
                        Thread.sleep(33); // ~30fps para envio (reduz banda)
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
            
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void enviarMensagem(String message) {
        try {
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(
                sendData, 
                sendData.length, 
                serverAddress, 
                SERVER_PORT
            );
            clientSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}