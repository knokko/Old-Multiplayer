package knokko.entity.ai;

import java.util.Arrays;

import knokko.client.Model;
import knokko.entity.Entity;

public class EntityAIMoveLegs extends EntityAI {
	
	public Model[] legs;
	
	private float maxRotation;
	private float multiplier;
	
	protected boolean leg;

	public EntityAIMoveLegs(Entity owner, Model...legsToMove) {
		this(owner, 45, 1, legsToMove);
	}
	
	public EntityAIMoveLegs(Entity owner, float maxRot, float multi, Model... legsToMove){
		super(owner);
		legs = legsToMove;
		maxRotation = maxRot;
		multiplier = multi;
	}
	
	@Override
	public void update(){
		if(!shouldMove())
			return;
		int t = 0;
		while(t < legs.length){
			Model leg1 = legs[t];
			Model leg2 = legs[t + 1];
			float mx = entity.onGround != 0 ? multiplier * Math.abs(entity.motionX) : multiplier * Math.abs(entity.motionY);
			if(leg){
				leg1.rotation += mx;
				leg2.rotation -= mx;
			}
			else {
				leg1.rotation -= mx;
				leg2.rotation += mx;
			}
			if(leg1.rotation > maxRotation || leg2.rotation > maxRotation){
				if(leg){
					leg1.rotation = maxRotation;
					leg2.rotation = -maxRotation;
				}
				else {
					leg1.rotation = -maxRotation;
					leg2.rotation = maxRotation;
				}
				leg = !leg;
			}
			if(leg1.rotation < 0 && entity.onWall == -1 || leg2.rotation < 0 && entity.onWall == -1){
				if(leg){
					leg1.rotation = maxRotation;
					leg2.rotation = 0;
				}
				else {
					leg1.rotation = 0;
					leg2.rotation = maxRotation;
				}
				leg = !leg;
			}
			if(leg1.rotation > 0 && entity.onWall == 1 || leg2.rotation > 0 && entity.onWall == 1){
				if(leg){
					leg1.rotation = 0;
					leg2.rotation = -maxRotation;
				}
				else {
					leg1.rotation = -maxRotation;
					leg2.rotation = 0;
				}
				leg = !leg;
			}
			t += 2;
		}
	}
	
	@Override
	public byte[] addExtraData(byte[] data, int i){
		data = Arrays.copyOf(data, i + 1);
		data[i] = (byte) (leg ? 1 : 0);
		return data;
	}
	
	@Override
	public int readExtraData(byte[] data, int i){
		leg = data[i] == 1;
		return i + 1;
	}
	
	public boolean shouldMove(){
		return entity.onGround < 0 || entity.onWall != 0;
	}
}
