package knokko.util;

import java.awt.Point;
import java.awt.geom.Point2D.Float;

import knokko.client.ClientMain;

public final class PointUtils {
	
	public static Point gameToScreenPoint(ClientMain client, Float point){
		int tx = (int) (((point.x - client.camera.x) * client.zoom + client.getWidth() / 2));
		int ty = (int) ((-(point.y - client.camera.y) * client.zoom + client.getHeight() / 2));
		return new Point(tx, ty);
	}
	
	public static Point gameToScreenPoint(ClientMain client, Point point){
		return gameToScreenPoint(client, new Float(point.x, point.y));
	}
	
	public static Float screenToGamePoint(ClientMain client, Point point) {
		if(!client.isUndecorated()){
			point.x -= 8;
			point.y -= 30;
		}
		if(point == null)
			return null;
		float differenceX = point.x - client.getWidth() / 2;
		float factorX = (float) (differenceX / client.zoom);
		float originalX = factorX + client.camera.x;
		
		float differenceY = -(point.y - client.getHeight() / 2);
		float factorY = (float) (differenceY / client.zoom);
		float originalY = factorY + client.camera.y;
		return new Float(originalX, originalY);
	}
	
	public static Point tileToScreenPoint(ClientMain client, Point point){
		return gameToScreenPoint(client, new Point(point.x * 60, point.y * 60));
	}
}
