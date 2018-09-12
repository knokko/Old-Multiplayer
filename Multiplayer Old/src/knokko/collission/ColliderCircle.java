package knokko.collission;

import java.awt.geom.Point2D.Float;

public class ColliderCircle extends Collider {
	
	Float center;
	
	float radius;

	public ColliderCircle(float circleRadius, Float circleCenter) {
		radius = circleRadius;
		center = circleCenter;
	}

	@Override
	public boolean hit(Collider other) {
		if(other instanceof ColliderBox){
			return distanceToBox((ColliderBox) other) <= radius;
		}
		if(other instanceof ColliderCircle){
			ColliderCircle circle = (ColliderCircle) other;
			return circle.center.distance(center) <= radius + circle.radius;
		}
		if(other instanceof ColliderNull)
			return false;
		if(other instanceof ColliderList)
			return other.hit(this);
		throw new Collider.CollissionException();
	}

	@Override
	public boolean equals(Collider other) {
		if(other instanceof ColliderCircle){
			ColliderCircle circle = (ColliderCircle) other;
			return circle.center.equals(center) && radius == circle.radius;
		}
		return false;
	}

	@Override
	public float distanceTo(Collider other) {
		if(other instanceof ColliderBox)
			return distanceToBox((ColliderBox) other);
		if(other instanceof ColliderCircle){
			ColliderCircle circle = (ColliderCircle) other;
			float distance = (float) (center.distance(circle.center) - radius - circle.radius);
			return distance > 0 ? distance : 0;
		}
		if(other instanceof ColliderNull)
			return java.lang.Float.NaN;
		if(other instanceof ColliderList)
			return other.distanceTo(this);
		throw new Collider.CollissionException();
	}

	@Override
	public boolean isInCollider(Float position) {
		return position.distance(center) <= radius;
	}

	@Override
	public float minX() {
		return center.x - radius;
	}

	@Override
	public float minY() {
		return center.y - radius;
	}

	@Override
	public float maxX() {
		return center.x + radius;
	}

	@Override
	public float maxY() {
		return center.y + radius;
	}
	
	private float distanceToBox(ColliderBox box){
		float x = center.x;
		if(center.x > box.maxX)
			x = box.maxX;
		if(center.x < box.minX)
			x = box.minX;
		float y = center.y;
		if(center.y > box.maxY)
			y = box.maxY;
		if(center.y < box.minY)
			y = box.minY;
		return (float) center.distance(x, y);
	}
}
