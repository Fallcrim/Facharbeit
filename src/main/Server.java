package main;
import main.RSAHandler.SchluesselPaar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    /**
     * Die Server Klasse, die den Server definiert, der alle Client Instanzen miteinander verbindet
     */

    private ServerSocket serverSocket;
    private final ArrayList<ClientHandler> verbundeneClients;
    private final ArrayList<String> verwendeteNicks;
    private boolean serverAn;
    private final RSAHandler rsaHandler;
    private final SchluesselPaar schluesselPaar;

    public Server() {
        this.verbundeneClients = new ArrayList<>();
        this.verwendeteNicks = new ArrayList<>();
        this.serverAn = true;
        this.rsaHandler = new RSAHandler();
        this.schluesselPaar = rsaHandler.erstelleSchluesselPaar(8);
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(2007);
            // Erstellung des Threadpools für die Client-Threads
            ExecutorService clientThreadPool = Executors.newCachedThreadPool();
            // Konsole starten
            this.starteKonsole();
            System.out.println("Server gestartet...");
            // Haupt-Schleife des Servers
            while (this.serverAn) {
                // eingehende Verbindung akzeptieren
                Socket neuerClient = this.serverSocket.accept();
                // für den neu verbundenen Client einen neuen Handler erstellen und diesen den vorhandenen Handlern in this.verbundeneClients hinzufügen
                ClientHandler neuerClientHandler = new ClientHandler(neuerClient);
                this.verbundeneClients.add(neuerClientHandler);
                // den ClientHandler als Thread ausführen
                clientThreadPool.execute(neuerClientHandler);
            }
        } catch (Exception e) {
            herunterfahren("Server abgestürzt --> " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void starteKonsole() {
        Thread konsolenThread = new Thread(new Konsole());
        konsolenThread.start();
    }

    public void sendeAnAlle(String pNachricht, String senderNickname) {
        for (ClientHandler alleHandler : this.verbundeneClients) {
            if (alleHandler != null) {
                alleHandler.sendeNachricht(senderNickname + " : " + pNachricht);
            }
        }
    }

    public void herunterfahren(String pGrund) {
        if (!this.serverSocket.isClosed()) {
            try {
                this.serverAn = false;
                this.sendeAnAlle("Der Server wird heruntergefahren. Grund: " + pGrund, "[SERVER]");
                for (ClientHandler alleClients : this.verbundeneClients) {
                    alleClients.verbindungSchliessen();
                }
                this.serverSocket.close();
            } catch (IOException e) {
                // ignorieren
            }
        }
    }

    private class Konsole implements Runnable {
        /*
        Die Klasse Konsole bietet auf Serverebene eine kleine Eingabestelle für den Administrator
         */
        private final BufferedReader eingabeReader;

        public Konsole() {
            this.eingabeReader = new BufferedReader(new InputStreamReader(System.in));
        }

        @Override
        public void run() {
            while (serverAn) {
                try {
                    String eingegebenerBefehl = this.eingabeReader.readLine();
                    // Den Befehl aus der Konsole verarbeiten
                    if (eingegebenerBefehl.startsWith("kick")) {
                        String[] befehlArgumente = eingegebenerBefehl.split(" ", 2);
                        String nickname = befehlArgumente[1];
                        if (verwendeteNicks.contains(nickname)) {
                            ClientHandler clientZuKicken = verbundeneClients.get(verwendeteNicks.indexOf(nickname));
                            clientZuKicken.sendeNachricht("[ADMIN] Deine Verbindung wurde durch einen Administrator geschlossen.");
                            clientZuKicken.verbindungSchliessen();
                        } else {
                            System.out.println("Nickname ist nicht verbunden!");
                        }
                    } else if (eingegebenerBefehl.equals("herunterfahren")) {
                        herunterfahren("main.Server wurde von einem Administrator heruntergefahren.");
                        serverAn = false;
                    }
                } catch (Exception e) {
                    // Ignorieren
                }
            }
        }
    }

    private class ClientHandler implements Runnable {

        private final Socket verbundenerClient;
        private BufferedReader eingehend;
        private PrintWriter ausgehend;
        private BigInteger oeffentlicherSchluessel;
        private BigInteger n;

        public ClientHandler(Socket pVerbundenerClient) {
            this.verbundenerClient = pVerbundenerClient;
        }

        @Override
        public void run() {
            try {
                this.ausgehend = new PrintWriter(this.verbundenerClient.getOutputStream(), true);
                this.eingehend = new BufferedReader(new InputStreamReader(this.verbundenerClient.getInputStream()));

                sendeRoheNachricht("CFG#KEY#" + schluesselPaar.oeffentlicherSchluessel + "#" + schluesselPaar.n);
                String[] verschluesselungsDaten = this.eingehend.readLine().split("#");
                this.oeffentlicherSchluessel = new BigInteger(verschluesselungsDaten[2]);
                this.n = new BigInteger(verschluesselungsDaten[3]);
                System.out.println("pkey = " + verschluesselungsDaten[2]);
                System.out.println("n = " + verschluesselungsDaten[3]);

                // Frage den client nach seinem gewünschten Nickname/Akronym
                String clientNickname = this.nicknameAnpassen(this.eingehend.readLine());
                if (verwendeteNicks.contains(clientNickname)) {
                    ausgehend.println("ERR#Nickname bereits in Verwendung! Wähle einen neuen.");
                    this.verbindungSchliessen();
                }
                verwendeteNicks.add(clientNickname);
                System.out.println(clientNickname + " wurde mit dem Chat verbunden!");

                // Kündige den neuen Benutzer im Chat an
                sendeAnAlle(clientNickname + " ist dem Chat beigetreten.", "[SERVER]");

                // Verarbeite neue Nachrichten des verbundenen Benutzers
                String neueNachricht = this.eingehend.readLine();
                while (neueNachricht != null) {
                    String[] nachrichtElemente = neueNachricht.split("#", 2);
                    if (nachrichtElemente[0].equals("MSG")) {
                        String entschluesselteNeueNachricht = rsaHandler.entschluesseln(nachrichtElemente[1], schluesselPaar.privaterSchluessel, schluesselPaar.n);
                        if (entschluesselteNeueNachricht.equals("/quit")) {
                            sendeAnAlle(clientNickname + " verlässt den Chat.", "[SERVER]");
                            verbindungSchliessen();
                        } else if (entschluesselteNeueNachricht.equals("%TST%")) {
                            sendeRoheNachricht("MSG#TEST! Dies ist eine Testnachricht! Sie ist unverschlüsselt!");
                        }
                        sendeAnAlle(entschluesselteNeueNachricht, clientNickname);
                    }
                    neueNachricht = this.eingehend.readLine();
                }
            } catch (IOException e) {
                // ignorieren
            }
        }

        private String nicknameAnpassen(String pNicknameRoh) {
            /*
            Formatiert den vom Client angegebenen Nicknamen
             */
            String angepassterNickname = pNicknameRoh;
            angepassterNickname = angepassterNickname.trim();
            return angepassterNickname;
        }

        public void sendeNachricht(String pNachricht) {
            /*
            Sendet eine verschlüsselte Nachricht, die als Textnachricht interpretiert werden soll
             */
            String vNachricht = rsaHandler.verschluesseln(pNachricht, this.oeffentlicherSchluessel, this.n);
            this.ausgehend.println("MSG#" + vNachricht);
        }

        public void sendeRoheNachricht(String pNachricht) {
            /*
            Sendet eine Nachricht unverschlüsselt; wird verwendet, um Signale und Schlüssel zu senden
             */
            this.ausgehend.println(pNachricht);
        }

        public void verbindungSchliessen() {
            if (!this.verbundenerClient.isClosed()) {
                try {
                    sendeRoheNachricht("SIG#TERM"); // bringt den Client dazu, die Verbindung auf seiner Seite zu schließen
                    // Schließen des Client Sockets & der In-/Output-Streams
                    this.verbundenerClient.close();
                    this.eingehend.close();
                    this.ausgehend.close();
                    verbundeneClients.remove(this);
                } catch (IOException e) {
                    // Ignorieren
                }
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
