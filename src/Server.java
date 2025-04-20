import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(5000);
        byte[] buffer = new byte[1024];

        ClientInfo client1 = null;
        ClientInfo client2 = null;

        System.out.println("Servidor UDP iniciado na porta 5000...");

        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            // Primeiro cliente
            if (client1 == null) {
                client1 = new ClientInfo(address, port);
                System.out.println("client 01 conectado: " + client1);
                continue;
            }

            // Segundo cliente
            if (client2 == null && !client1.equals(address, port)) {
                client2 = new ClientInfo(address, port);
                System.out.println("client 02 conectado: " + client2);
                continue;
            }

            // Só encaminha quando os dois clientes já estiverem registrados
            if (client1 != null && client2 != null) {
                ClientInfo destino = null;

                if (client1.equals(address, port)) {
                    destino = client2;
                } else if (client2.equals(address, port)) {
                    destino = client1;
                }

                if (destino != null) {
                    DatagramPacket response =
                        new DatagramPacket(packet.getData(), packet.getLength(), destino.address, destino.port);
                    socket.send(response);
                    System.out.println("Mensagem encaminhada para " + destino);
                }
            }
        }
    }
}
