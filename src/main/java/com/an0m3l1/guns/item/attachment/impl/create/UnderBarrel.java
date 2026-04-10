package com.an0m3l1.guns.item.attachment.impl.create;

import com.an0m3l1.guns.interfaces.IGunModifier;

/**
 * An attachment class related to under barrels. Use {@link #create(IGunModifier...)} to create a get.
 * <p>
 * Author: MrCrayfish
 */
public class UnderBarrel extends Attachment
{
	private UnderBarrel(IGunModifier... modifier)
	{
		super(modifier);
	}
	
	/**
	 * Creates an under barrel get
	 *
	 * @param modifier
	 * 		an array of gun modifiers
	 *
	 * @return an under barrel get
	 */
	public static UnderBarrel create(IGunModifier... modifier)
	{
		return new UnderBarrel(modifier);
	}
}
