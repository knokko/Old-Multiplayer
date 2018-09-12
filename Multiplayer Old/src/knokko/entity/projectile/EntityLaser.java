package knokko.entity.projectile;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D.Float;
import java.util.Arrays;

import knokko.client.ClientBoard;
import knokko.entity.Entity;
import knokko.entity.IPlayer;
import knokko.entity.enemy.IEnemy;
import knokko.util.PointUtils;
import knokko.world.World;

public class EntityLaser extends EntityProjectile {
	
	public Float previousPosition;
	
	public boolean shotByPlayer;

	public EntityLaser(World world, Entity shooter, Float target) {
		super(world, shooter != null ? shooter.position : new Float(), target, 10);
		shotByPlayer = shooter instanceof IPlayer;
		previousPosition = new Float(position.x, position.y);
		width = 1;
		height = 1;
	}
	
	@Override
	public void update(){
		previousPosition = new Float(position.x, position.y);
		super.update();
	}
	
	@Override
	public void paint(ClientBoard board){
		Point now = PointUtils.gameToScreenPoint(board.client, position);
		Point prev = PointUtils.gameToScreenPoint(board.client, previousPosition);
		board.drawLine(Color.RED, now, prev);
	}

	@Override
	public float getLaunchSpeed() {
		return 25f;
	}
	
	@Override
	public boolean canAttack(Entity target){
		return shotByPlayer ? target instanceof IEnemy : target instanceof IPlayer;
	}
	
	@Override
	public float getWeight(){
		return 0.00f;
	}
	
	@Override
	public float frictionMultiplier(){
		return 0;
	}
	
	@Override
	public byte[] addExtraData(byte[] data, int index){
		data = super.addExtraData(data, index);
		index = data.length;
		data = Arrays.copyOf(data, data.length + 1);
		data[index] = (byte) (shotByPlayer ? 1 : 0);
		return data;
	}
	
	@Override
	public int readExtraData(byte[] data, int index){
		index = super.readExtraData(data, index);
		shotByPlayer = data[index] == 1;
		return index + 1;
	}
}
