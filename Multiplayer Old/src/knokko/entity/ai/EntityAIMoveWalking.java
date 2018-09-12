package knokko.entity.ai;

import knokko.entity.Entity;

public class EntityAIMoveWalking extends EntityAI {

	public EntityAIMoveWalking(Entity owner) {
		super(owner);
	}
	
	@Override
	public void update(){
		if(entity.moveX != 0 && entity.onGround != 0){
			entity.motionX += entity.moveX;
			entity.facingLeft = entity.moveX < 0;
			if(!entity.move(entity.moveX, 0, true)){
				entity.onGround = 0;
				entity.onWall = (byte) (entity.moveX > 0 ? 1 : -1);
			}
		}
		if(entity.moveY != 0 && entity.onGround != 0)
			entity.motionY += entity.moveY;
		if(entity.onGround != 0)
			entity.onWall = 0;
		if(entity.moveX != 0 && entity.onWall != 0 && entity.moveY == 0)
			entity.motionY += entity.moveX * entity.onWall * 0.5f;
		if(entity.moveY != 0 && entity.onWall != 0){
			entity.motionY = entity.moveY * 1;
			entity.motionX = -entity.onWall * 2;
			entity.onWall = 0;
		}
	}
}
