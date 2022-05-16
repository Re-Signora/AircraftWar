package work.chiro.game.application;

import android.view.MotionEvent;

import work.chiro.game.aircraft.HeroAircraft;
import work.chiro.game.aircraft.HeroAircraftFactory;
import work.chiro.game.config.RunningConfig;
import work.chiro.game.utils.Utils;

public class HeroControllerAndroidImpl implements HeroController {
    @Override
    public boolean isShootPressed() {
        return true;
    }

    public void onTouchEvent(MotionEvent e) {
        HeroAircraft heroAircraft = HeroAircraftFactory.getInstance();
        if (heroAircraft != null) {
            heroAircraft.setPosition(Utils.setInRange(e.getX(), 0, RunningConfig.windowWidth), Utils.setInRange(e.getY(), 0, RunningConfig.windowHeight));
        }
    }
}
