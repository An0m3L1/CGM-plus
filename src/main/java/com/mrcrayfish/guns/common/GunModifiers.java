package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.interfaces.IGunModifier;

public class GunModifiers
{
    /* Scopes */
    public static final IGunModifier RED_DOT = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.95F;
        }
    };
    public static final IGunModifier X2_SCOPE = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.9F;
        }
    };
    public static final IGunModifier X4_SCOPE = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.85F;
        }
    };
    public static final IGunModifier X6_SCOPE = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.8F;
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
        public double modifyMuzzleFlashScale(double scale) {return scale * 0.5;}
        @Override
        public double modifyFireSoundRadius(double radius)
        {
            return radius * 0.75;
        }

        @Override
        public float recoilModifier()
        {
            return 1.1F;
        }
        @Override
        public float kickModifier()
        {
            return 1.05F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.9F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.9F;}
    };
    public static final IGunModifier HEAVY_SILENCER = new IGunModifier() {
        @Override
        public boolean silencedFire()
        {
            return true;
        }
        @Override
        public double modifyMuzzleFlashScale(double scale) {return scale * 0.25;}
        @Override
        public double modifyFireSoundRadius(double radius)
        {
            return radius * 0.5;
        }

        @Override
        public float recoilModifier() {return 1.15F;}
        @Override
        public float kickModifier() {return 1.075F;}

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.85F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.85F;}
    };
    public static final IGunModifier FLASH_HIDER = new IGunModifier() {
        @Override
        public double modifyMuzzleFlashScale(double scale) {return scale * 0.5;}

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.9F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.95F;}
    };
    public static final IGunModifier MUZZLE_BRAKE = new IGunModifier() {
        @Override
        public double modifyMuzzleFlashScale(double scale) {return scale * 0.75;}

        @Override
        public float recoilModifier() {return 0.9F;}
        @Override
        public float kickModifier() {return 0.95F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.95F;}
    };
    public static final IGunModifier COMPENSATOR = new IGunModifier() {
        @Override
        public double modifyMuzzleFlashScale(double scale) {return scale * 0.75;}

        @Override
        public float recoilModifier() {return 0.95F;}
        @Override
        public float kickModifier() {return 0.975F;}

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.95F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.95F;}
    };

    /* Stocks */
    public static final IGunModifier LIGHT_STOCK = new IGunModifier() {
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
        public float modifyProjectileSpread(float spread) {return spread * 0.8F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.9F;}
    };
    public static final IGunModifier MEDIUM_STOCK = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.65F;
        }
        @Override
        public float kickModifier()
        {
            return 0.825F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.7F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.85F;}
    };
    public static final IGunModifier SOLID_STOCK = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.65F;
        }
        @Override
        public float kickModifier()
        {
            return 0.825F;
        }

        @Override
        public float modifyProjectileSpread(float spread) {return spread * 0.7F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.85F;}
    };
    public static final IGunModifier HEAVY_STOCK = new IGunModifier() {
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
        public float modifyProjectileSpread(float spread) {return spread * 0.6F;}

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.8F;}
    };

    /* Grips */
    public static final IGunModifier HORIZONTAL_GRIP = new IGunModifier() {
        @Override
        public float modifyProjectileSpread(float spread) {
            return spread * 0.8F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.9F;
        }
    };
    public static final IGunModifier ANGLED_GRIP = new IGunModifier() {
        @Override
        public float modifyProjectileSpread(float spread) {
            return spread * 0.9F;
        }
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
        public double modifyAimDownSightSpeed(double speed) {
            return speed * 0.9F;
        }
    };
    public static final IGunModifier VERTICAL_GRIP = new IGunModifier() {
        @Override
        public float recoilModifier()
        {
            return 0.8F;
        }
        @Override
        public float kickModifier()
        {
            return 0.9F;
        }

        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.9F;}
    };

    /* Magazines */
    public static final IGunModifier LIGHT_MAG = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 1.1F;}
        @Override
        public boolean lightMag() {return true;}
    };
    public static final IGunModifier EXTENDED_MAG = new IGunModifier() {
        @Override
        public double modifyAimDownSightSpeed(double speed) {return speed * 0.9F;}
        @Override
        public boolean extMag() {return true;}
    };

    public static final IGunModifier NONE = new IGunModifier() {};
}
