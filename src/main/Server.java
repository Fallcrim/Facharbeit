package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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

    public Server() {
        this.verbundeneClients = new ArrayList<>();
        this.verwendeteNicks = new ArrayList<>();
        this.serverAn = true;
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(2007);
            ExecutorService clientThreadPool = Executors.newCachedThreadPool();
            this.starteKonsole();
            System.out.println("main.Server gestartet...");
            while (this.serverAn) {
                Socket neuerClient = this.serverSocket.accept();
                ClientHandler neuerClientHandler = new ClientHandler(neuerClient);
                this.verbundeneClients.add(neuerClientHandler);
                clientThreadPool.execute(neuerClientHandler);
            }
        } catch (Exception e) {
            herunterfahren("main.Server abgestürzt --> " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void starteKonsole() {
        Thread konsolenThread = new Thread(new Konsole());
        konsolenThread.start();
    }

    public void sendeAnAlle(String pNachricht) {
        for (ClientHandler alleHandler : this.verbundeneClients) {
            if (alleHandler != null) {
                alleHandler.sendeNachricht(pNachricht);
            }
        }
    }

    public void herunterfahren(String pGrund) {
        if (!this.serverSocket.isClosed()) {
            try {
                this.serverAn = false;
                this.sendeAnAlle("Der main.Server wird heruntergefahren. Grund: " + pGrund);
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

        public ClientHandler(Socket pVerbundenerClient) {
            this.verbundenerClient = pVerbundenerClient;
        }

        @Override
        public void run() {
            try {
                this.ausgehend = new PrintWriter(this.verbundenerClient.getOutputStream(), true);
                this.eingehend = new BufferedReader(new InputStreamReader(this.verbundenerClient.getInputStream()));

                // Frage den client nach seinem gewünschten Nickname/Akronym
                this.ausgehend.println("NICK");
                String clientNickname = this.nicknameAnpassen(this.eingehend.readLine());
                if (verwendeteNicks.contains(clientNickname)) {
                    ausgehend.println("ERR%Nickname bereits in Verwendung! Wähle einen neuen.");
                    this.verbindungSchliessen();
                }
                System.out.println(clientNickname + " wurde mit dem Chat verbunden!");
                // Kündige den neuen Benutzer im Chat an
                sendeAnAlle(clientNickname + " ist dem Chat beigetreten.");
                // Verarbeite neue Nachrichten des verbundenen Benutzers
                String neueNachricht = this.eingehend.readLine();
                while (neueNachricht != null) {
                    if (neueNachricht.equals("/quit")) { // Befehl, wenn der Nutzer den Chat verlässt
                        this.verbindungSchliessen();
                        sendeAnAlle(clientNickname + " hat den Chat verlassen.");
                        return;
                    } else {
                        sendeAnAlle(neueNachricht);
                    }
                    neueNachricht = this.eingehend.readLine();
                }
            } catch (IOException e) {
                // ignorieren
            }
        }

        private String nicknameAnpassen(String pNicknameRoh) {
            String angepassterNickname = pNicknameRoh;
            angepassterNickname = angepassterNickname.trim();
            return angepassterNickname;
        }

        public void sendeNachricht(String pNachricht) {
            this.ausgehend.println("MSG%" + pNachricht);
        }

        public void verbindungSchliessen() {
            if (!this.verbundenerClient.isClosed()) {
                try {
                    this.ausgehend.println("SIG%TERM"); // bringt den main.Client dazu, die Verbindung auf seiner Seite zu schließen
                    // Schließen des main.Client Sockets & der In-/Output-Streams
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
