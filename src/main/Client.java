package main;

import main.RSAHandler.SchluesselPaar;

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
    private SchluesselPaar schluesselPaar;
    private BigInteger serverOeffentlicherSchluessel;
    private BigInteger serverN;

    @Override
    public void run() {
        try {
            this.clientSocket = new Socket("localhost", 2007);
            this.ausgehend = new PrintWriter(clientSocket.getOutputStream(), true);
            this.eingehend = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.rsaHandler = new RSAHandler();
            this.schluesselPaar = rsaHandler.erstelleSchluesselPaar(8);
            System.out.println("pschluessel = " + schluesselPaar.oeffentlicherSchluessel);
            System.out.println("n = " + schluesselPaar.n);
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
            /*

             */
            Scanner eingabeLeser = new Scanner(System.in);

            System.out.print("Nickname: ");
            String clientNickname = eingabeLeser.nextLine();
//            ausgehend.println("CFG#NCK#" + clientNickname);
            ausgehend.println(clientNickname);

            while (clientAktiv) {
                String nachricht = eingabeLeser.nextLine();
                String verschluesselteNachricht = rsaHandler.verschluesseln(nachricht, serverOeffentlicherSchluessel, serverN);
                ausgehend.println("MSG#" + verschluesselteNachricht);
            }
        }
    }

    private class AusgabeHandler implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader serverAusgabeLeser = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                while (clientAktiv) {
                    String empfangeneNachricht = serverAusgabeLeser.readLine();
                    if (empfangeneNachricht.startsWith("CFG#")) {
                        // Konfiguration des Schlüsselaustauschs
                        String[] serverEinstellung = empfangeneNachricht.split("#");

                        serverOeffentlicherSchluessel = new BigInteger(serverEinstellung[2]);
                        serverN = new BigInteger(serverEinstellung[3]);
                        System.out.println("serverEinstellung pkey = " + serverEinstellung[2]);
                        System.out.println("serverEinstellung n = " + serverEinstellung[3]);
                        System.out.println("Server Schlüssel erhalten!");

                        ausgehend.println("CFG#KEY#" + schluesselPaar.oeffentlicherSchluessel + "#" + schluesselPaar.n);
                    }
                    String[] nachrichtElemente = empfangeneNachricht.split("#", 2);
                    if (nachrichtElemente[0].equals("MSG")) {
                        // Verarbeitung einer Textnachricht
                        if (nachrichtElemente[1].startsWith("TEST")) {
                            System.out.println(nachrichtElemente[1]);
                            continue;
                        }
                        String entschluesselteNachricht = rsaHandler.entschluesseln(nachrichtElemente[1], schluesselPaar.privaterSchluessel, schluesselPaar.n);
                        System.out.println(entschluesselteNachricht);
                    } else if (nachrichtElemente[0].equals("SIG")) {
                        // Verarbeitung von Signalen des Servers
                        if (nachrichtElemente[1].equals("TERM")) {
                            // Client stoppt
                            clientAktiv = false;
                            clientSocket.close();
                            eingehend.close();
                            ausgehend.close();
                            System.exit(1);
                        }
                    }
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
