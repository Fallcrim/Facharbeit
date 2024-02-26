package main;

import main.RSAHandler;
import main.RSAHandler.SchluesselPaar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client implements Runnable {

    private Socket clientSocket;
    private BufferedReader eingehend;
    private PrintWriter ausgehend;
    private boolean clientAktiv;
    private RSAHandler rsaHandler;
    private SchluesselPaar schluesselPaar;
    private BigInteger serverOeffentlicherSchluessel;

    @Override
    public void run() {
        try {
            this.clientSocket = new Socket("localhost", 2007);
            this.ausgehend = new PrintWriter(clientSocket.getOutputStream(), true);
            this.eingehend = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.rsaHandler = new RSAHandler();
            this.schluesselPaar = rsaHandler.erstelleSchluesselPaar(32);
        } catch (IOException e) {
            // ignorieren
        }
    }

    private class EingabeHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader eingabeReader = new BufferedReader(new InputStreamReader(System.in));

                String serverEinstellung = eingehend.readLine().split("CFG%")[0];
                serverOeffentlicherSchluessel = new BigInteger(serverEinstellung);

                ausgehend.println("CFG%KEY%" + schluesselPaar.oeffentlicherSchluessel + "%" + schluesselPaar.n);

                while (clientAktiv) {
                    String nachricht = eingabeReader.readLine();
                    String verschluesselteNachricht = rsaHandler.verschluesseln(nachricht, serverOeffentlicherSchluessel, schluesselPaar.n);
                    ausgehend.println("MSG%" + verschluesselteNachricht);
                }
            } catch (IOException e) {
                // TODO: verarbeiten
            }
        }
    }
}
