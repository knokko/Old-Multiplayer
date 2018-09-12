package knokko.entity;

import knokko.client.ClientBoard;
import knokko.client.Model;
import knokko.collission.Collider;
import knokko.collission.ColliderBox;
import knokko.entity.ai.EntityAI;
import knokko.world.World;

import java.awt.Point;
import java.awt.geom.Point2D.Float;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import knokko.util.PointUtils;

public abstract class Entity {
	
	public ArrayList<Model> models = new ArrayList<Model>();
	public ArrayList<EntityAI> ai = new ArrayList<EntityAI>();
	
	public World world;
	public Float position;
	
	public float motionX;
	public float motionY;
	public float moveX;
	public float moveY;
	
	public int width = 60;
	public int height = 60;
	
	public boolean inactive;
	public byte onGround;
	public byte onWall;
	public boolean facingLeft;

	public Entity(World worldIn, Float spawn) {
		world = worldIn;
		position = new Float(spawn.x, spawn.y);
	}
	
	public void addAI(EntityAI entityAI){
		ai.add(entityAI);
	}
	
	public Collider getCollider(Float position){
		return new ColliderBox(position.x - width / 2, position.x + width / 2, position.y - height / 2, position.y + height / 2);
	}
	
	public Collider getCollider(){
		return getCollider(position);
	}
	
	public boolean canFly(){
		return false;
	}
	
	public void attack(float damage){}
	
	public void disable(){
		inactive = true;
	}
	
	public void update(){
		int t = 0;
		while(t < ai.size()){
			ai.get(t).update();
			++t;
		}
		if(onWall == -1)
			facingLeft = true;
		if(onWall == 1)
			facingLeft = false;
		if(!canFly())
			addForce(0, -9.8F * getWeight());
		if((onGround != 0 || onWall != 0) && !canFly()){
			motionX *= 0.9;
			motionY *= 0.9;
		}
		float friction = frictionMultiplier();
		float speedX = pttoms(motionX);
		float speedY = pttoms(motionY);
		if(motionX > 0)
			addForce(-friction * speedX * speedX, 0);
		else
			addForce(friction * speedX * speedX, 0);
		if(motionY > 0)
			addForce(0, -friction * speedY * speedY);
		else
			addForce(0, friction * speedY * speedY);
		move(motionX, motionY, false);
		if(position.y < -120)
			inactive = true;
	}
	
	public void paintIfInRange(ClientBoard board){
		Collider c = getCollider();
		Point screen = PointUtils.gameToScreenPoint(board.client, new Float(c.minX(), c.minY()));
		Point screen2 = PointUtils.gameToScreenPoint(board.client, new Float(c.maxX(), c.maxY()));
		if(screen.x <= board.client.getWidth() && screen2.x >= 0 && screen2.y <= board.client.getHeight() && screen.y >= 0)
			paint(board);
	}
	
	public void paint(ClientBoard board){
		int t = 0;
		while(t < models.size()){
			models.get(t).paint(board);
			++t;
		}
	}
	
	public boolean teleport(Float target, boolean isTest){
		Collider collider = getCollider(target);
		ArrayList<Collider> colliders = world.getColliders(getCollider(target));
		int t = 0;
		while(t < colliders.size()){
			if(collider.hit(colliders.get(t)))
				return false;
			++t;
		}
		if(target.x < 0 || target.x > world.tiles.length * 60)
			return false;
		if(!isTest)
			position = new Float(target.x, target.y);
		return true;
	}
	
