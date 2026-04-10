package com.an0m3l1.guns.item.attachment.impl.create;

import com.an0m3l1.guns.interfaces.IGunModifier;

/**
 * An attachment class related to scopes. Scopes need to at least specify the additional zoom (or fov)
 * they provide and the y-offset to the center of the scope for them to render correctly. Use
 * {@link #create(float, double, IGunModifier...)} to create a get.
 * <p>
 * Author: MrCrayfish
 */
public class Scope extends Attachment
{
	protected float aimFovModifier;
	protected float additionalZoom;
	protected double reticleOffset;
	protected boolean stable;
	protected double viewFinderDist;
	
	private Scope()
	{
	}
	
	private Scope(float additionalZoom, double reticleOffset, IGunModifier... modifier)
	{
		super(modifier);
		this.aimFovModifier = 1.0F;
		this.additionalZoom = additionalZoom;
		this.reticleOffset = reticleOffset;
	}
	
	private Scope(float aimFovModifier, float additionalZoom, double reticleOffset, boolean stable, double viewFinderDist, IGunModifier... modifiers)
	{
		super(modifiers);
		this.aimFovModifier = aimFovModifier;
		this.additionalZoom = additionalZoom;
		this.reticleOffset = reticleOffset;
		this.stable = stable;
		this.viewFinderDist = viewFinderDist;
	}
	
	/**
	 * Marks this scope to allow it to be stabilized while using a controller. This is essentially
	 * holding your breath while looking down the sight.
	 */
	public void stabilize()
	{
		this.stable = true;
	}
	
	/**
	 * Deprecated: Use meta files instead
	 * <p>
	 * Sets the offset distance from the camera to the view finder
	 *
	 * @param offset
	 * 		the view finder offset
	 *
	 * @return this scope get
	 */
	public Scope viewFinderOffset(double offset)
	{
		this.viewFinderDist = offset;
		return this;
	}
	
	public float getFovModifier()
	{
		return this.aimFovModifier;
	}
	
	/**
	 * Deprecated: Use {@link #getFovModifier()}
	 * <p>
	 * Gets the amount of additional zoom (or reduced fov) this scope provides
	 *
	 * @return the scopes additional zoom
	 */
	public float getAdditionalZoom()
	{
		return this.additionalZoom;
	}
	
	/**
	 * Deprecated: Use meta files instead
	 * <p>
	 * Gets the offset to the center of the scope. Used to render scope crosshair exactly in the
	 * middle of the screen.
	 *
	 * @return the scope center offset
	 */
	public double getCenterOffset()
	{
		return this.reticleOffset;
	}
	
	/**
	 * Deprecated: Use meta files instead
	 * <p>
	 * Gets the offset need to translate the gun model so the reticle of the scope aligns with the
	 * center of the screen.
	 *
	 * @return the reticle offset
	 */
	public double getReticleOffset()
	{
		return this.reticleOffset;
	}
	
	/**
	 * @return If this scope can be stabilized
	 */
	public boolean isStable()
	{
		return this.stable;
	}
	
	/**
	 * Deprecated: Use meta files instead
	 *
	 * @return The view finder offset of this scope
	 */
	public double getViewFinderOffset()
	{
		return this.viewFinderDist;
	}
	
	/**
	 * @return The distance to offset camera from the center of the scope model.
	 */
	public double getViewFinderDistance()
	{
		return this.viewFinderDist;
	}
	
	public Scope copy()
	{
		Scope scope = new Scope();
		scope.aimFovModifier = this.aimFovModifier;
		scope.additionalZoom = this.additionalZoom;
		scope.reticleOffset = this.reticleOffset;
		scope.stable = this.stable;
		scope.viewFinderDist = this.viewFinderDist;
		return scope;
	}
	
	/**
	 * Deprecated: Use the builder instead.
	 * <p>
	 * Creates a scope. This method is now deprecated.
	 *
	 * @param additionalZoom
	 * 		the additional zoom this scope provides
	 * @param centerOffset
	 * 		the length to the center of the view finder from the base of the scope model in pixels
	 * @param modifiers
	 * 		an array of gun modifiers
	 *
	 * @return a scope get
	 */
	public static Scope create(float additionalZoom, double centerOffset, IGunModifier... modifiers)
	{
		// -1 to indicate that it should use the default fov
		return new Scope(additionalZoom, centerOffset, modifiers);
	}
	
	public static Builder builder()
	{
		return new Builder();
	}
	
	public static class Builder
	{
		private float aimFovModifier = 1.0F;
		private float additionalZoom = 0.0F;
		private double reticleOffset = 0.0;
		private boolean stable = false;
		private double viewFinderDist = 0.0;
		private IGunModifier[] modifiers = new IGunModifier[]{};
		
		private Builder()
		{
		}
		
		public Builder aimFovModifier(float fovModifier)
		{
			this.aimFovModifier = fovModifier;
			return this;
		}
		
		/**
		 * Deprecated: Use {@link #aimFovModifier(float)}
		 */
		public Builder additionalZoom(float additionalZoom)
		{
			this.additionalZoom = additionalZoom;
			return this;
		}
		
		/**
		 * Deprecated: Use meta files instead
		 */
		public Builder centerOffset(double centerOffset)
		{
			this.reticleOffset = centerOffset;
			return this;
		}
		
		/**
		 * Deprecated: Use meta files instead
		 */
		public Builder reticleOffset(double reticleOffset)
		{
			this.reticleOffset = reticleOffset;
			return this;
		}
		
		public Builder stable(boolean stable)
		{
			this.stable = stable;
			return this;
		}
		
		/**
		 * Deprecated: Use meta files instead
		 */
		public Builder viewFinderOffset(double viewFinderOffset)
		{
			this.viewFinderDist = viewFinderOffset;
			return this;
		}
		
		/**
		 * Deprecated: Use meta files instead
		 */
		public Builder viewFinderDistance(double viewFinderDist)
		{
			this.viewFinderDist = viewFinderDist;
			return this;
		}
		
		public Builder modifiers(IGunModifier... modifiers)
		{
			this.modifiers = modifiers;
			return this;
		}
		
		public Scope build()
		{
			return new Scope(this.aimFovModifier, this.additionalZoom, this.reticleOffset, this.stable, this.viewFinderDist, this.modifiers);
		}
	}
}
