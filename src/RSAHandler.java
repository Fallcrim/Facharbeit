import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RSAHandler {

    private static class SchluesselErzeuger {
        /*
        Diese Klasse erstellt die Schlüsselpaare für die Verschlüsselung des Chatrooms
         */
        public int[] erstelleSchluesselPaar() {
            int p = generierePrimzahl(128);
            int q = generierePrimzahl(128);
            long n = (long) p * q;
            // Eulers Totalitätsfunktion
            int phiN = (p - 1) * (q - 1);
            int oeffentlicherSchluessel = generiereOeffentlichenSchluessel(phiN);
            int privaterSchluessel = generierePrivatenSchluessel(oeffentlicherSchluessel, phiN);

            return new int[]{oeffentlicherSchluessel, privaterSchluessel};
        }

        private int generierePrimzahl(int pLaengeInBits) {
            /*
            Generiert eine Primzahl mit der Länge von pLaengeBits
             */
            while (true) {
                int zufaelligeZahl = zufaelligeNBitLangeZahl(pLaengeInBits);
                if (istPrimzahl(zufaelligeZahl)) {
                    return zufaelligeZahl;
                }
            }
        }

        private int zufaelligeNBitLangeZahl(int pN) {
        /*
        Erstellt eine zufällige Zahl mit der Länge von pN Bits
         */
            int max = (int) Math.pow(2, pN);
            int min = (int) Math.pow(2, pN - 1) + 1;
            return ThreadLocalRandom.current().nextInt(min, max);
        }

        private boolean istPrimzahl(int pZahl) {
        /*
        Prüft, ob eine gegebene Zahl eine Primzahl ist
         */
            if (pZahl <= 3) {
                return pZahl == 2 || pZahl == 3;
            }
            if (pZahl % 2 == 0 || pZahl % 3 == 0) {
                return false;
            }
            double quadratWurzel = Math.sqrt(pZahl);
            for (int zaehler = 5; zaehler <= quadratWurzel; zaehler += 6) {
                if (pZahl % zaehler == 0 || pZahl % (zaehler + 2) == 0) {
                    return false;
                }
            }
            return true;
        }

        private int groessterGemeinsamerTeiler(int a, int b) {
            /*
            Ermittelt den ggT von a und b
             */
            if (b == 0) return a;
            return groessterGemeinsamerTeiler(b, a % b);
        }

        private int generiereOeffentlichenSchluessel(int pPhiN) {
            /*
            Generiert einen öffentlichen Schlüssel für die Verschlüsselung von Nachrichten
             */
            Random generator = new Random();
            while (true) {
                int e = generator.nextInt(pPhiN - 2) + 2;
                if (groessterGemeinsamerTeiler(e, pPhiN) == 1) {
                    return e;
                }
            }
        }

        private int generierePrivatenSchluessel(int e, int pPhiN) {
            /*
            Generiert einen privaten Schlüssel für die Entschlüsselung von Nachrichten
             */
            Random generator = new Random();
            while (true) {
                int d = generator.nextInt(e + 1, pPhiN);
                if (groessterGemeinsamerTeiler(d, pPhiN) == 1) {
                    return d;
                }
            }
        }
    }
}
