package knokko.entity.ai;

import knokko.entity.Entity;

import java.awt.geom.Point2D.Float;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class EntityAIMove extends EntityAI {
	
	protected Float goal;
	
	protected float speed;
	protected float jumpPower;

	public EntityAIMove(Entity owner) {
		this(owner, null);
	}
	
	public EntityAIMove(Entity entity, Float destination){
		this(entity, null, 0.4f);
	}
	
	public EntityAIMove(Entity entity, Float destination, float movementSpeed){
		this(entity, destination, movementSpeed, 5);
	}
	
	public EntityAIMove(Entity entity, Float destination, float movementSpeed, float jumpForce){
		super(entity);
		setDestination(destination);
		speed = movementSpeed;
		jumpPower = jumpForce;
	}
	
	@Override
	public byte[] addExtraData(byte[] data, int index){
		data = Arrays.copyOf(data, data.length + 8);
		if(goal != null)
			data = ByteBuffer.wrap(data).putFloat(index, goal.x).putFloat(index + 4, goal.y).array();
		else 
			data = ByteBuffer.wrap(data).putFloat(index, -10).putFloat(index + 4, -10).array();
		return data;
	}
	
	@Override
	public int readExtraData(byte[] data, int index){
		goal = new Float(ByteBuffer.wrap(data).getFloat(index), ByteBuffer.wrap(data).getFloat(index + 4));
		if(goal.x == -10 && goal.y == -10)
			goal = null;
		return index + 8;
	}
	
	@Override
	public void update(){
		if(entity == null || goal == null)
			return;
		float x = entity.position.x;
		float y = entity.position.y;
		if(goal.x > x)
			entity.moveX = speed;
		else if(goal.x < x)
			entity.moveX = -speed;
		if(goal.y > y){
			if(entity.onGround == -1 && Math.abs(goal.x - x) < 50)
				entity.moveY = jumpPower;
			if(entity.onWall == -1)
				entity.moveX = -speed;
			if(entity.onWall == 1)
				entity.moveX = speed;
		}
		if(goal.y < y){
			if(entity.onGround == 1)
				entity.onGround = 0;
			if(entity.onWall == -1)
				entity.moveX = speed;
			if(entity.onWall == 1)
				entity.moveX = -speed;
			entity.moveY = -jumpPower;
		}
		if(goal.y == y){
			if(entity.onWall == 1)
				entity.moveY = jumpPower;
			else
				entity.moveY = 0;
		}
	}
	
	public void setTarget(Entity entity){
		setDestination(entity.position);
	}
	
	public void setDestination(Float destination){
		goal = destination;
	}

}
