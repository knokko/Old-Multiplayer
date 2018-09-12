package knokko.entity.enemy;

import java.awt.geom.Point2D.Float;
import java.nio.ByteBuffer;
import java.util.Arrays;

import knokko.entity.Entity;
import knokko.entity.EntityLiving;
import knokko.entity.IPlayer;
import knokko.entity.projectile.EntityProjectile;
import knokko.world.World;

public abstract class EntityEnemy extends EntityLiving implements IEnemy{
	
	public IPlayer target;
	protected int targetIndex = -1;

	public EntityEnemy(World worldIn, Float spawn) {
		super(worldIn, spawn);
	}
	
	@Override
	public void update(){
		if(targetIndex >= 0 && target == null && targetIndex < world.entities.size()){
			if(world.entities.get(targetIndex) instanceof IPlayer)
				target = (IPlayer) world.entities.get(targetIndex);
			targetIndex = -1;
		}
		if(target == null || target.getPosition().distance(position) > getFollowDistance())
			target = findTarget();
		if(target != null)
			getMoveHelper().setDestination(target.getPosition());
		super.update();
	}
	
	@Override
	public byte[] addExtraData(byte[] data, int index){
		data = super.addExtraData(data, index);
		index = data.length;
		data = Arrays.copyOf(data, index + 4);
		data = ByteBuffer.wrap(data).putInt(index, targetIndex).array();
		return data;
	}
	
	@Override
	public int readExtraData(byte[] data, int index){
		index = super.readExtraData(data, index);
		targetIndex = ByteBuffer.wrap(data).getInt(index);
		return index + 4;
	}
	
	public EntityProjectile getAttackProjectile(Entity target){
		return null;
	}
	
	public IPlayer findTarget(){
		int t = 0;
		double distance = getFollowDistance();
		IPlayer target = null;
		while(t < world.entities.size()){
			Entity entity = world.entities.get(t);
			if(entity instanceof IPlayer && entity.position.distance(position) < distance){
				target = (IPlayer) entity;
				distance = entity.position.distance(position);
				targetIndex = t;
			}
			++t;
		}
		return target;
	}
	
	public double getFollowDistance(){
		return 1000;
	}
}
