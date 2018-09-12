package knokko.client;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D.Float;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import knokko.entity.Entity;
import knokko.util.PointUtils;
import knokko.util.Resources;

public class Model {
	
	public Model parent;
	
	public float rotation;
	public BufferedImage image;
	public BufferedImage image2;
	public BufferedImage imageLeft;
	public BufferedImage imageLeft2;
	public Point rotationPoint;
	public Point basePoint;
	public Entity entity;
	public String name;
	public float prevZoom;
	
	public Model(String sprite, Point base, Point rotate, Entity owner) {
		this(Resources.getBuffImage(sprite), sprite.substring(sprite.lastIndexOf("/") + 1, sprite.length() - 4), base, rotate, owner);
	}
	
	public Model(BufferedImage baseImage, String modelName, Point base, Point rotate, Entity owner){
		rotationPoint = rotate;
		basePoint = base;
		entity = owner;
		name = modelName;
		image = baseImage;
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-image.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		imageLeft = op.filter(image, null);
		setZoom();
	}
	
	public Model setParent(Model parentModel){
		parent = parentModel;
		return this;
	}
	
	public void paint(ClientBoard board){
		if(prevZoom != board.client.zoom)
			setZoom();
		prevZoom = board.client.zoom;
		Float base = base();
		Point screen = PointUtils.gameToScreenPoint(board.client, parent == null ? (new Float(entity.position.x + base.x - (parent == null ? image.getWidth() / 2 : 0), entity.position.y + base.y)) : base);
		/*
		AffineTransform tx = new AffineTransform();
		tx.setToRotation(Math.toRadians(getRotation()), rotatePointX() * board.client.zoom, rotationPoint.y * board.client.zoom);
		AffineTransformOp op = new AffineTransformOp(tx, 2);
		gr.drawImage(entity.facingLeft ? imageLeft2 : image2, op, screen.x, screen.y);
		*/
		board.drawImage(screen.x, screen.y, rotatePointX(), rotationPoint.y, rotation, entity.facingLeft ? imageLeft2 : image2);
	}
	
	public void paintSimple(ClientBoard board){
		if(prevZoom != board.client.zoom)
			setZoom();
		prevZoom = board.client.zoom;
		Float base = base();
		Point screen = PointUtils.gameToScreenPoint(board.client, parent == null ? (new Float(entity.position.x + base.x - (parent == null ? image.getWidth() / 2 : 0), entity.position.y + base.y)) : base);
		
		//gr.drawImage(entity.facingLeft ? imageLeft2 : image2, screen.x, screen.y, null);
		board.drawImage(screen.x, screen.y, (int) (screen.x + image.getWidth() * board.client.zoom), (int) (screen.y + image.getHeight() * board.client.zoom), entity.facingLeft ? imageLeft2 : image2, false);
	}
	
	public Float base(){
		if(parent != null){
			double dx = basePoint.x + rotationPoint.x - parent.rotationPoint.x;
			if(entity.facingLeft)
				dx = -dx;
			double dy = basePoint.y + rotationPoint.y - parent.rotationPoint.y;
			double distance = Math.hypot(dx, dy);
			double extraR = Math.toDegrees(Math.atan(dy / dx));
			double r = parent.getRotation() + extraR;
			if(entity.facingLeft)
				r -= 180;
			float ex = (float) (Math.cos(Math.toRadians(r)) * distance);
			float ey = (float) (-Math.sin(Math.toRadians(r)) * distance);
			Float parentBase = parent.base();
			Float test = new Float(entity.position.x + parentBase.x + parent.rotatePointX() - parent.image.getWidth() / 2, entity.position.y + parentBase.y - parent.rotationPoint.y);
			return new Float(test.x + ex - rotatePointX(), test.y + ey + rotationPoint.y);
		}
		return new Float(entity.facingLeft ? -basePoint.x : basePoint.x, basePoint.y);
	}
	
	public float rotatePointX(){
		return entity.facingLeft ? image.getWidth() - rotationPoint.x : rotationPoint.x;
	}
	
	public void setImage(String sprite){
		name = sprite.substring(sprite.lastIndexOf("/") + 1, sprite.length() - 4);
		image = Resources.getBuffImage(sprite);
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-image.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		imageLeft = op.filter(image, null);
		setZoom();
	}
	
	public void setZoom(){
		image2 = scaleToSize(image);
		imageLeft2 = scaleToSize(imageLeft);
	}
	
	public float getRotation(){
		if(parent != null)
			return rotation + parent.getRotation();
		return rotation;
	}
	
	private BufferedImage scaleToSize(BufferedImage uploadImage){
		  AffineTransform atx=new AffineTransform();
		  atx.scale(ClientMain.instance.zoom, ClientMain.instance.zoom);
		  AffineTransformOp afop=new AffineTransformOp(atx,AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		  uploadImage=afop.filter(uploadImage,null);
		  return uploadImage;
	}
}
