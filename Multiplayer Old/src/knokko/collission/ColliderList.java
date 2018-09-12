package knokko.collission;

import java.awt.geom.Point2D.Float;

public class ColliderList extends Collider {
	
	Collider[] colliders;
	
	float minX;
	float minY;
	float maxX;
	float maxY;

	public ColliderList(Collider... list) {
		colliders = list;
		minX = colliders[0].minX();
		minY = colliders[0].minY();
		maxX = colliders[0].maxX();
		maxY = colliders[0].maxY();
		int t = 1;
		while(t < colliders.length){
			if(colliders[t].minX() < minX)
				minX = colliders[t].minX();
			if(colliders[t].minY() < minY)
				minY = colliders[t].minY();
			if(colliders[t].maxX() > maxX)
				maxX = colliders[t].maxX();
			if(colliders[t].maxY() > maxY)
				maxY = colliders[t].maxY();
			++t;
		}
	}

	@Override
	public float minX() {
		return minX;
	}

	@Override
	public float minY() {
		return minY;
	}

	@Override
	public float maxX() {
		return maxX;
	}

	@Override
	public float maxY() {
		return maxY;
	}

	@Override
	public boolean hit(Collider other) {
		if(other instanceof ColliderNull || minX > other.maxX() || maxX < other.minX() || minY > other.maxY() || maxY < other.minY())
			return false;
		int t = 0;
		while(t < colliders.length){
			if(colliders[t].hit(other))
				return true;
			++t;
		}
		return false;
	}

	@Override
	public boolean equals(Collider other) {
		if(other instanceof ColliderList){
			ColliderList list = (ColliderList) other;
			if(list.colliders.length == colliders.length){
				int t = 0;
				while(t < colliders.length){
					if(!colliders[t].equals(list.colliders[t]))
						return false;
					++t;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public float distanceTo(Collider other) {
		float distance = colliders[0].distanceTo(other);
		int t = 1;
		while(t < colliders.length){
			float d = colliders[t].distanceTo(other);
			if(d < distance)
				distance = d;
			if(distance == 0)
				return distance;
			++t;
		}
		return distance;
	}

	@Override
	public boolean isInCollider(Float position) {
		if(position.x >= minX && position.x <= maxX && position.y >= minY && position.y <= maxY){
			int t = 0;
			while(t < colliders.length){
				if(colliders[t].isInCollider(position))
					return true;
				++t;
			}
		}
		return false;
	}

}
