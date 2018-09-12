package knokko.tile;

import knokko.client.ClientBoard;
import knokko.collission.Collider;
import knokko.collission.ColliderNull;

public class TileAir extends Tile {

	public TileAir() {
		super("air");
	}
	
	@Override
	public void paint(ClientBoard board, int tileX, int tileY){}
	
	@Override
	public boolean hasImage(){
		return false;
	}
	
	@Override
	public Collider getCollider(int x, int y){
		return new ColliderNull();
	}
}
