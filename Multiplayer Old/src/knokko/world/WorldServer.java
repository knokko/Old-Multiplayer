package knokko.world;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;


import knokko.entity.Entity;
import knokko.server.ServerMain;
import knokko.util.Resources;

public class WorldServer extends World {
	
	public final ServerMain server;

	public WorldServer(int width, int height, ServerMain serverMain, String name) {
		super(width, height, 0, System.nanoTime(), 0, name);
		server = serverMain;
	}
	
	@Override
	public void spawnEntity(Entity entity){
		super.spawnEntity(entity);
		server.connection.sendEntitySpawnMessages(entity);
	}
	
	@Override
	public boolean removeEntity(Entity entity){
		int index = entities.indexOf(entity);
		if(index >= 0){
			removeEntity(index);
			return true;
		}
		return false;
	}
	
	@Override
	public Entity removeEntity(int index){
		server.connection.sendEntityRemoveMessages(index);
		return super.removeEntity(index);
	}
	
	@Override
	public void setTile(short tile, Point tilePosition){
		super.setTile(tile, tilePosition);
		server.connection.sendTileMessages(tile, tilePosition);
	}
	
	@Override
	public void setTiles(short tile, int minX, int minY, int maxX, int maxY){
		super.setTiles(tile, minX, minY, maxX, maxY);
		server.connection.sendFillTileMessages(tile, (short) minX, (short) minY, (short) maxX, (short) maxY); 
	}
	
	public boolean save(String fileName){
		try {
			File folder = new File("levels");
			if(!folder.exists())
				folder.mkdir();
			FileOutputStream stream = new FileOutputStream("levels/" + fileName + (ServerMain.instance.isBuilding ? ".level" : ".world"));
			stream.write(Resources.storeWorldData(this));
			stream.close();
			return true;
		} catch(Exception ex){
			ex.printStackTrace(server.board.getConsoleStream());
			return false;
		}
	}
	
	public boolean save(){
		return save(worldName);
	}

	@Override
	public boolean isClientWorld() {
		return false;
	}
}
