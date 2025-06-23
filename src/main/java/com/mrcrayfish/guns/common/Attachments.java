package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.util.attachment.impl.Scope;

/**
 * Author: MrCrayfish
 */
public class Attachments
{
    public static final Scope RED_DOT_SIGHT = Scope.builder().aimFovModifier(0.75F).modifiers(GunModifiers.RED_DOT_SIGHT).build();
    public static final Scope X2_SCOPE = Scope.builder().aimFovModifier(0.5F).modifiers(GunModifiers.X2_SCOPE).build();
    public static final Scope X4_SCOPE = Scope.builder().aimFovModifier(0.25F).modifiers(GunModifiers.X4_SCOPE).build();
    public static final Scope X6_SCOPE = Scope.builder().aimFovModifier(0.17F).modifiers(GunModifiers.X6_SCOPE).build();
}
