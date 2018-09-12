package knokko.entity;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Point2D.Float;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import knokko.client.ClientBoard;
import knokko.client.Model;
import knokko.entity.ai.*;
import knokko.util.PointUtils;
import knokko.util.Resources;
import knokko.world.World;

public class EntityPlayer extends EntityLiving implements IPlayer{
	
	public String name = "";
	
	public Color armorColor;

	public EntityPlayer(World worldIn, Float spawn) {
		super(worldIn, spawn);
		String resource = "sprites/entities/player/";
		Model body = new Model(resource + "body.png", new Point(0, 18), new Point(5, 9), this);
		models.add(new Model(resource + "leg.png", new Point(2, 18), new Point(3, 1), this).setParent(body));
		models.add(new Model(resource + "arm.png", new Point(2, 10), new Point(1, 2), this).setParent(body));
		models.add(body);
		models.add(new Model(resource + "head.png", new Point(2, -10), new Point(4, 9), this).setParent(body));
		models.add(new Model(resource + "saber arm.png", new Point(2, 10), new Point(1, 2), this).setParent(body));
		models.add(new Model(resource + "leg.png", new Point(2, 18), new Point(3, 1), this).setParent(body));
		addAI(new EntityAIMoveWalking(this));
		addAI(new EntityAIMoveLegs(this, models.get(0), models.get(5)));
		addAI(new EntityAIMoveArms(this, models.get(1), models.get(4)));
		width = 32;
		height = 50;
		Random random = new Random();
		armorColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
	}
	
	public void paintArmor(Color color){
		float[] rotations = new float[models.size()];
		int t = 0;
		while(t < models.size()){
			rotations[t] = models.get(t).rotation;
			++t;
		}
		models = new ArrayList<Model>();
		String resource = "sprites/entities/player/";
		Model body = new Model(Resources.getPaintedImage(resource + "body.png", color), "body", new Point(0, 18), new Point(5, 9), this);
		models.add(new Model(Resources.getPaintedImage(resource + "leg.png", color), "leg", new Point(2, 18), new Point(3, 1), this).setParent(body));
		models.add(new Model(Resources.getPaintedImage(resource + "arm.png", color), "arm", new Point(2, 10), new Point(1, 2), this).setParent(body));
		models.add(body);
		models.add(new Model(Resources.getPaintedImage(resource + "head.png", color), "head", new Point(2, -10), new Point(4, 9), this).setParent(body));
		models.add(new Model(Resources.getPaintedImage(resource + "saber arm.png", color), "saber arm", new Point(2, -10), new Point(1, 22), this).setParent(body));
		models.add(new Model(Resources.getPaintedImage(resource + "leg.png", color), "leg", new Point(2, 18), new Point(3, 1), this).setParent(body));
		t = 0;
		while(t < models.size()){
			models.get(t).rotation = rotations[t];
			++t;
		}
		((EntityAIMoveLegs)ai.get(2)).legs = new Model[]{models.get(0), models.get(5)};
		((EntityAIMoveArms)ai.get(3)).legs = new Model[]{models.get(1), models.get(4)};
	}

	@Override
	public float getMaxHealth() {
		return 100;
	}

	@Override
	public float getWeight() {
		return 80;
	}
	
	@Override
	public void paint(ClientBoard board){
		super.paint(board);
		Point screen = PointUtils.gameToScreenPoint(board.client, new Float(position.x, this.getCollider().maxY()));
		board.drawCenteredString(name, screen.x, (int) (screen.y - 10 * board.client.zoom), false, new Font("TimesRoman", 0, (int)(20 * board.client.zoom)), Color.GREEN);
	}
	
	@Override
	public byte[] addExtraData(byte[] data, int i){
		data = super.addExtraData(data, i);
		i = data.length;
		data = Arrays.copyOfRange(data, 0, data.length + 3);
		data[i + 0] = (byte) (armorColor.getRed() - 128);
		data[i + 1] = (byte) (armorColor.getGreen() - 128);
		data[i + 2] = (byte) (armorColor.getBlue() - 128);
		data = Arrays.copyOfRange(data, 0, data.length + 1 + 2 * (byte)name.length());
		int t1 = i + 3;
		data[t1] = (byte) name.length();
		int t2 = 0;
		while(t2 < name.length()){
			byte[] token = ByteBuffer.allocate(2).putChar(name.charAt(t2)).array();
			data[1 + t1] = token[0];
			data[2 + t1] = token[1];
			t1 += 2;
			++t2;
		}
		return data;
	}
	
	@Override
	public int readExtraData(byte[] data, int i){
		i = super.readExtraData(data, i);
		int red = data[i + 0] + 128;
		int green = data[i + 1] + 128;
		int blue = data[i + 2] + 128;
		armorColor = new Color(red, green, blue);
		paintArmor(armorColor);
		i += 3;
		byte length = data[i];
		i++;
		int t = 0;
		while(t < length){
			name += ByteBuffer.wrap(data, i, 2).getChar();
			++t;
			i += 2;
		}
		return i;
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	@Override
	public boolean equals(Object other){
		return other instanceof EntityPlayer && ((EntityPlayer)other).name.equals(name);
	}

	@Override
	public Float getPosition() {
		return position;
	}

	@Override
	public float getSpeed() {
		return 0.4f;
	}
}
