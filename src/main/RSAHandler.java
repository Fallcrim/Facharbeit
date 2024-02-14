package main;

import java.math.BigInteger;
import java.util.Random;

public class RSAHandler {

    private static class SchluesselErzeuger {
        /*
        Diese Klasse erstellt die Schlüsselpaare für die Verschlüsselung des Chatrooms
         */
        private BigInteger[] erstelleSchluesselPaar(int pLaengeInBits) {
            BigInteger p = generierePrimzahl(pLaengeInBits);
            BigInteger q = generierePrimzahl(pLaengeInBits);
            BigInteger n = p.multiply(q);
            // Eulers Totalitätsfunktion
            BigInteger phiN = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
            BigInteger oeffentlicherSchluessel = generiereOeffentlichenSchluessel(phiN);
            BigInteger privaterSchluessel = generierePrivatenSchluessel(oeffentlicherSchluessel, phiN);

            return new BigInteger[]{oeffentlicherSchluessel, privaterSchluessel, n};
        }

        public BigInteger zufaelligerBigInteger(BigInteger min, BigInteger max) {
            Random generator = new Random();
            BigInteger ergebnis = new BigInteger(max.bitLength(), generator);
            while (ergebnis.compareTo(min) >= 0) {
                ergebnis = new BigInteger(max.bitLength(), generator);
            }
            return ergebnis;
        }

        private BigInteger generierePrimzahl(int pLaengeInBits) {
            /*
            Generiert eine Primzahl mit der Länge von pLaengeBits
             */
            while (true) {
                BigInteger zufaelligeZahl = zufaelligeNBitLangeZahl(BigInteger.valueOf(pLaengeInBits));
                if (istPrimzahl(zufaelligeZahl)) {
                    return zufaelligeZahl;
                }
            }
        }

        private BigInteger moduloBigInteger(BigInteger a, int b) {
            return a.mod(BigInteger.valueOf(b));
        }

        private BigInteger zufaelligeNBitLangeZahl(BigInteger pN) {
        /*
        Erstellt eine zufällige Zahl mit der Länge von pN Bits
         */
            BigInteger max = BigInteger.TWO.pow(pN.intValue());
            BigInteger min = BigInteger.TWO.pow(pN.subtract(BigInteger.ONE).intValue()).add(BigInteger.ONE);
            return zufaelligerBigInteger(min, max);
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

        private BigInteger generierePrivatenSchluessel(BigInteger e, BigInteger pPhiN) {
            /*
            Generiert einen privaten Schlüssel für die Entschlüsselung von Nachrichten
             */
            while (true) {
                BigInteger d = zufaelligerBigInteger(e.add(BigInteger.valueOf(1)), pPhiN);
                if (d.gcd(pPhiN).equals(BigInteger.ONE)) {
                    return d;
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
            int nvBuchstabeASCII = (int) nvBuchstabe;
            ausgabe.append((char) (BigInteger.valueOf(nvBuchstabeASCII).pow(pOeffentlicherSchluessel.intValue()).mod(pN)).intValue());
        }
        return ausgabe.toString();
    }

    public String entschluesseln(String pVerschluesselteNachricht, BigInteger pPrivaterSchluessel, BigInteger pN) {
        /*
        Entschlüsselt eine Nachricht mit dem RSA Algorithmus
         */
        StringBuilder ausgabe = new StringBuilder();
        for (int i = 0; i < pVerschluesselteNachricht.length(); i++) {
            char nvBuchstabe = pVerschluesselteNachricht.charAt(i);
            int nvBuchstabeASCII = (int) nvBuchstabe;
            ausgabe.append(BigInteger.valueOf(nvBuchstabeASCII).pow(pPrivaterSchluessel.intValue()).mod(pN).intValue());
        }
        return ausgabe.toString();
    }

    public BigInteger[] erstelleSchluesselPaar(int pLaengeInBits) {
        SchluesselErzeuger erzeuger = new SchluesselErzeuger();
        return erzeuger.erstelleSchluesselPaar(pLaengeInBits);
    }
}
