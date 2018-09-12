package knokko.entity.ai;

import java.util.Arrays;

import knokko.client.Model;
import knokko.connection.Connection;
import knokko.entity.Entity;

public class EntityAIMoveArms extends EntityAIMoveLegs {
	
	private boolean increasedRotation;
	private boolean previousFacing;

	public EntityAIMoveArms(Entity owner, Model... armsToMove) {
		super(owner, 30, 0.5F, armsToMove);
	}
	
	@Override
	public boolean shouldMove(){
		return entity.onGround != 0 || entity.onWall != 0;
	}
	
	@Override
	public void update(){
		boolean flag = entity.onGround > 0 && increasedRotation;
		boolean l = entity.facingLeft;
		if(flag){
			int t = 0;
			while(t < legs.length){
				legs[t].rotation -= l ? 90 : -90;
				++t;
			}
		}
		super.update();
		if(flag){
			int t = 0;
			while(t < legs.length){
				legs[t].rotation += l ? 90 : -90;
				if(entity.facingLeft && !previousFacing)
					legs[t].rotation += 180;
				++t;
			}
		}
		if(entity.onGround > 0 && !increasedRotation){
			int t = 0;
			while(t < legs.length){
				legs[t].rotation += l ? 90 : -90;
				++t;
			}
			increasedRotation = true;
		}
		if(increasedRotation && entity.onGround <= 0){
			int t = 0;
			while(t < legs.length){
				legs[t].rotation -= l ? 90 : -90;
				++t;
			}
			increasedRotation = false;
		}
		previousFacing = entity.facingLeft;
	}
	
	@Override
	public byte[] addExtraData(byte[] data, int i){
		data = Arrays.copyOf(data, i + 1);
		data[i] = Connection.byteFromBinair(leg, increasedRotation, previousFacing, false, false, false, false, false);
		return data;
	}
	
	@Override
	public int readExtraData(byte[] data, int i){
		boolean[] bools = Connection.byteToBinair(data[i]);
		leg = bools[0];
		increasedRotation = bools[1];
		previousFacing = bools[2];
		return i + 1;
	}
}
