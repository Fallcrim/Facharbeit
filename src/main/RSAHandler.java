package main;

import java.math.BigInteger;
import java.util.Random;

public class RSAHandler {


    public static class SchluesselPaar {
        public BigInteger oeffentlicherSchluessel;
        public BigInteger privaterSchluessel;
        public BigInteger n;

        public SchluesselPaar(BigInteger[] pSchluesselPaar) {
            this.oeffentlicherSchluessel = pSchluesselPaar[0];
            this.privaterSchluessel = pSchluesselPaar[1];
            this.n = pSchluesselPaar[2];
        }
    }


    private static class SchluesselErzeuger {
        /*
        Diese Klasse stellt die Schlüsselpaare für die Verschlüsselung des Chatrooms dar
         */
        private SchluesselPaar erstelleSchluesselPaar(int pLaengeInBits) {
            BigInteger p = zufaelligePrimzahl(pLaengeInBits);
            BigInteger q = zufaelligePrimzahl(pLaengeInBits);
            BigInteger n = p.multiply(q);

            // Eulers Totalitätsfunktion
            BigInteger phiN = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

            // Erstellung der Schlüssel
            BigInteger oeffentlicherSchluessel = generiereOeffentlichenSchluessel(phiN);
            BigInteger privaterSchluessel = oeffentlicherSchluessel.modInverse(phiN);

            return new SchluesselPaar(new BigInteger[]{oeffentlicherSchluessel, privaterSchluessel, n});
        }

        private BigInteger zufaelligePrimzahl(int pBitLaenge) {
            /*
            Generiert eine zufällige Primzahl mit der Größe pBitLaenge
             */
            Random generator = new Random();
            BigInteger primzahl = BigInteger.probablePrime(pBitLaenge, generator);
            while (!istPrimzahl(primzahl)) {
                primzahl = BigInteger.probablePrime(pBitLaenge, generator);
            }
            return primzahl;
        }

        public BigInteger zufaelligerBigInteger(BigInteger min, BigInteger max) {
            /*
            Generiert einen zufälligen BigInteger im gegebenen Interval
             */
            Random generator = new Random();
            BigInteger ergebnis = new BigInteger(max.bitLength(), generator);
            while (ergebnis.compareTo(max) >= 0 && ergebnis.compareTo(min) <= 0) {
                ergebnis = new BigInteger(max.bitLength(), generator);
            }
            return ergebnis;
        }

        private BigInteger moduloBigInteger(BigInteger a, int b) {
            /*
            Modulo-Operation für einen BigInteger und einen Integer
             */
            return a.mod(BigInteger.valueOf(b));
        }

        private boolean istPrimzahl(BigInteger pZahl) {
            /*
            Prüft, ob eine gegebene Zahl eine Primzahl ist
             */
            if (pZahl.compareTo(BigInteger.valueOf(3)) <= 0) {
                return false;
            }
            if (moduloBigInteger(pZahl, 2).compareTo(BigInteger.ZERO) == 0 || moduloBigInteger(pZahl, 3).compareTo(BigInteger.ZERO) == 0) {
                return false;
            }
            for (int zaehler = 5; pZahl.sqrt().compareTo(BigInteger.valueOf(zaehler)) > 0; zaehler += 6) {
                if (moduloBigInteger(pZahl, zaehler).compareTo(BigInteger.ZERO) == 0 || moduloBigInteger(pZahl, zaehler + 2).compareTo(BigInteger.ZERO) == 0) {
                    return false;
                }
            }
            return true;
        }

        private BigInteger generiereOeffentlichenSchluessel(BigInteger pPhiN) {
            /*
            Generiert einen öffentlichen Schlüssel für die Verschlüsselung von Nachrichten
             */
            while (true) {
                BigInteger e = zufaelligerBigInteger(BigInteger.ZERO, pPhiN.subtract(BigInteger.TWO)).add(BigInteger.TWO);
                if (e.gcd(pPhiN).equals(BigInteger.ONE)) {
                    return e;
                }
            }
        }
    }

    public String verschluesseln(String pNachricht, BigInteger pOeffentlicherSchluessel, BigInteger pN) {
        /*
        Verschlüsselt eine Nachricht mit dem RSA Algorithmus
         */
        StringBuilder ausgabe = new StringBuilder();
        for (int i = 0; i < pNachricht.length(); i++) {
            char c = pNachricht.charAt(i);
            BigInteger m = BigInteger.valueOf((int) c);
            BigInteger vBuchstabeASCII = m.modPow(pOeffentlicherSchluessel, pN);
            ausgabe.append(vBuchstabeASCII.intValue()).append(" ");
        }
        return ausgabe.toString();
    }

    public String entschluesseln(String pVerschluesselteNachricht, BigInteger pPrivaterSchluessel, BigInteger pN) {
        /*
        Entschlüsselt eine Nachricht mit dem RSA Algorithmus
         */
        StringBuilder ausgabe = new StringBuilder();
        String[] vBuchstaben = pVerschluesselteNachricht.split(" ");
        for (String vBuchstabe : vBuchstaben) {
            BigInteger vBuchstabeASCII = new BigInteger(vBuchstabe).abs();
            BigInteger eBuchstabeASCII = vBuchstabeASCII.modPow(pPrivaterSchluessel, pN);
            char eBuchstabe = (char) eBuchstabeASCII.intValue();
            ausgabe.append(eBuchstabe);
        }
        return ausgabe.toString();
    }

    public SchluesselPaar erstelleSchluesselPaar(int pLaengeInBits) {
        /*
        Erstellt ein Schlüsselpaar mit Schlüsseln der Länge pLaengeInBits
         */
        SchluesselErzeuger erzeuger = new SchluesselErzeuger();
        return erzeuger.erstelleSchluesselPaar(pLaengeInBits);
    }
}
