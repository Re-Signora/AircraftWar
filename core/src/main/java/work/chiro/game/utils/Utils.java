package work.chiro.game.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import work.chiro.game.objects.bullet.BaseBullet;
import work.chiro.game.objects.bullet.EnemyBulletFactory;
import work.chiro.game.x.compatible.ResourceProvider;
import work.chiro.game.x.compatible.XImage;
import work.chiro.game.config.Difficulty;
import work.chiro.game.x.logger.AbstractLogger;
import work.chiro.game.vector.Vec2;

/**
 * 工具类
 *
 * @author Chiro
 */
public class Utils {
    public static LinkedList<BaseBullet> letEnemyShoot(Vec2 position) {
        LinkedList<BaseBullet> ret = new LinkedList<>();
        ret.add(new EnemyBulletFactory(position, EnemyBulletFactory.BulletType.Direct).create());
        return ret;
    }

    public static Boolean isInRange(double value, double down, double up) {
        return down <= value && value < up;
    }

    static long timeStartGlobal = 0;

    public static double getTimeMills() {
        if (timeStartGlobal == 0) {
            timeStartGlobal = System.currentTimeMillis();
        }
        return (double) (System.currentTimeMillis() - timeStartGlobal);
    }

    public static double setInRange(double value, double down, double up) {
        return Math.max(down, Math.min(value, up));
    }

    private static final Map<String, XImage<?>> CACHED_IMAGE = new HashMap<>();

    public static XImage<?> getCachedImage(String filePath) throws IOException {
        synchronized (CACHED_IMAGE) {
            if (CACHED_IMAGE.containsKey(filePath)) {
                return CACHED_IMAGE.get(filePath);
            }
            try {
                XImage<?> image = ResourceProvider.getInstance().getImageFromResource(filePath);
                CACHED_IMAGE.put(filePath, image);
                return image;
            } catch (NullPointerException e) {
                e.printStackTrace();
                System.exit(-1);
                return null;
            }
        }
    }

    static Map<String, Difficulty> difficultyHashMap = Map.of("简单", Difficulty.Easy, "中等", Difficulty.Medium, "困难", Difficulty.Hard);
    static Map<Difficulty, String> difficultyToStringHashMap = Map.of(Difficulty.Easy, "简单", Difficulty.Medium, "中等", Difficulty.Hard, "困难");

    public static String difficultyToString(Difficulty difficulty) {
        if (difficulty == null || !difficultyToStringHashMap.containsKey(difficulty)) {
            return null;
        }
        return difficultyToStringHashMap.get(difficulty);
    }

    public static Difficulty difficultyFromString(String string) {
        if (string == null || !difficultyHashMap.containsKey(string)) {
            return null;
        }
        return difficultyHashMap.get(string);
    }

    static Random random = new Random();

    public static Random getRandom() {
        return random;
    }

    public static Vec2 randomPosition(Vec2 rangeLeft, Vec2 rangeRight) {
        return rangeRight.minus(rangeLeft).times(new Vec2(random.nextDouble(), random.nextDouble())).plus(rangeLeft);
    }

    public static String convertDoubleToString(double val) {
        BigDecimal bd = new BigDecimal(String.valueOf(Double.parseDouble(String.format(Locale.CHINA, "%.2f", val))));
        return bd.stripTrailingZeros().toPlainString();
    }

    public static AbstractLogger getLogger() {
        return ResourceProvider.getInstance().getLogger();
    }

    private static int idNow = 0;

    public static int idGenerator() {
        return idNow++;
    }

    @SuppressWarnings("StringOperationCanBeSimplified")
    public static String getStringFromResource(String path) throws IOException {
        InputStream fileInputStream = Utils.class.getResourceAsStream(path);
        if (fileInputStream == null) {
            throw new IOException(path + " not found!");
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final int maxSize = 1024;
        int len;
        byte[] b = new byte[maxSize];
        while ((len = fileInputStream.read(b)) != -1) {
            byteArrayOutputStream.write(b, 0, len);
        }
        // String str = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        return new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
    }
}
