package persistence;

import domain.Enemigos;
import domain.GameMap;
import domain.IceCream;
import domain.IceCreamExceptions;
import domain.enemies.CalamarNaranja;
import domain.enemies.Maceta;
import domain.enemies.Narval;
import domain.enemies.Troll;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class IceCreamPersistence {


    public static void saveState(File file,String mode,int level,int timeRemaining,int score,String flavor,GameMap mapa,IceCream helado,List<Enemigos> enemigos) throws IceCreamExceptions {
        if (file == null) throw new IceCreamExceptions("Archivo destino nulo");
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            w.write("mode=" + safe(mode)); w.newLine();
            w.write("level=" + level); w.newLine();
            w.write("timeRemaining=" + timeRemaining); w.newLine();
            w.write("score=" + score); w.newLine();
            w.write("flavor=" + safe(flavor)); w.newLine();
            w.write("iceX=" + (helado != null ? helado.getX() : 0)); w.newLine();
            w.write("iceY=" + (helado != null ? helado.getY() : 0)); w.newLine();

            int count = enemigos != null ? enemigos.size() : 0;
            w.write("enemiesCount=" + count); w.newLine();
            if (enemigos != null) {
                for (Enemigos e : enemigos) {
                    String type = e.getClass().getSimpleName();
                    w.write(String.format(Locale.ROOT, "enemy=%s,%d,%d", safe(type), e.getX(), e.getY()));
                    w.newLine();
                }
            }

            w.write("tiles:"); w.newLine();
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < GameMap.WIDTH; x++) {
                    sb.append(mapa.getTile(x, y));
                    if (x < GameMap.WIDTH - 1) sb.append(',');
                }
                w.write(sb.toString()); w.newLine();
            }

            w.write("fruits:"); w.newLine();
            for (int y = 0; y < GameMap.HEIGHT; y++) {
                StringBuilder sb = new StringBuilder();
                for (int x = 0; x < GameMap.WIDTH; x++) {
                    sb.append(mapa.getFruit(x, y));
                    if (x < GameMap.WIDTH - 1) sb.append(',');
                }
                w.write(sb.toString()); w.newLine();
            }
        } catch (IOException e) {
            throw new IceCreamExceptions("Error guardando estado: " + e.getMessage(), e);
        }
    }

    
    public static void loadState(File file,GameMap mapa,IceCream helado,List<Enemigos> enemigosOut) throws IceCreamExceptions {
        if (file == null || !file.exists()) throw new IceCreamExceptions("Archivo inexistente");
        List<String> enemyTypes = new ArrayList<>();
        List<Integer> enemyXs = new ArrayList<>();
        List<Integer> enemyYs = new ArrayList<>();

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            int phase = 0;
            int tilesRow = 0;
            int fruitsRow = 0;
            int iceX = 0, iceY = 0;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (phase == 0) {
                    if (line.startsWith("iceX=")) iceX = parseInt(line.substring(5));
                    else if (line.startsWith("iceY=")) iceY = parseInt(line.substring(5));
                    else if (line.startsWith("enemy=")) {
                        String[] parts = line.substring(6).split(",");
                        if (parts.length >= 3) {
                            enemyTypes.add(parts[0]);
                            enemyXs.add(parseInt(parts[1]));
                            enemyYs.add(parseInt(parts[2]));
                        }
                    } else if (line.equals("tiles:")) {
                        phase = 1;
                        tilesRow = 0;
                    } else if (line.equals("fruits:")) {
                        phase = 2;
                        fruitsRow = 0;
                    }
                } else if (phase == 1) {
                    String[] vals = line.split(",");
                    for (int x = 0; x < Math.min(vals.length, GameMap.WIDTH); x++) {
                        mapa.setTile(x, tilesRow, parseInt(vals[x]));
                    }
                    tilesRow++;
                    if (tilesRow >= GameMap.HEIGHT) phase = 0;
                } else if (phase == 2) {
                    String[] vals = line.split(",");
                    for (int x = 0; x < Math.min(vals.length, GameMap.WIDTH); x++) {
                        mapa.setFruit(x, fruitsRow, parseInt(vals[x]));
                    }
                    fruitsRow++;
                    if (fruitsRow >= GameMap.HEIGHT) break;
                }
            }

            int targetTileX = iceX / GameMap.TILE_SIZE;
            int targetTileY = iceY / GameMap.TILE_SIZE;
            int curTileX = helado.getX() / GameMap.TILE_SIZE;
            int curTileY = helado.getY() / GameMap.TILE_SIZE;
            helado.moverTile(targetTileX - curTileX, targetTileY - curTileY);

            if (enemigosOut != null) {
                enemigosOut.clear();
                for (int i = 0; i < enemyTypes.size(); i++) {
                    Enemigos e = createEnemyByType(enemyTypes.get(i), enemyXs.get(i), enemyYs.get(i), mapa);
                    if (e != null) enemigosOut.add(e);
                }
            }
        } catch (IOException e) {
            throw new IceCreamExceptions("Error leyendo estado: " + e.getMessage(), e);
        }
    }

    public static String readMode(File file) throws IceCreamExceptions { return readHeaderString(file, "mode="); }
    public static int readLevel(File file) throws IceCreamExceptions { return readHeaderInt(file, "level="); }
    public static int readTime(File file) throws IceCreamExceptions { return readHeaderInt(file, "timeRemaining="); }
    public static int readScore(File file) throws IceCreamExceptions { return readHeaderInt(file, "score="); }
    public static String readFlavor(File file) throws IceCreamExceptions { return readHeaderString(file, "flavor="); }
    public static int[] readIceXY(File file) throws IceCreamExceptions {
        int x = readHeaderInt(file, "iceX=");
        int y = readHeaderInt(file, "iceY=");
        return new int[]{x, y};
    }

    private static String readHeaderString(File file, String key) throws IceCreamExceptions {
        if (file == null || !file.exists()) throw new IceCreamExceptions("Archivo inexistente");
        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith(key)) return line.substring(key.length()).trim();
                if (line.equals("tiles:")) break;
            }
            return "";
        } catch (IOException e) {
            throw new IceCreamExceptions("Error leyendo cabeceras: " + e.getMessage(), e);
        }
    }
    private static int readHeaderInt(File file, String key) throws IceCreamExceptions {
        String s = readHeaderString(file, key);
        return parseInt(s);
    }

    private static Enemigos createEnemyByType(String type, int x, int y, GameMap mapa) {
        if (type == null) return null;
        switch (type) {
            case "Troll": return new Troll(x, y, mapa);
            case "Maceta": return new Maceta(x, y, mapa);
            case "CalamarNaranja": return new CalamarNaranja(x, y, mapa);
            case "Narval": return new Narval(x, y, mapa);
            default: return null;
        }
    }
    private static int parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }
    private static String safe(String s) { return (s == null) ? "" : s; }
}
