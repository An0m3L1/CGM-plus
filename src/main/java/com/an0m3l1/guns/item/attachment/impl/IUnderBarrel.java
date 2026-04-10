package com.an0m3l1.guns.item.attachment.impl;

import com.an0m3l1.guns.item.attachment.UnderBarrelItem;
import com.an0m3l1.guns.item.attachment.impl.create.UnderBarrel;

/**
 * An interface to turn an any item into an underbarrel attachment. This is useful if your item
 * extends a custom item class otherwise {@link UnderBarrelItem} can be
 * used instead of this interface.
 * <p>
 * Author: MrCrayfish
 */
public interface IUnderBarrel extends IAttachment<UnderBarrel>
{
	/**
	 * @return The type of this attachment
	 */
	@Override
	default Type getType()
	{
		return Type.UNDER_BARREL;
	}
}