	public boolean move(float x, float y, boolean isTest){
		if(position.x < 0){
			inactive = true;
			return false;
		}
		x += onWall;
		if(!isTest){
			onGround = 0;
			onWall = 0;
		}
		Float back = new Float(position.x, position.y);
		Float t = new Float(back.x, back.y);
		while(x != 0 || y != 0){
			float mx = 0;
			if(x > 0){
				if(x >= 1)
					mx = 1;
				else
					mx = x;
			}
			if(x < 0){
				if(x <= -1)
					mx = -1;
				else
					mx = x;
			}
			float my = 0;
			if(y > 0){
				if(y >= 1)
					my = 1;
				else
					my = y;
			}
			if(y < 0){
				if(y <= -1)
					my = -1;
				else
					my = y;
			}
			if(teleport(new Float(position.x + mx, position.y), isTest)){
				x -= mx;
				t.x += mx;
			}
			else {
				x = 0;
				if(!isTest){
					motionX = 0;
					if(getCollider(new Float(position.x + mx, position.y)).minX() > 1)
						onWall = (byte) (mx > 0 ? 1 : -1);
				}
			}
			if(teleport(new Float(position.x, position.y + my), isTest)){
				y -= my;
				t.y += my;
			}
			else {
				y = 0;
				if(!isTest){
					onGround = (byte) (my > 0 ? 1 : -1);
					motionY = 0;
				}
			}
		}
		return isTest ? !back.equals(t) : !back.equals(position);
	}
	
	public float getWeight(){
		return 50;
	}
	
	public void addForce(float fx, float fy){
		if(fx != 0)
			motionX += ntopt(fx);
		if(fy != 0)
			motionY += ntopt(fy);
	}
	
	public byte[] addExtraData(byte[] data, int i){
		data = Arrays.copyOfRange(data, 0, data.length + 4 * models.size() + 8);
		data = ByteBuffer.wrap(data).putFloat(i, moveX).putFloat(i + 4, moveY).array();
		i += 8;
		int t = 0;
		while(t < models.size()){
			byte[] rotation = ByteBuffer.allocate(4).putFloat(models.get(t).rotation).array();
			data[i + t * 4 + 0] = rotation[0];
			data[i + t * 4 + 1] = rotation[1];
			data[i + t * 4 + 2] = rotation[2];
			data[i + t * 4 + 3] = rotation[3];
			++t;
		}
		i += t * 4;
		t = 0;
		while(t < ai.size()){
			data = ai.get(t).addExtraData(data, i);
			i = data.length;
			++t;
		}
		return data;
	}
	
	public float frictionMultiplier(){
		return 0.1f;
	}
	
	public int readExtraData(byte[] data, int i){
		moveX = ByteBuffer.wrap(data).getFloat(i);
		moveY = ByteBuffer.wrap(data).getFloat(i + 4);
		i += 8;
		int t = 0;
		while(t < models.size()){
			models.get(t).rotation = ByteBuffer.wrap(data, i + t * 4, 4).getFloat();
			++t;
		}
		i += t * 4;
		t = 0;
		while(t < ai.size()){
			i = ai.get(t).readExtraData(data, i);
			++t;
		}
		return i;
	}
	
	public float ntomss(float n){
		return n / getWeight();
	}
	
	public float msston(float mss){
		return mss * getWeight();
	}
	
	public float ntoms(float n){
		return msstoms(ntomss(n));
	}
	
	public float mston(float ms){
		return msston(mstomss(ms));
	}
	
	public float ntopt(float n){
		return mstopt(ntoms(n));
	}
	
	public float ptton(float pt){
		return mston(pttoms(pt));
	}
	
	public static float msstoms(float mss){
		return mss / 50;
	}
	
	public static float mstomss(float ms){
		return ms * 50;
	}
	
	/**
	 * converts a speed in meter/second to a speed in pixel/tick
	 * @param ms the speed in meters per second
	 * @return the speed in pixels per tick
	 */
	public static float mstopt(float ms){
		return ms * 30 / 50;
	}
	
	/**
	 * converts a speed in pixel/tick to a speed in meter/second
	 * @param pt the speed in pixels per tick
	 * @return the speed in meters per second
	 */
	public static float pttoms(float pt){
		return pt / 30 * 50;
	}
}
