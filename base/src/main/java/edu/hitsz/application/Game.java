package edu.hitsz.application;

import edu.hitsz.Utils;
import edu.hitsz.aircraft.*;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.prop.*;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

/**
 * 游戏主面板，游戏启动
 *
 * @author hitsz
 */
public class Game extends JPanel {

    private int backGroundTop = 0;

    /**
     * 创建线程的工厂函数
     */
    private final MyThreadFactory threadFactory = new MyThreadFactory("AircraftWar");
    /**
     * 线程池，自动管理
     */
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1, threadFactory);

    private final HeroAircraft heroAircraft;

    private final List<AbstractAircraft> enemyAircrafts = new LinkedList<>();
    private final List<BaseBullet> heroBullets = new LinkedList<>();
    private final List<BaseBullet> enemyBullets = new LinkedList<>();
    private final List<AbstractProp> props = new LinkedList<>();
    private final List<TimerController> timerControllers = new LinkedList<>();

    private boolean gameOverFlag = false;
    private int score = 0;
    // private int time = 0;

    private static class TimerController {
        private static double frameTime = 0;
        private static double lastFrameTime = 0;
        private static final List<Double> FRAME_COUNTER = new LinkedList<>();

        public static void init(double startTime) {
            frameTime = startTime;
        }

        public static void update() {
            frameTime = Utils.getTimeMills();
            FRAME_COUNTER.add(frameTime);
            FRAME_COUNTER.removeIf(t -> t < ((frameTime >= 1000) ? (frameTime - 1000) : 0));
        }

        public static void done() {
            lastFrameTime = frameTime;
        }

        public static int getFps() {
            return FRAME_COUNTER.size();
        }

        public static double getFrameTime() {
            return frameTime;
        }

        interface TimerCallback {
            void run();
        }

        public final double duration;
        private final TimerCallback callback;
        public double time = 0;

        private static double getTimeDelta() {
            return frameTime - lastFrameTime;
        }

        public TimerController(double duration, TimerCallback callback) {
            this.duration = duration;
            this.callback = callback;
        }

        void execute(double now) {
            time += getTimeDelta();
            if (time >= duration) {
                time %= duration;
                callback.run();
            }
        }
    }

    /**
     * 周期（ms)
     * 指示子弹的发射、敌机的产生频率
     */
    @SuppressWarnings("FieldCanBeLocal")
    public Game() {
        heroAircraft = new HeroAircraftFactory().create();
        //启动英雄机鼠标监听
        new HeroController(this, heroAircraft);
    }

    /**
     * 游戏启动入口，执行游戏逻辑
     */
    public void action() {
        TimerController.init(0);
        // 英雄射击事件
        timerControllers.add(new TimerController(1, () -> heroBullets.addAll(heroAircraft.shoot())));
        // 产生精英敌机事件
        timerControllers.add(new TimerController(1200, () -> {
            synchronized (enemyAircrafts) {
                enemyAircrafts.add(new EliteEnemyFactory().create());
            }
        }));
        // 产生普通敌机事件
        timerControllers.add(new TimerController(700, () -> {
            synchronized (enemyAircrafts) {
                enemyAircrafts.add(new MobEnemyFactory().create());
            }
        }));
        // 敌机射击事件
        timerControllers.add(new TimerController(1000, () -> {
            synchronized (enemyBullets) {
                enemyAircrafts.forEach(enemyAircraft -> enemyBullets.addAll(enemyAircraft.shoot()));
            }
        }));
        // fps 输出事件
        timerControllers.add(new TimerController(1000, () -> System.out.println("fps: " + TimerController.getFps())));


        // 定时任务：绘制、对象产生、碰撞判定、击毁及结束判定
        Runnable task = () -> {
            TimerController.update();
            // execute all
            double now = Utils.getTimeMills();
            timerControllers.forEach(c -> c.execute(now));

            // 子弹移动
            bulletsMoveAction();

            // 飞机移动
            aircraftsMoveAction();

            // 道具移动
            propsMoveAction();

            // 撞击检测
            crashCheckAction();

            // 后处理
            postProcessAction();

            //每个时刻重绘界面
            repaint();

            // 游戏结束检查
            if (heroAircraft.getHp() <= 0) {
                // 游戏结束
                executorService.shutdown();
                gameOverFlag = true;
                System.out.println("Game Over!");
            }
            TimerController.done();
        };

         // 时间间隔(ms)，控制刷新频率
        int timeInterval = 1;
        // 以固定延迟时间进行执行本次任务执行完成后，需要延迟设定的延迟时间，才会执行新的任务
        executorService.scheduleWithFixedDelay(task, timeInterval, timeInterval, TimeUnit.MILLISECONDS);

    }

    private void bulletsMoveAction() {
        for (BaseBullet bullet : heroBullets) {
            bullet.forward();
        }
        for (BaseBullet bullet : enemyBullets) {
            bullet.forward();
        }
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft enemyAircraft : enemyAircrafts) {
            enemyAircraft.forward();
        }
        if (BossEnemyFactory.getInstance() != null) {
            BossEnemyFactory.getInstance().forward();
        }
    }

    private void propsMoveAction() {
        for (AbstractProp prop : props) {
            prop.forward();
        }
    }


    /**
     * 碰撞检测：
     * 1. 敌机攻击英雄
     * 2. 英雄攻击/撞击敌机
     * 3. 英雄获得补给
     */
    private void crashCheckAction() {
        // 英雄子弹攻击敌机
        for (BaseBullet bullet : heroBullets) {
            if (bullet.notValid()) {
                continue;
            }
            BossEnemy boss = BossEnemyFactory.getInstance();
            if (boss != null) {
                if (bullet.crash(boss)) {
                    boss.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (boss.notValid()) {
                        score += boss.getScore();
                    }
                    continue;
                }
            }
            for (AbstractAircraft enemyAircraft : enemyAircrafts) {
                if (enemyAircraft.notValid()) {
                    // 已被其他子弹击毁的敌机，不再检测
                    // 避免多个子弹重复击毁同一敌机的判定
                    continue;
                }
                if (enemyAircraft.crash(bullet)) {
                    // 敌机撞击到英雄机子弹
                    // 敌机损失一定生命值
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (enemyAircraft.notValid()) {
                        // 获得分数，添加掉落道具
                        score += enemyAircraft.getScore();
                        props.addAll(enemyAircraft.dropProps());
                    }
                }
                // 英雄机 与 敌机 相撞，均损毁
                if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                    enemyAircraft.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }

        // 敌机子弹攻击英雄
        for (BaseBullet bullet : enemyBullets) {
            if (bullet.notValid()) {
                continue;
            }
            if (bullet.crash(heroAircraft)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }

        // 我方获得道具，道具生效
        for (AbstractProp prop : props) {
            if (prop.notValid()) {
                continue;
            }
            if (prop.crash(heroAircraft)) {
                prop.handleAircrafts(enemyAircrafts);
                prop.vanish();
            }
        }

    }

    /**
     * 后处理：
     * 1. 删除无效的子弹
     * 2. 删除无效的敌机
     * 3. 检查英雄机生存
     * <p>
     * 无效的原因可能是撞击或者飞出边界
     */
    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
    }


    /**
     * 重写paint方法
     * 通过重复调用paint方法，实现游戏动画
     *
     * @param g 绘图
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // 绘制背景,图片滚动
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop - Main.WINDOW_HEIGHT, null);
        g.drawImage(ImageManager.BACKGROUND_IMAGE, 0, this.backGroundTop, null);
        this.backGroundTop += 1;
        if (this.backGroundTop == Main.WINDOW_HEIGHT) {
            this.backGroundTop = 0;
        }

        // 先绘制子弹，后绘制飞机
        // 这样子弹显示在飞机的下层
        paintImageWithPositionRevised(g, enemyBullets);
        paintImageWithPositionRevised(g, heroBullets);
        paintImageWithPositionRevised(g, enemyAircrafts);
        paintImageWithPositionRevised(g, props);

        g.drawImage(ImageManager.HERO_IMAGE, (int) (heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2),
                (int) (heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2), null);
        BossEnemy boss = BossEnemyFactory.getInstance();
        if (boss != null) {
            g.drawImage(ImageManager.BOSS_ENEMY_IMAGE, (int) (boss.getLocationX() - ImageManager.BOSS_ENEMY_IMAGE.getWidth() / 2),
                    (int) (boss.getLocationY() - ImageManager.BOSS_ENEMY_IMAGE.getHeight() / 2), null);
        }

        //绘制得分和生命值
        paintScoreAndLife(g);

    }

    private void paintImageWithPositionRevised(Graphics g, List<? extends AbstractFlyingObject> objects) {
        if (objects.size() == 0) {
            return;
        }

        for (AbstractFlyingObject object : objects) {
            BufferedImage image = object.getImage();
            assert image != null : objects.getClass().getName() + " has no image! ";
            g.drawImage(image, (int) (object.getLocationX() - image.getWidth() / 2),
                    (int) (object.getLocationY() - image.getHeight() / 2), null);
        }
    }

    private void paintScoreAndLife(Graphics g) {
        int x = 10;
        int y = 25;
        g.setColor(new Color(16711680));
        g.setFont(new Font("SansSerif", Font.BOLD, 22));
        g.drawString("SCORE:" + this.score, x, y);
        y = y + 20;
        g.drawString("LIFE:" + this.heroAircraft.getHp(), x, y);
    }
}
