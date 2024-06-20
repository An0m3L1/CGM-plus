package com.mrcrayfish.guns.common;

import com.mrcrayfish.guns.item.attachment.impl.Scope;

/**
 * Author: MrCrayfish
 */
public class Attachments
{
    public static final Scope SHORT_SCOPE = Scope.builder().aimFovModifier(0.75F).modifiers(GunModifiers.SHORT_SCOPE).build();
    public static final Scope MEDIUM_SCOPE = Scope.builder().aimFovModifier(0.5F).modifiers(GunModifiers.MEDIUM_SCOPE).build();
    public static final Scope LONG_SCOPE = Scope.builder().aimFovModifier(0.25F).modifiers(GunModifiers.LONG_SCOPE).build();
}
