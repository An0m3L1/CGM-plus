package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.item.attachment.impl.Scope;

/**
 * Author: MrCrayfish
 */
public class Attachments
{
    public static final Scope SHORT_SCOPE = Scope.builder().aimFovModifier(0.75F).reticleOffset(1.55F).viewFinderDistance(1.1).modifiers(GunModifiers.ADS_FAST_S).build();
    public static final Scope MEDIUM_SCOPE = Scope.builder().aimFovModifier(0.5F).reticleOffset(1.625F).viewFinderDistance(1.0).modifiers(GunModifiers.ADS_SLOW_S).build();
    public static final Scope LONG_SCOPE = Scope.builder().aimFovModifier(0.25F).reticleOffset(1.4F).viewFinderDistance(1.4).modifiers(GunModifiers.ADS_SLOW_M).build();
}
