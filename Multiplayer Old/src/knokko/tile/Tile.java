package knokko.tile;

import java.awt.Image;
import java.awt.Point;

import knokko.client.ClientBoard;
import knokko.collission.Collider;
import knokko.collission.ColliderBox;
import knokko.util.PointUtils;
import knokko.util.Resources;

public class Tile {
	
	private static short nextId;
	
	public Image image;
	public String name;
	
	public final short id;

	public Tile(String name) {
		if(hasImage())
			image = Resources.getImage("sprites/tiles/" + name + ".png");
		this.name = name;
		id = nextId;
		Tiles.tiles.add(this);
		++nextId;
	}
	
	public void paint(ClientBoard board, int tileX, int tileY){
		Point screen = PointUtils.tileToScreenPoint(board.client, new Point(tileX, tileY));
		Point screen2 = PointUtils.tileToScreenPoint(board.client, new Point(tileX + 1, tileY + 1));
		board.drawImage(screen.x, screen.y, screen2.x, screen2.y, image, false);
	}
	
	public boolean hasImage(){
		return true;
	}

	public Collider getCollider(int x, int y) {
		x /= 60; x *= 60;
		y /= 60; y *= 60;
		return new ColliderBox(x, x + 60, y, y + 60);
	}

	public Collider getCollider(float x, float y) {
		return getCollider((int)x, (int)y);
	}
}
