import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

    private Socket clientSocket;
    private BufferedReader eingehend;
    private PrintWriter ausgehend;
    private boolean clientAktiv;

    @Override
    public void run() {
        try {
            this.clientSocket = new Socket("localhost", 2007);
            this.ausgehend = new PrintWriter(clientSocket.getOutputStream(), true);
            this.eingehend = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            // TODO: verarbeiten
        }
    }

    private class EingabeHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader eingabeReader = new BufferedReader(new InputStreamReader(System.in));
                while (clientAktiv) {
                    String nachricht = eingabeReader.readLine();

                }
            } catch (IOException e) {
                // TODO: verarbeiten
            }
        }
    }
}
