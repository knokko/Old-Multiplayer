package knokko.collission;

import java.awt.geom.Point2D.Float;

public class ColliderNull extends Collider {

	public ColliderNull() {}

	@Override
	public boolean hit(Collider other){
		return false;
	}

	@Override
	public boolean equals(Collider other) {
		return other instanceof ColliderNull;
	}
	
	@Override
	public String toString(){
		return "null collider";
	}

	@Override
	public float distanceTo(Collider other) {
		return java.lang.Float.NaN;
	}

	@Override
	public boolean isInCollider(Float position) {
		return false;
	}

	@Override
	public float minX() {
		return java.lang.Float.NaN;
	}

	@Override
	public float minY() {
		return java.lang.Float.NaN;
	}

	@Override
	public float maxX() {
		return java.lang.Float.NaN;
	}

	@Override
	public float maxY() {
		return java.lang.Float.NaN;
	}
}
