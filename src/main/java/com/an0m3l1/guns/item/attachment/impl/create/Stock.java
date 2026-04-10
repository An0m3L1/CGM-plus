package com.an0m3l1.guns.item.attachment.impl.create;

import com.an0m3l1.guns.interfaces.IGunModifier;

/**
 * An attachment class related to stocks. Use {@link #create(IGunModifier...)} to create a get.
 * <p>
 * Author: MrCrayfish
 */
public class Stock extends Attachment
{
	private Stock(IGunModifier... modifier)
	{
		super(modifier);
	}
	
	/**
	 * Creates a stock get
	 *
	 * @param modifier
	 * 		an array of gun modifiers
	 *
	 * @return a stock get
	 */
	public static Stock create(IGunModifier... modifier)
	{
		return new Stock(modifier);
	}
}
