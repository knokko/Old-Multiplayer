package knokko.world;

import knokko.client.ClientBoard;
import knokko.client.ClientMain;
import knokko.collission.Collider;
import knokko.connection.ConnectionServer;
import knokko.entity.Entity;
import knokko.entity.EntityPlayer;
import knokko.server.ServerMain;
import knokko.tile.Tile;
import knokko.tile.Tiles;
import knokko.util.Resources;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Point2D.Float;
import java.io.FileInputStream;
import java.util.ArrayList;

public abstract class World {
	
	public ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public String worldName;
	
	public short[][] tiles;
	
	public long ticks;
	public long startTime;
	public long passedTime;
	
	public static World loadWorld(String fileName, Object clientOrServer){
		try {
			FileInputStream stream = new FileInputStream("levels/" + fileName);
			byte[] data = new byte[stream.available()];
			stream.read(data);
			stream.close();
			return Resources.loadWorldData(data, clientOrServer, fileName);
		} catch(Exception ex){
			ex.printStackTrace();
			World world;
			if(clientOrServer instanceof ClientMain)
				world = new WorldClient(100, 50, 0, 0, System.currentTimeMillis(), (ClientMain) clientOrServer, fileName);
			else
				world =  new WorldServer(100, 50, (ServerMain) clientOrServer, fileName);
			world.loadBasicWorld();
			return world;
		}
	}
	
	public World(int width, int height, long ticks, long startTime, long passedTime, String name){
		tiles = new short[width][height];
		if(name.length() >= 6)
			worldName = name.substring(0, name.length() - 6);
		this.ticks = ticks;
		this.startTime = startTime;
		this.passedTime = passedTime;
	}
	
	public void update(){
		int t = 0;
		while(t < entities.size()){
			Entity entity = entities.get(t);
			if(entity.inactive){
				entities.remove(t);
				if(!isClientWorld() && entity instanceof EntityPlayer){
					String name = ((EntityPlayer)entity).name;
					Color color = ((EntityPlayer)entity).armorColor;
					ConnectionServer connection = ServerMain.instance.getConnectionByName(name);
					connection.player = ServerMain.instance.spawnPlayer();
					connection.player.name = name;
					connection.player.armorColor = color;
				}
			}
			else {
				entity.update();
				++t;
			}
		}
		++ticks;
	}
	
	public void paint(ClientBoard board){
		int x = 0;
		while(x < tiles.length){
			int y = 0;
			while(y < tiles[x].length){
				Tiles.fromId(tiles[x][y]).paint(board, x, y);
				++y;
			}
			++x;
		}
		int t = 0;
		while(t < entities.size()){
			entities.get(t).paintIfInRange(board);
			++t;
		}
	}
	
	public void loadBasicWorld(){
		tiles = new short[100][50];
		int x = 0;
		while(x < 100){
			tiles[x][0] = 1;
			tiles[x][1] = 1;
			++x;
		}
	}
	
	public void spawnEntity(Entity entity){
		entities.add(entity);
	}
	
	public boolean removeEntity(Entity entity){
		return entities.remove(entity);
	}
	
	public Entity removeEntity(int index){
		return entities.remove(index);
	}
	
	public void setTile(Tile tile, Float position){
		setTile(tile.id, new Point((int)position.x / 60, (int)position.y / 60));
	}
	
	public void setTile(Tile tile, Point tilePosition){
		setTile(tile.id, tilePosition);
	}

	public void setTile(short tile, Point tilePosition) {
		try {
			tiles[tilePosition.x][tilePosition.y] = tile;
		} catch(Exception ex){}
	}
	
	public void setTiles(short tile, int minX, int minY, int maxX, int maxY){
		int x = minX;
		while(x <= maxX){
			int y = minY;
			while(y <= maxY){
				try {
					tiles[x][y] = tile;
				} catch(Exception ex){}
				++y;
			}
			++x;
		}
	}
	
	public Tile getTile(float x, float y){
		try {
			return Tiles.fromId(tiles[(int) (x / 60)][(int) (y / 60)]);
		} catch(Exception ex){
			return Tiles.air;
		}
	}

	public ArrayList<Collider> getColliders(Collider collider){
		ArrayList<Collider> colliders = new ArrayList<Collider>();
		float x = collider.minX();
		while(x <= collider.maxX()){
			float y = collider.minY();
			while(y <= collider.maxY()){
				Collider c = getTile(x, y).getCollider(x, y);
				if(!colliders.contains(c))
					colliders.add(c);
				y += 60;
				if(y > collider.maxY() && y < collider.maxY() + 60){
					y = collider.maxY();
				}
			}
			x += 60;
			if(x > collider.maxX() && x < collider.maxX() + 60){
				x = collider.maxX();
			}
		}
		//TODO add tile entities first
		/*
		int t = 0;
		while(t < tileEntities.size()){
			if(tileEntities.get(t).getCollider().hit(collider))
				colliders.add(tileEntities.get(t).getCollider());
			++t;
		}
		*/
		return colliders;
	}
	
	public Float getSpawn(){
		return new Float(250, 250);
	}
	
	public EntityPlayer getPlayerByName(String name){
		int t = 0;
		while(t < entities.size()){
			Entity entity = entities.get(t);
			if(entity instanceof EntityPlayer && ((EntityPlayer) entity).name.equals(name))
				return (EntityPlayer) entity;
			++t;
		}
		return null;
	}
	
	public abstract boolean isClientWorld();
}
