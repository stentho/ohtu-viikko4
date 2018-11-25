package ohtu.verkkokauppa;

import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class KauppaTest {

    Pankki pankki;
    Viitegeneraattori viite;
    Varasto varasto;

    @Before
    public void setUp() {
        // luodaan ensin mock-oliot
        pankki = mock(Pankki.class);

        viite = mock(Viitegeneraattori.class);
        // määritellään että viitegeneraattori palauttaa viitten 42
        when(viite.uusi()).thenReturn(42);

        varasto = mock(Varasto.class);
    }

    @Test
    public void ostetaanYksiTuoteJaMetodiaTilisiirtoKutsutaanOikein() {

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        Kauppa k = new Kauppa(varasto, pankki, viite);

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        // oikealla asiakkaalla, tilinumerolla ja summalla.
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), anyString(), eq(5));
    }

    @Test
    public void ostetaanKaksiEriTuotettaJaMetodiaTilisiirtoKutsutaanOikein() {

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        // määritellään että tuote numero 2 on ananas jonka hinta on 10 ja saldo 5
        when(varasto.saldo(2)).thenReturn(5);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(1, "maito", 10));

        // sitten testattava kauppa 
        Kauppa k = new Kauppa(varasto, pankki, viite);

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.lisaaKoriin(2);     // ostetaan tuotetta numero 2 eli ananasta
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        // oikealla asiakkaalla, tilinumerolla ja summalla.
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), anyString(), eq(15));
    }

    @Test
    public void ostetaanKaksiSamaaTuotettaJaMetodiaTilisiirtoKutsutaanOikein() {

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        Kauppa k = new Kauppa(varasto, pankki, viite);

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa taas
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        // oikealla asiakkaalla, tilinumerolla ja summalla.
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), anyString(), eq(10));
    }

    @Test
    public void ostetaanKaksiEriTuotettaJoistaYksiLoppuJaMetodiaTilisiirtoKutsutaanOikein() {

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));
        // määritellään että tuote numero 2 on ananas jonka hinta on 10 ja saldo 0 (loppu)
        when(varasto.saldo(2)).thenReturn(0);
        when(varasto.haeTuote(2)).thenReturn(new Tuote(1, "maito", 10));

        // sitten testattava kauppa 
        Kauppa k = new Kauppa(varasto, pankki, viite);

        // tehdään ostokset
        k.aloitaAsiointi();
        k.lisaaKoriin(1);     // ostetaan tuotetta numero 1 eli maitoa
        k.lisaaKoriin(2);     // ostetaan tuotetta numero 2 eli ananasta, loppu
        k.tilimaksu("pekka", "12345");

        // sitten suoritetaan varmistus, että pankin metodia tilisiirto on kutsuttu
        // oikealla asiakkaalla, tilinumerolla ja summalla.
        verify(pankki).tilisiirto(eq("pekka"), anyInt(), eq("12345"), anyString(), eq(5));
    }

    @Test
    public void metodinAloitaAsiointiKutsuminenNollaaEdellisenOstoksenTiedot() {

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        Kauppa kauppa = new Kauppa(varasto, pankki, viite);

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1); // lisätään tuote 1 eli maito
        kauppa.tilimaksu("pekka", "12345");

        // Tarkistetaan, että summa on oikea
        verify(pankki, times(1)).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(5));

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1); // lisätään jälleen tuote 1 eli maito
        kauppa.tilimaksu("pekka", "12345");

        // Tarkistetaan, että summa on oikea, edellisen ostoksen ei pitäisi näkyä 
        // uudessa ostoksessa
        verify(pankki, times(2)).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(5));
    }

    @Test
    public void pyydetaanUusiViiteJokaiseenMaksuun() {

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        Kauppa kauppa = new Kauppa(varasto, pankki, viite);

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");

        // tarkistetaan että tässä vaiheessa viitegeneraattorin metodia uusi()
        // on kutsuttu kerran
        verify(viite, times(1)).uusi();

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");

        // tarkistetaan että tässä vaiheessa viitegeneraattorin metodia uusi()
        // on kutsuttu kaksi kertaa
        verify(viite, times(2)).uusi();

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1);
        kauppa.tilimaksu("pekka", "12345");

        // tarkistetaan että tässä vaiheessa viitegeneraattorin metodia uusi()
        // on kutsuttu kolme kertaa        
        verify(viite, times(3)).uusi();
    }

    @Test
    public void poistaKoristaOikein() {

        // määritellään että tuote numero 1 on maito jonka hinta on 5 ja saldo 10
        when(varasto.saldo(1)).thenReturn(10);
        when(varasto.haeTuote(1)).thenReturn(new Tuote(1, "maito", 5));

        // sitten testattava kauppa 
        Kauppa kauppa = new Kauppa(varasto, pankki, viite);

        kauppa.aloitaAsiointi();
        kauppa.lisaaKoriin(1); // lisätään tuote 1, maito
        kauppa.poistaKorista(1); // poistetaan korista tuote 1
        kauppa.tilimaksu("pekka", "12345");

        // Tarkistetaan, että summa on oikea, korissa pitäisi olla yksi maito
        verify(pankki, times(1)).tilisiirto(anyString(), anyInt(), anyString(), anyString(), eq(0));
    }
}
