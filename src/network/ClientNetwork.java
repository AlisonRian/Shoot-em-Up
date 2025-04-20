package network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Model.Player;

public class ClientNetwork {
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private static final int SERVER_PORT = 5000;
    private byte[] buffer = new byte[1024];

    public ClientNetwork() throws Exception {
        socket = new DatagramSocket();
        serverAddress = InetAddress.getByName("127.0.0.1");
    }

    public void sendState(Player player, boolean shotFired) throws Exception {
        String msg = player.getX() + ";" + player.getY() + ";" + shotFired;
        byte[] data = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, SERVER_PORT);
        socket.send(packet);
    }

    public String receiveState() throws Exception {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength());
    }
}
