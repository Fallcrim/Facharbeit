package main;

import main.RSAHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable {

    private Socket clientSocket;
    private BufferedReader eingehend;
    private PrintWriter ausgehend;
    private boolean clientAktiv;
    private RSAHandler rsaHandler;
    private RSAHandler.SchluesselPaar schluesselPaar;

    @Override
    public void run() {
        try {
            this.clientSocket = new Socket("localhost", 2007);
            this.ausgehend = new PrintWriter(clientSocket.getOutputStream(), true);
            this.eingehend = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.rsaHandler = new RSAHandler();
            this.schluesselPaar = rsaHandler.erstelleSchluesselPaar(32);
            this.clientAktiv = true;

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
                Scanner eingabeLeser = new Scanner(System.in);

                String[] serverEinstellung = eingehend.readLine().split("#");

                BigInteger serverOeffentlicherSchluessel = new BigInteger(serverEinstellung[2]);
                BigInteger serverN = new BigInteger(serverEinstellung[3]);

//                System.out.println("Server Public Key = " + serverEinstellung[2]);
//                System.out.println("Server N value = " + serverEinstellung[3]);

                ausgehend.println("CFG#KEY#" + schluesselPaar.oeffentlicherSchluessel + "#" + schluesselPaar.n);

                System.out.print("Nickname: ");
                String clientNickname = eingabeLeser.nextLine();
                ausgehend.println(clientNickname);

                while (clientAktiv) {
                    String nachricht = eingabeLeser.nextLine();
                    String verschluesselteNachricht = rsaHandler.verschluesseln(nachricht, serverOeffentlicherSchluessel, serverN);
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
