package main;

import main.RSAHandler.SchluesselPaar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

            ExecutorService handlerThreadPool = Executors.newCachedThreadPool();
            EingabeHandler eingabeHandler = new EingabeHandler();
            handlerThreadPool.execute(eingabeHandler);
            AusgabeHandler ausgabeHandler = new AusgabeHandler();
            handlerThreadPool.execute(ausgabeHandler);
        } catch (IOException e) {
            // ignorieren
        }
    }

    private class EingabeHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader eingabeLeser = new BufferedReader(new InputStreamReader(System.in));

                String serverEinstellung = eingehend.readLine().split("#")[2];
                serverOeffentlicherSchluessel = new BigInteger(serverEinstellung);
                System.out.println("serverEinstellung = " + serverEinstellung);

                ausgehend.println("CFG#KEY#" + schluesselPaar.oeffentlicherSchluessel + "#" + schluesselPaar.n);

                while (clientAktiv) {
                    String nachricht = eingabeLeser.readLine();
                    String verschluesselteNachricht = rsaHandler.verschluesseln(nachricht, serverOeffentlicherSchluessel, schluesselPaar.n);
                    ausgehend.println("MSG#" + verschluesselteNachricht);
                }
            } catch (IOException e) {
                // TODO: verarbeiten
            }
        }
    }

    private class AusgabeHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader serverAusgabeLeser = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while (clientAktiv) {
                    String entschluesselteNachricht = rsaHandler.entschluesseln(serverAusgabeLeser.readLine(), schluesselPaar.privaterSchluessel, schluesselPaar.n);
                    System.out.println(entschluesselteNachricht);
                }
            } catch (IOException e) {
                // ignorieren
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
