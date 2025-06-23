package com.mrcrayfish.guns.item.attachment.impl;

import com.mrcrayfish.guns.item.attachment.BarrelItem;
import com.mrcrayfish.guns.item.attachment.impl.create.Tactical;

/**
 * An interface to turn an any item into a tactical attachment. This is useful if your item extends a
 * custom item class otherwise {@link BarrelItem} can be used instead of
 * this interface.
 * <p>
 * Author: Ocelot, MrCrayfish
 */
public interface ITactical extends IAttachment<Tactical>
{
    /**
     * @return The type of this attachment
     */
    @Override
    default Type getType()
    {
        return Type.TACTICAL;
    }
}
