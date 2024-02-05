import java.net.Proxy;
import java.net.Socket;

public class Server implements Runnable {
    /**
     * Die Server Klasse, die den Server definiert, der alle Client Instanzen miteinander verbindet
     */

    Socket serverSocket;

    @Override
    public void run() {
        serverSocket = new Socket(Proxy.NO_PROXY);
    }

    private class ClientHandler implements Runnable {

        @Override
        public void run() {

        }
    }
}
