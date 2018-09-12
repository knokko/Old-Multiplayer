package knokko.entity;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D.Float;
import java.nio.ByteBuffer;
import java.util.Arrays;

import knokko.client.ClientBoard;
import knokko.collission.Collider;
import knokko.entity.ai.EntityAIMove;
import knokko.util.PointUtils;
import knokko.world.World;

public abstract class EntityLiving extends Entity {
	
	protected float health;

	public EntityLiving(World worldIn, Float spawn) {
		super(worldIn, spawn);
		addAI(new EntityAIMove(this, null, getSpeed()));
		health = getMaxHealth();
	}
	
	@Override
	public void attack(float damage){
		health -= damage;
		if(health <= 0)
			disable();
	}
	
	@Override
	public void paint(ClientBoard board){
		super.paint(board);
		Collider c = getCollider();
		Point screen = PointUtils.gameToScreenPoint(board.client, new Float(c.minX(), c.maxY()));
		Point screen2 = PointUtils.gameToScreenPoint(board.client, new Float(c.maxX(), c.minY()));
		int h = (int) (health / getMaxHealth() * width * board.client.zoom);
		int ymin = (int) (screen.y - 15 * board.client.zoom);
		int ymax = (int) (screen.y - 5 * board.client.zoom);
		board.fillRect(screen.x, ymin, (int) (screen.x + width * board.client.zoom), ymax, Color.RED, false);
		board.fillRect(screen.x, ymin, screen.x + h, ymax, Color.GREEN, false);
		board.drawRect(screen.x, ymin, (int) (screen.x + width * board.client.zoom), ymax, Color.BLACK, false);
		board.drawRect(screen.x, screen.y, screen2.x, screen2.y, Color.RED, false);
	}
	
	@Override
	public byte[] addExtraData(byte[] data, int i){
		data = super.addExtraData(data, i);
		i = data.length;
		data = Arrays.copyOfRange(data, 0, data.length + 4);
		byte[] hp = ByteBuffer.allocate(4).putFloat(health).array();
		data[i] = hp[0];
		data[i + 1] = hp[1];
		data[i + 2] = hp[2];
		data[i + 3] = hp[3];
		return data;
	}
	
	@Override
	public int readExtraData(byte[] data, int i){
		i = super.readExtraData(data, i);
		health = ByteBuffer.wrap(data, i, 4).getFloat();
		return i + 4;
	}
	
	public EntityAIMove getMoveHelper(){
		return (EntityAIMove) ai.get(0);
	}
	
	public abstract float getMaxHealth();
	public abstract float getWeight();
	public abstract float getSpeed();
}
