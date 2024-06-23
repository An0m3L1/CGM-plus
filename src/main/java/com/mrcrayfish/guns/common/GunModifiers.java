package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.interfaces.IGunModifier;

public class GunModifiers
{
    /* Scopes */
    public static final IGunModifier SHORT_SCOPE = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 0.95F;
        }
    };
    public static final IGunModifier MEDIUM_SCOPE = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 0.9F;
        }
    };
    public static final IGunModifier LONG_SCOPE = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 0.85F;
        }
    };

    /* Barrels */
    public static final IGunModifier SILENCER = new IGunModifier() {
        @Override
        public boolean silencedFire()
        {
            return true;
        }
        @Override
        public double modifyMuzzleFlashScale(double scale) {return 0.25f;}

        @Override
        public double modifyFireSoundRadius(double radius)
        {
            return radius * 0.25;
        }

        @Override
        public float recoilModifier()
        {
            return 1.15F;
        }
        @Override
        public float kickModifier()
        {
            return 1.05f;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.85F;}
    };

    /* Stocks */
    public static final IGunModifier LIGHT_STOCK = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.65F;
        }
        @Override
        public float kickModifier()
        {
            return 0.85F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.8F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.95F;}
    };
    public static final IGunModifier TACTICAL_STOCK = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.6F;
        }
        @Override
        public float kickModifier()
        {
            return 0.8F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.7F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.9F;}
    };
    public static final IGunModifier WEIGHTED_STOCK = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.55F;
        }
        @Override
        public float kickModifier()
        {
            return 0.75F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.6F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.85F;}
    };

    /* Grips*/
    public static final IGunModifier LIGHT_GRIP = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.9F;
        }
        @Override
        public float kickModifier()
        {
            return 0.95F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {
            return spread * 0.9F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 1.1F;
        }
    };
    public static final IGunModifier SPECIALISED_GRIP = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.7F;
        }
        @Override
        public float kickModifier()
        {
            return 0.85F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.7F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.9F;}
    };

    //Old modifiers kept for compatibility reasons
    public static final IGunModifier SILENCED = new IGunModifier() {
        @Override
        public boolean silencedFire()
        {
            return true;
        }

        @Override
        public double modifyFireSoundRadius(double radius)
        {
            return radius * 0.25;
        }
    };
    public static final IGunModifier REDUCED_VELOCITY = new IGunModifier() {
        @Override
        public double modifyProjectileSpeed(double speed)
        {
            return speed * 0.8;
        }

        @Override
        public float modifyProjectileDamage(float damage)
        {
            return damage * 0.95F;
        }
    };
    public static final IGunModifier REDUCED_DAMAGE = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        { return damage * 0.75F;
        }
    };
    public static final IGunModifier SLOW_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.95F;
        }
    };
    public static final IGunModifier SLOWER_ADS = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 0.9F;
        }
    };
    public static final IGunModifier BETTER_CONTROL = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.3F;
        }

        @Override
        public float kickModifier()
        {
            return 0.8F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.95F;
        }
    };
    public static final IGunModifier STABILISED = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.4F;
        }

        @Override
        public float kickModifier()
        {
            return 0.35F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.9F;
        }
    };
    public static final IGunModifier SUPER_STABILISED = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.18F;
        }

        @Override
        public float kickModifier()
        {
            return 0.25F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.35F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.7F;
        }
    };
    public static final IGunModifier LIGHT_RECOIL = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.75F;
        }

        @Override
        public float kickModifier()
        {
            return 0.75F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 1.2F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.8F;
        }
    };
    public static final IGunModifier REDUCED_RECOIL = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.5F;
        }

        @Override
        public float kickModifier()
        {
            return 0.5F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed)
        {
            return speed * 0.95F;
        }

        @Override
        public float modifyProjectileSpread(float spread)
        {
            return spread * 0.5F;
        }
    };
}
