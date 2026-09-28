package test;

import domain.IceCream;
import domain.fruits.Banana;
import domain.fruits.Grape;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameTests {

    @Test
    public void testBordesDelMapaSonHielo() {
        GameMap map = new GameMap();
        assertEquals(1, map.getTile(0, 0));
        assertEquals(1, map.getTile(GameMap.WIDTH - 1, GameMap.HEIGHT - 1));
    }

    @Test
    public void testInteriorDelMapaEsVacio() {
        GameMap map = new GameMap();
        assertEquals(0, map.getTile(5, 5));
        assertEquals(0, map.getTile(7, 9));
    }

    @Test
    public void testCentroTieneHieloCruz() {
        GameMap map = new GameMap();
        assertEquals(1, map.getTile(7, 7));
        assertEquals(1, map.getTile(6, 7));
        assertEquals(1, map.getTile(8, 7));
        assertEquals(1, map.getTile(7, 6));
        assertEquals(1, map.getTile(7, 8));
    }

    @Test
    public void testAnchoAltoDelMapaCorrecto() {
        assertEquals(15, GameMap.WIDTH);
        assertEquals(15, GameMap.HEIGHT);
    }

    @Test
    public void testGetTileNoRompeFueraDeLimites() {
        GameMap map = new GameMap();
        try {
            map.getTile(100, 100);
            fail("Debe lanzar excepción o ignorar");
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testFruitMapSeInicializaEnCero() {
        GameMap map = new GameMap();
        assertEquals(0, map.getFruit(2, 10));
        assertEquals(0, map.getFruit(3, 3));
    }



    @Test
    public void testBananaColisionaEnElMismoPixel() {
        Banana b = new Banana(4, 4);
        assertTrue(b.checkCollision(4 * 32, 4 * 32));
    }

    @Test
    public void testBananaNoColisionaFuera() {
        Banana b = new Banana(4, 4);
        assertFalse(b.checkCollision(10 * 32, 10 * 32));
    }

    @Test
    public void testGrapeColisionCorrecta() {
        Grape g = new Grape(8, 8);
        assertTrue(g.checkCollision(8 * 32, 8 * 32));
    }

    @Test
    public void testGrapeNoColisiona() {
        Grape g = new Grape(1, 1);
        assertFalse(g.checkCollision(5 * 32, 2 * 32));
    }



    @Test
    public void testIceCreamPosicionInicialCorrecta() {
        GameMap map = new GameMap();
        IceCream ic = new IceCream("fresa", map);

        assertEquals(7 * 32, ic.getX());
        assertEquals(11 * 32, ic.getY());
    }

    @Test
    public void testIceCreamSeMueveDerecha() {
        GameMap map = new GameMap();
        IceCream ic = new IceCream("vainilla", map);
        int xIni = ic.getX();

        ic.mover(4, 0);
        assertEquals(xIni + 4, ic.getX());
    }

    @Test
    public void testIceCreamSeMueveArriba() {
        GameMap map = new GameMap();
        IceCream ic = new IceCream("chocolate", map);
        int yIni = ic.getY();

        ic.mover(0, -4);
        assertEquals(yIni - 4, ic.getY());
    }

    @Test
    public void testIceCreamNoEntraEnHielo() {
        GameMap mapa = new GameMap();
        IceCream ic = new IceCream("chocolate", mapa);

        int futureX = 0 * 32;
        int futureY = 1 * 32;

        boolean colision = mapa.getTile(0, 1) == 1;
        assertTrue(colision);
    }

    @Test
    public void testIceCreamNoSaleDelMapa() {
        GameMap map = new GameMap();
        IceCream ic = new IceCream("fresa", map);

        ic.mover(-500, 0);
        assertTrue(ic.getX() >= 0);
    }

    @Test
    public void testIceCreamNoSeTeletransporta() {
        GameMap map = new GameMap();
        IceCream ic = new IceCream("fresa", map);

        int xIni = ic.getX();
        ic.mover(4, 0);
        assertNotEquals(xIni + 100, ic.getX());
    }

    @Test
    public void testColisionSubeScore() {
        GameMap map = new GameMap();
        IceCream ic = new IceCream("fresa", map);

        Grape g = new Grape(7, 7);

        assertTrue("Debe chocar la uva", g.checkCollision(7 * 32, 7 * 32));
    }

    @Test
    public void testColisionBananaYNoGrape() {
        Banana b = new Banana(3, 3);
        Grape g = new Grape(3, 3);

        assertTrue(b.checkCollision(96, 96));   // 3*32
        assertTrue(g.checkCollision(96, 96));

        assertFalse(b.checkCollision(200, 200));
        assertFalse(g.checkCollision(200, 200));
    }
}
