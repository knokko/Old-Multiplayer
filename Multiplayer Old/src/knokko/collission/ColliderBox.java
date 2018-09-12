package knokko.collission;

import java.awt.geom.Point2D.Float;

public class ColliderBox extends Collider {
	
	float minX;
	float maxX;
	float minY;
	float maxY;
	
	public ColliderBox(float minX, float maxX, float minY, float maxY) {
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;
	}
	
	public ColliderBox(Float min, Float max){
		this(min.x, max.x, min.y, max.y);
	}

	@Override
	public boolean hit(Collider other) {
		if(other instanceof ColliderBox){
			ColliderBox box = (ColliderBox) other;
			return box.minX <= maxX && box.maxX >= minX && box.minY <= maxY && box.maxY >= minY;
		}
		if(other instanceof ColliderCircle || other instanceof ColliderList)
			return other.hit(this);
		if(other instanceof ColliderNull)
			return false;
		throw new Collider.CollissionException();
	}

	@Override
	public boolean equals(Collider other) {
		if(other instanceof ColliderBox){
			ColliderBox box = (ColliderBox) other;
			return box.minX == minX && box.minY == minY && box.maxX == maxX && box.maxY == maxY;
		}
		return false;
	}
	
	@Override
	public String toString(){
		return "box collider:(minX:" + minX + ",minY:" + minY + ",maxX:" + maxX + ",maxY:" + maxY + ")";
	}

	@Override
	public float distanceTo(Collider other) {
		if(hit(other))
			return 0;
		if(other instanceof ColliderNull)
			return java.lang.Float.NaN;
		if(other instanceof ColliderBox){
			ColliderBox box = (ColliderBox) other;
			double distanceX;
			if(maxX < box.minX)
				distanceX = box.minX - maxX;
			else if(minX > box.maxX)
				distanceX = minX - box.maxX;
			else
				distanceX = 0;
			double distanceY;
			if(maxY < box.minY)
				distanceY = box.minY - maxY;
			else if(minY > box.maxY)
				distanceY = minY - box.maxY;
			else
				distanceY = 0;
			return (float) Math.hypot(distanceX, distanceY);
		}
		if(other instanceof ColliderCircle || other instanceof ColliderList)
			return other.distanceTo(this);
		throw new Collider.CollissionException();
	}
	
	@Override
	public boolean isInCollider(Float position){
		return position.x >= minX && position.x <= maxX && position.y >= minY && position.y <= maxY;
	}
	
	public float minX(){
		return minX;
	}
	
	public float minY(){
		return minY;
	}
	
	public float maxX(){
		return maxX;
	}
	
	public float maxY(){
		return maxY;
	}
}
