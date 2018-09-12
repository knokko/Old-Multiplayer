package knokko.entity.projectile;

import java.awt.geom.Point2D.Float;
import java.nio.ByteBuffer;
import java.util.Arrays;

import knokko.entity.Entity;
import knokko.server.ServerMain;
import knokko.world.World;

public abstract class EntityProjectile extends Entity implements IProjectile {
	
	public float damage;

	public EntityProjectile(World worldIn, Float spawn, Float target, float attackDamage) {
		super(worldIn, spawn);
		launch(target);
		damage = attackDamage;
	}
	
	public EntityProjectile(World world, Float spawn){
		super(world, spawn);
	}
	
	@Override
	public byte[] addExtraData(byte[] data, int index){
		data = super.addExtraData(data, index);
		index = data.length;
		data = Arrays.copyOf(data, data.length + 4);
		data = ByteBuffer.wrap(data).putFloat(index, damage).array();
		return data;
	}
	
	@Override
	public int readExtraData(byte[] data, int index){
		index = super.readExtraData(data, index);
		damage = ByteBuffer.wrap(data).getFloat(index);
		return index + 4;
	}
	
	@Override
	public boolean teleport(Float target, boolean test){
		int t = 0;
		while(t < world.entities.size()){
			Entity entity = world.entities.get(t);
			if(canAttack(entity) && entity.getCollider().hit(getCollider(target))){
				if(attack(entity)){
					inactive = true;
					return false;
				}
			}
			++t;
		}
		if(!super.teleport(target, test)){
			inactive = true;
			return false;
		}
		return true;
	}
	
	public void launch(Float target){
		float distance = (float) position.distance(target);
		float distanceX = target.x - position.x;
		float distanceY = target.y - position.y;
		motionX += getLaunchSpeed() * distanceX / distance;
		motionY += getLaunchSpeed() * distanceY / distance;
	}
	
	public float getLaunchSpeed(){
		return 1 / getWeight();
	}
	
	public boolean canAttack(Entity entity){
		return true;
	}
	
	public boolean attack(Entity entity){
		entity.attack(damage);
		if(!world.isClientWorld())
			ServerMain.instance.connection.updateClientEntities();
		return true;
	}
}
