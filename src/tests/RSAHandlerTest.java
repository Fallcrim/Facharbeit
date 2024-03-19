import main.RSAHandler;
import main.RSAHandler.SchluesselPaar;

import static org.junit.jupiter.api.Assertions.*;

class RSAHandlerTest {

    @org.junit.jupiter.api.Test
    void erstelleSchluesselPaar() {
        RSAHandler handler = new RSAHandler();
        SchluesselPaar schluesselPaar = handler.erstelleSchluesselPaar(32);
        assertNotNull(schluesselPaar);
    }

    @org.junit.jupiter.api.Test
    void verschluesseln() {
        RSAHandler handler = new RSAHandler();
        SchluesselPaar schluesselPaar = handler.erstelleSchluesselPaar(32);
        String vNachricht = handler.verschluesseln("Hello World!", schluesselPaar.oeffentlicherSchluessel, schluesselPaar.n);
        assertNotNull(vNachricht);
        System.out.println(vNachricht);
    }

    @org.junit.jupiter.api.Test
    void entschluesseln() {
        RSAHandler handler = new RSAHandler();
        SchluesselPaar schluesselPaar = handler.erstelleSchluesselPaar(16);
        System.out.println("schluesselPaar.oeffentlicherSchluessel = " + schluesselPaar.oeffentlicherSchluessel);
        System.out.println("schluesselPaar.privaterSchluessel = " + schluesselPaar.privaterSchluessel);
        String vNachricht = handler.verschluesseln("abcdefghijklmnopqrstuvwxyz", schluesselPaar.oeffentlicherSchluessel, schluesselPaar.n);
        System.out.println("vNachricht = " + vNachricht);
        assertNotNull(vNachricht);
        String eNachricht = handler.entschluesseln(vNachricht, schluesselPaar.privaterSchluessel, schluesselPaar.n);
        assertNotNull(eNachricht);
        assertEquals("abcdefghijklmnopqrstuvwxyz", eNachricht);
        System.out.println("eNachricht = " + eNachricht);
    }
}