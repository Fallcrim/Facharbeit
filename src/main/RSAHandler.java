package main;

import java.math.BigInteger;
import java.util.Random;

public class RSAHandler {

    private static class SchluesselErzeuger {
        /*
        Diese Klasse erstellt die Schlüsselpaare für die Verschlüsselung des Chatrooms
         */
        private BigInteger[] erstelleSchluesselPaar(int pLaengeInBits) {
            BigInteger p = zufaelligePrimzahl(pLaengeInBits);
            BigInteger q = zufaelligePrimzahl(pLaengeInBits);
            BigInteger n = p.multiply(q);

            // Eulers Totalitätsfunktion
            BigInteger phiN = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));

            // Erstellung der Schlüssel
            BigInteger oeffentlicherSchluessel = generiereOeffentlichenSchluessel(phiN);
            BigInteger privaterSchluessel = oeffentlicherSchluessel.modInverse(phiN);

            return new BigInteger[]{oeffentlicherSchluessel, privaterSchluessel, n};
        }

        private BigInteger zufaelligePrimzahl(int pBitLaenge) {
            Random generator = new Random();
            BigInteger primzahl = BigInteger.probablePrime(pBitLaenge, generator);
            while (!istPrimzahl(primzahl)) {
                primzahl = BigInteger.probablePrime(pBitLaenge, generator);
            }
            return primzahl;
        }

        public BigInteger zufaelligerBigInteger(BigInteger min, BigInteger max) {
            Random generator = new Random();
            BigInteger ergebnis = new BigInteger(max.bitLength(), generator);
            while (ergebnis.compareTo(max) >= 0 && ergebnis.compareTo(min) <= 0) {
                ergebnis = new BigInteger(max.bitLength(), generator);
            }
            return ergebnis;
        }

//        private BigInteger generierePrimzahl(int pLaengeInBits) {
//            /*
//            Generiert eine Primzahl mit der Länge von pLaengeBits
//             */
//            while (true) {
//                BigInteger zufaelligeZahl = zufaelligeNBitLangeZahl(BigInteger.valueOf(pLaengeInBits));
//                if (istPrimzahl(zufaelligeZahl)) {
//                    return zufaelligeZahl;
//                }
//            }
//        }

        private BigInteger moduloBigInteger(BigInteger a, int b) {
            return a.mod(BigInteger.valueOf(b));
        }

//        private BigInteger zufaelligeNBitLangeZahl(BigInteger pN) {
//        /*
//        Erstellt eine zufällige Zahl mit der Länge von pN Bits
//         */
//            BigInteger max = BigInteger.TWO.pow(pN.intValue());
//            BigInteger min = BigInteger.TWO.pow(pN.subtract(BigInteger.ONE).intValue()).add(BigInteger.ONE);
//            return zufaelligerBigInteger(min, max);
//        }

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
            BigInteger quadratWurzel = pZahl.sqrt();
            for (int zaehler = 5; quadratWurzel.compareTo(BigInteger.valueOf(zaehler)) > 0; zaehler += 6) {
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
            char nvBuchstabe = pNachricht.charAt(i);
            BigInteger vBuchstabe = BigInteger.valueOf(nvBuchstabe).modPow(pOeffentlicherSchluessel, pN);
            ausgabe.append((char) vBuchstabe.intValue());
        }
        return ausgabe.toString();
    }

    public String entschluesseln(String pVerschluesselteNachricht, BigInteger pPrivaterSchluessel, BigInteger pN) {
        /*
        Entschlüsselt eine Nachricht mit dem RSA Algorithmus
         */
        StringBuilder ausgabe = new StringBuilder();
        for (int i = 0; i < pVerschluesselteNachricht.length(); i++) {
            char vBuchstabe = pVerschluesselteNachricht.charAt(i);
            BigInteger nvBuchstabe = BigInteger.valueOf(vBuchstabe).modPow(pPrivaterSchluessel, pN);
            ausgabe.append((char) nvBuchstabe.intValue());
        }
        return ausgabe.toString();
    }

    public BigInteger[] erstelleSchluesselPaar(int pLaengeInBits) {
        SchluesselErzeuger erzeuger = new SchluesselErzeuger();
        return erzeuger.erstelleSchluesselPaar(pLaengeInBits);
    }
}
