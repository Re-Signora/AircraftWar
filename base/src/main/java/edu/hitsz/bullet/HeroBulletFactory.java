package edu.hitsz.bullet;

import edu.hitsz.animate.AnimateContainerFactory;
import edu.hitsz.vector.Vec2;

/**
 * @author Chiro
 */
public class HeroBulletFactory extends BaseBulletFactory {
    public HeroBulletFactory(Vec2 posInit) {
        super(posInit);
    }

    @Override
    public BaseBullet create() {
        return new HeroBullet(
                getPosition(),
                new AnimateContainerFactory(
                        AnimateContainerFactory.ContainerType.ConstSpeed,
                        getPosition())
                        .setupSpeed(new Vec2(0, -2))
                        .create(),
                30);
    }
}
