package knokko.entity.enemy;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D.Float;

import knokko.client.Model;
import knokko.entity.ai.EntityAIMoveArms;
import knokko.entity.ai.EntityAIMoveLegs;
import knokko.entity.ai.EntityAIMoveWalking;
import knokko.util.Resources;
import knokko.world.World;

public class EntitySoldier extends EntityEnemy {

	public EntitySoldier(World worldIn, Float spawn) {
		super(worldIn, spawn);
		String resource = "sprites/entities/enemies/soldier/";
		Model body = new Model(Resources.getPaintedImage(resource + "body.png", Color.RED), "body", new Point(0, 18), new Point(5, 9), this);
		models.add(new Model(Resources.getPaintedImage(resource + "leg.png", Color.RED), "leg", new Point(2, 18), new Point(3, 1), this).setParent(body));
		models.add(new Model(Resources.getPaintedImage(resource + "arm.png", Color.RED), "arm", new Point(2, 10), new Point(1, 2), this).setParent(body));
		models.add(body);
		models.add(new Model(Resources.getPaintedImage(resource + "head.png", Color.RED), "head", new Point(2, -10), new Point(4, 9), this).setParent(body));
		models.add(new Model(Resources.getPaintedImage(resource + "arm.png", Color.RED), "arm", new Point(2, 10), new Point(1, 2), this).setParent(body));
		models.add(new Model(Resources.getPaintedImage(resource + "leg.png", Color.RED), "leg", new Point(2, 18), new Point(3, 1), this).setParent(body));
		addAI(new EntityAIMoveWalking(this));
		addAI(new EntityAIMoveLegs(this, models.get(0), models.get(5)));
		addAI(new EntityAIMoveArms(this, models.get(1), models.get(4)));
		width = 32;
		height = 50;
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
	public float getSpeed() {
		return 0.3f;
	}

}
