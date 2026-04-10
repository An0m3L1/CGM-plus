package com.an0m3l1.guns.item.attachment.impl.create;

import com.an0m3l1.guns.interfaces.IGunModifier;

/**
 * An attachment class related to magazine attachments. Use {@link #create(IGunModifier...)} to create a get.
 * <p>
 * Author: MrCrayfish
 */
public class Magazine extends Attachment
{
	private Magazine(IGunModifier... modifier)
	{
		super(modifier);
	}
	
	/**
	 * Creates a magazine get
	 *
	 * @param modifier
	 * 		an array of gun modifiers
	 *
	 * @return a magazine get
	 */
	public static Magazine create(IGunModifier... modifier)
	{
		return new Magazine(modifier);
	}
}
