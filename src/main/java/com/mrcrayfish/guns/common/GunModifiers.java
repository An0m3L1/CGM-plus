package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.interfaces.IGunModifier;

public class GunModifiers
{
    //Other modifiers
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

    //ADS modifiers
    public static final IGunModifier ADS_SLOW_S = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 0.9F;
        }
    };
    public static final IGunModifier ADS_SLOW_M = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 0.8F;
        }
    };
    public static final IGunModifier ADS_SLOW_L = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 0.7F;
        }
    };

    public static final IGunModifier ADS_FAST_S = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 1.1F;
        }
    };
    public static final IGunModifier ADS_FAST_M = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 1.2F;
        }
    };
    public static final IGunModifier ADS_FAST_L = new IGunModifier()
    {
        @Override
        public double modifyAimDownSightSpeed(double speed)
        {return speed * 1.3F;
        }
    };

    //Recoil modifiers
    public static final IGunModifier RECOIL_INC_S = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 1.1F;
        }
        @Override
        public float kickModifier()
        {
            return 1.05f;
        }
    };
    public static final IGunModifier RECOIL_INC_M = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 1.2F;
        }
        @Override
        public float kickModifier()
        {
            return 1.1F;
        }
    };
    public static final IGunModifier RECOIL_INC_L = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 1.4F;
        }
        @Override
        public float kickModifier()
        {
            return 1.2F;
        }
    };

    public static final IGunModifier RECOIL_RED_S = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.8F;
        }
        @Override
        public float kickModifier()
        {
            return 0.9f;
        }
    };
    public static final IGunModifier RECOIL_RED_M = new IGunModifier() {
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
    };
    public static final IGunModifier RECOIL_RED_L = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.4F;
        }
        @Override
        public float kickModifier()
        {
            return 0.7F;
        }
    };

    //Spread modifiers
    public static final IGunModifier SPREAD_INC_S = new IGunModifier()
    {
        public float modifyProjectileSpread(float spread)
        {return spread * 1.1F;
        }
    };
    public static final IGunModifier SPREAD_INC_M = new IGunModifier()
    {
        public float modifyProjectileSpread(float spread)
        {return spread * 1.2F;
        }
    };
    public static final IGunModifier SPREAD_INC_L = new IGunModifier()
    {
        public float modifyProjectileSpread(float spread)
        {return spread * 1.4F;
        }
    };

    public static final IGunModifier SPREAD_RED_S = new IGunModifier()
    {
        public float modifyProjectileSpread(float spread)
        {return spread * 0.8F;
        }
    };
    public static final IGunModifier SPREAD_RED_M = new IGunModifier()
    {
        public float modifyProjectileSpread(float spread)
        {return spread * 0.6F;
        }
    };
    public static final IGunModifier SPREAD_RED_L = new IGunModifier()
    {
        public float modifyProjectileSpread(float spread)
        {return spread * 0.4F;
        }
    };

    //Damage modifiers
    public static final IGunModifier DMG_INC_S = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        { return damage * 1.1F;
        }
    };
    public static final IGunModifier DMG_INC_L = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        { return damage * 1.2F;
        }
    };

    public static final IGunModifier DMG_RED_S = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        { return damage * 0.9F;
        }
    };
    public static final IGunModifier DMG_RED_L = new IGunModifier()
    {
        @Override
        public float modifyProjectileDamage(float damage)
        { return damage * 0.8F;
        }
    };

    //Velocity modifiers
    public static final IGunModifier VEL_FAST_S = new IGunModifier() {
        @Override
        public double modifyProjectileSpeed(double speed)
        {return speed * 1.1f;
        }
    };
    public static final IGunModifier VEL_FAST_L = new IGunModifier() {
        @Override
        public double modifyProjectileSpeed(double speed)
        {return speed * 1.2f;
        }
    };

    public static final IGunModifier VEL_SLOW_S = new IGunModifier() {
        @Override
        public double modifyProjectileSpeed(double speed)
        {return speed * 0.9f;
        }
    };
    public static final IGunModifier VEL_SLOW_L = new IGunModifier() {
        @Override
        public double modifyProjectileSpeed(double speed)
        {return speed * 0.8f;
        }
    };

    //Muzzle flash modifiers
    public static final IGunModifier FLASH_INC = new IGunModifier() {
        @Override
        public double modifyMuzzleFlashScale(double scale) {return 1.5f;}
    };
    public static final IGunModifier FLASH_RED = new IGunModifier() {
        @Override
        public double modifyMuzzleFlashScale(double scale) {return 0.5f;}
    };

    //Critical hit modifiers
    public static final IGunModifier CRIT_S = new IGunModifier() {
        @Override
        public float criticalChance() {
            return 0.10f;
        }
    };
    public static final IGunModifier CRIT_M = new IGunModifier() {
        @Override
        public float criticalChance() {
            return 0.15f;
        }
    };
    public static final IGunModifier CRIT_L = new IGunModifier() {
        @Override
        public float criticalChance() {
            return 0.20f;
        }
    };

    //Old modifiers kept for compatibility reasons
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
