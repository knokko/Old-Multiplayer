package knokko.connection;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Point;
import java.awt.geom.Point2D.Float;

import knokko.client.ClientMain;
import knokko.entity.Entity;
import knokko.entity.EntityPlayer;
import knokko.entity.enemy.EntitySoldier;
import knokko.entity.projectile.EntityLaser;
import knokko.server.ServerMain;
import knokko.util.Resources;

public class Connection {
	
	public static final short[] BYTES = new short[]{16384,8192,4096,2048,1024,512,256,128,64,32,16,8,4,2,1};
	
	public static final short MESSAGE_WORLD = 0;
	public static final short MESSAGE_SET_TILE = 1;
	public static final short MESSAGE_DISCONNECT = 2;
	public static final short MESSAGE_WALK = 3;
	public static final short MESSAGE_MOTION_X = 4;
	public static final short MESSAGE_MOTION_Y = 5;
	public static final short MESSAGE_MOTION = 6;
	public static final short MESSAGE_SPAWN_ENTITY = 7;
	public static final short MESSAGE_BREAK = 8;
	public static final short MESSAGE_REMOVE_ENTITY = 8;
	public static final short MESSAGE_UPDATE_ENTITIES = 9;
	public static final short MESSAGE_CHAT = 10;
	public static final short MESSAGE_USERNAME = 11;
	public static final short MESSAGE_ENABLE_BUILD_MODE = 12;
	public static final short MESSAGE_DISABLE_BUILD_MODE = 13;
	public static final short MESSAGE_SHOOT = 14;
	public static final short MESSAGE_FILL_TILES = 15;
	
	public static final short ENCODING_SHORT_MAP = 0;
	
	public final ServerMain server;
	public ArrayList<ConnectionServer> connections = new ArrayList<ConnectionServer>();
	
	public static ConnectionServer openServerConnection(ServerMain server){
		ConnectionServer connection = new ConnectionServer(server);
		connection.start();
		return connection;
	}
	
	public static ConnectionClient openClientConnection(ClientMain client, String name){
		client.console.println("Opening client connection...");
		ConnectionClient connection = new ConnectionClient(client, name);
		connection.start();
		client.console.println("Opened client connection.");
		return connection;
	}
	
	public static boolean[] byteToBinair(byte b){
		boolean[] bools = new boolean[8];
		if(b >= 0)
			bools[7] = true;
		else {
			b++;
			b *= -1;
		}
		byte t = 8;
		while(t < BYTES.length){
			if(b >= BYTES[t]){
				b -= BYTES[t];
				bools[t - 8] = true;
			}
			++t;
		}
		return bools;
	}
	
	public static boolean[] shortToBinair(short s){
		boolean[] bools = new boolean[16];
		if(s >= 0)
			bools[BYTES.length] = true;
		else {
			s++;
			s *= -1;
		}
		byte t = 0;
		while(t < BYTES.length){
			if(s >= BYTES[t]){
				s -= BYTES[t];
				bools[t] = true;
			}
			++t;
		}
		return bools;
	}
	
	public static short shortFromBinair(boolean... bools){
		short s = 0;
		int t = 0;
		while(t < BYTES.length){
			if(bools[t])
				s += BYTES[t];
			++t;
		}
		if(!bools[15]){
			s *= -1;
			s--;
		}
		return s;
	}
	
	public static byte byteFromBinair(boolean... bools){
		byte b = 0;
		int t = 8;
		while(t < BYTES.length){
			if(bools[t - 8])
				b += BYTES[t];
			++t;
		}
		if(!bools[7]){
			b *= -1;
			b--;
		}
		return b;
	}
	
	public static byte[] shortToBytes(short s){
		boolean[] bools = shortToBinair(s);
		return new byte[]{byteFromBinair(Arrays.copyOfRange(bools, 0, 8)), byteFromBinair(Arrays.copyOfRange(bools, 8, 16))};
	}
	
	public static short bytesToShort(byte... bytes){
		boolean[] bools1 = byteToBinair(bytes[0]);
		boolean[] bools2 = byteToBinair(bytes[1]);
		boolean[] bools = new boolean[16];
		System.arraycopy(bools1, 0, bools, 0, 8);
		System.arraycopy(bools2, 0, bools, 8, 8);
		return shortFromBinair(bools);
	}
	
	public static short getEntityID(Entity entity){
		if(entity instanceof EntityPlayer)
			return 0;
		if(entity instanceof EntitySoldier)
			return 1;
		if(entity instanceof EntityLaser)
			return 2;
		return -1;
	}
	
	public static Entity getEntityFromID(short id){
		if(id == 0)
			return new EntityPlayer(null, new Float());
		if(id == 1)
			return new EntitySoldier(null, new Float());
		if(id == 2)
			return new EntityLaser(null, null, new Float());
		System.out.println("id = " + id);
		return null;
	}
	
	public Connection(ServerMain serverMain){
		server = serverMain;
	}
	
	public void disconnect(){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).disconnect();
			++t;
		}
	}
	
	public void sendToClients(byte... data){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).sendToClient(data);
			++t;
		}
	}
	
	public void sendToClients(byte[]... data){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).sendToClient(data);
			++t;
		}
	}
	
	public void sendMotionMessages(int entityID, boolean updateX, boolean updateY){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).sendMotionMessage(entityID, updateX, updateY);
			++t;
		}
	}
	
	public void sendWalkMessages(int entityID, EntityPlayer player){
		byte movement = 0;
		if(player.moveY == 0 && player.moveX != 0)
			movement = 1;
		if(player.moveY != 0 && player.moveX == 0)
			movement = 2;
		if(player.moveY != 0 && player.moveX != 0)
			movement = 3;
		if(player.moveX < 0)
			movement *= -1;
		int t = 0;
		while(t < connections.size()){
			connections.get(t).sendWalkMessage(entityID, movement);
			++t;
		}
	}
	
	public void sendEntitySpawnMessages(Entity entity){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).sendEntitySpawnMessage(entity);
			++t;
		}
	}
	
	public void sendEntityRemoveMessages(int index){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).sendEntityRemoveMessage(index);
			++t;
		}
	}
	
	public void sendTileMessages(short tile, Point tilePosition){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).setTileInClient(tile, (short)tilePosition.x, (short)tilePosition.y);
			++t;
		}
	}
	
	public void sendFillTileMessages(short tile, short minX, short minY, short maxX, short maxY){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).setTilesInClient(tile, minX, minY, maxX, maxY);
			++t;
		}
	}
	
	public void updateClientWorlds(){
		int t = 0;
		while(t < connections.size()){
			connections.get(t).sendWorld();
			++t;
		}
	}
	
	public void updateClientEntities(){
		byte[] data = shortToBytes(MESSAGE_UPDATE_ENTITIES);
		data = Resources.storeEntities(server.world, 2, data);
		sendToClients(data);
	}
	
	public void sendChatMessage(String message){
		byte[] bytes = new byte[2 + message.length() * 2];
		byte[] id = shortToBytes(MESSAGE_CHAT);
		bytes[0] = id[0];
		bytes[1] = id[1];
		int t = 0;
		while(t < message.length()){
			byte[] token = ByteBuffer.allocate(2).putChar(message.charAt(t)).array();
			bytes[2 + t * 2] = token[0];
			bytes[3 + t * 2] = token[1];
			++t;
		}
		sendToClients(bytes);
	}
}
