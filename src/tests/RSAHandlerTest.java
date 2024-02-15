import main.RSAHandler;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;

class RSAHandlerTest {

    @org.junit.jupiter.api.Test
    void erstelleSchluesselPaar() {
        RSAHandler handler = new RSAHandler();
        BigInteger[] schluesselPaar = handler.erstelleSchluesselPaar(16);
        assertEquals(3, schluesselPaar.length);
    }

    @org.junit.jupiter.api.Test
    void verschluesseln() {
        RSAHandler handler = new RSAHandler();
        BigInteger[] schluesselPaar = handler.erstelleSchluesselPaar(4);
        BigInteger oSchluessel = schluesselPaar[0];
        BigInteger n = schluesselPaar[2];
        String vNachricht = handler.verschluesseln("Hello World!", oSchluessel, n);
        assertNotNull(vNachricht);
        System.out.println(vNachricht);
    }

    @org.junit.jupiter.api.Test
    void entschluesseln() {
        RSAHandler handler = new RSAHandler();
        BigInteger[] schluesselPaar = handler.erstelleSchluesselPaar(8);
        BigInteger oSchluessel = schluesselPaar[0];
        BigInteger pSchluessel = schluesselPaar[1];
        BigInteger n = schluesselPaar[2];
        String vNachricht = handler.verschluesseln("Hello World!", oSchluessel, n);
        assertNotNull(vNachricht);
        String eNachricht = handler.entschluesseln(vNachricht, pSchluessel, n);
        assertNotNull(eNachricht);
        assertEquals("Hello World!", eNachricht);
        System.out.println(eNachricht);
    }
}