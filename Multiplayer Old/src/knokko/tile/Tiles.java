package knokko.tile;

import java.util.ArrayList;

public final class Tiles {
	
	static final ArrayList<Tile> tiles = new ArrayList<Tile>();
	
	public static final Tile air = new TileAir();
	public static final Tile black_bricks = new Tile("black_bricks");
	public static final Tile yellow_bricks = new Tile("yellow_bricks");
	
	public static final Tile fromId(int id){
		return tiles.get(id) != null ? tiles.get(id) : air;
	}
	
	public static final int tiles(){
		return tiles.size();
	}
}
