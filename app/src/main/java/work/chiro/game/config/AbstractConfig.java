package work.chiro.game.config;

import work.chiro.game.timer.TimerLinearChange;

/**
 * 配置参数类
 *
 * @author Chiro
 */
public abstract class AbstractConfig {
    protected TimerLinearChange mobCreate = new TimerLinearChange(700);
    protected TimerLinearChange eliteCreate = new TimerLinearChange(1200);
    protected TimerLinearChange enemyShoot = new TimerLinearChange(200);
    protected TimerLinearChange bossShoot = new TimerLinearChange(200);
    protected TimerLinearChange heroShoot = new TimerLinearChange(100d, 1e-3, 10d);

    public TimerLinearChange getMobCreate() {
        return mobCreate;
    }

    public TimerLinearChange getEliteCreate() {
        return eliteCreate;
    }

    public TimerLinearChange getEnemyShoot() {
        return enemyShoot;
    }

    public TimerLinearChange getHeroShoot() {
        return heroShoot;
    }

    public TimerLinearChange getBossShoot() {
        return bossShoot;
    }

    public void printNow() {
        System.out.println("MobCreate:" + getMobCreate().getScaleNow() +
                " EliteCreate:" + getEliteCreate().getScaleNow() +
                " enemyShoot:" + getEnemyShoot().getScaleNow() +
                " bossShoot:" + getBossShoot().getScaleNow() +
                " heroShoot:" + getHeroShoot().getScaleNow());
    }
}
