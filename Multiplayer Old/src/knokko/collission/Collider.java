package knokko.collission;

import java.awt.geom.Point2D.Float;

public abstract class Collider {
	
	public abstract float minX();
	
	public abstract float minY();
	
	public abstract float maxX();
	
	public abstract float maxY();
	
	public abstract boolean hit(Collider other);
	
	public abstract boolean equals(Collider other);
	
	public abstract float distanceTo(Collider other);
	
	public abstract boolean isInCollider(Float position);
	
	public static class CollissionException extends RuntimeException {

		private static final long serialVersionUID = -4849637632915697254L;
		
	}
}
