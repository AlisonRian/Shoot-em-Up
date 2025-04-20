import java.net.InetAddress;

public class ClientInfo {
     InetAddress address;
     int port;

    public ClientInfo(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public boolean equals(InetAddress addr, int p) {
        return this.address.equals(addr) && this.port == p;
    }

    @Override
    public String toString() {
        return address.toString() + ":" + port;
    }
}