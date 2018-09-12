package knokko.connection;

import java.awt.Point;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import knokko.client.ClientMain;
import knokko.entity.Entity;
import knokko.entity.EntityPlayer;
import knokko.util.Resources;
import static knokko.connection.Connection.*;

public class ConnectionClient extends Thread {
	
	public ClientMain client;
	public Socket socket;
	public InputStream input;
	public OutputStream output;
	
	public String name;
	
	private boolean isStopping;
	
	public ConnectionClient(ClientMain clientMain, String userName) {
		client = clientMain;
		name = userName;
	}
	
	 @Override
	 public void run(){
		 String host = "192.168.2.39";
		 try {
			client.console.println("start socket constructor...");
			socket = new Socket(host, 40537);
			client.console.println("get input stream...");
			input = socket.getInputStream();
			client.console.println("get output stream...");
			output = socket.getOutputStream();
			client.console.println("got output stream, send usernames...");
			sendUserNameMessage();
			client.console.println("start loop...");
			while(true){
				byte[] bytes = new byte[100000];
				int i = input.read(bytes);
				client.console.println("received first message");
				try {
					if(i > 0)
						receiveMessage(Arrays.copyOfRange(bytes, 0, i));
				} catch( Exception ex){
					client.console.println("The following error occured:");
					ex.printStackTrace(client.console);
				}
				if(isStopping)
					return;
			}
		} catch (Exception e) {
			client.console.println("The following connection error occured:");
			e.printStackTrace(client.console);
			client.console.println("The game should shut itself down now.");
			client.dispose();
		} 
	 }
	 
	 public void sendToServer(byte... bytes){
		 try {
			byte[] data = new byte[bytes.length + 2];
			System.arraycopy(bytes, 0, data, 2, bytes.length);
			byte[] size = shortToBytes((short)bytes.length);
			data[0] = size[0];
			data[1] = size[1];
			if(output != null)
				output.write(data);
			} catch (Exception e) {
				if(!(e instanceof NullPointerException))
					e.printStackTrace(client.console);
			}
	 }
	 
	 public void sendToServer(byte[]... bytes){
		 int size = 0;
		 int t = 0;
		 while(t < bytes.length){
			 size += bytes[t].length;
			 ++t;
		 }
		 byte[] data = new byte[size];
		 t = 0;
		 int i = 0;
		 while(t < bytes.length){
			 int t1 = 0;
			 while(t1 < bytes[t].length){
				 data[i] = bytes[t][t1];
				 ++i;
				 ++t1;
			 }
			 ++t;
		 }
		 sendToServer(data);
	 }
	 
	 public void setTileInServer(short tile, short x, short y){
		 byte[] id = shortToBytes(MESSAGE_SET_TILE);
		 byte[] btile = shortToBytes(tile);
		 byte[] bx = shortToBytes(x);
		 byte[] by = shortToBytes(y);
		 sendToServer(id, btile, bx, by);
	 }
	 
	 public void setTilesInServer(short tile, short x1, short y1, short x2, short y2){
		 byte[] id = shortToBytes(MESSAGE_FILL_TILES);
		 byte[] btile = shortToBytes(tile);
		 short minX = x1 > x2 ? x2 : x1;
		 short minY = y1 > y2 ? y2 : y1;
		 short maxX = x1 > x2 ? x1 : x2;
		 short maxY = y1 > y2 ? y1 : y2;
		 byte[] bx = shortToBytes(minX);
		 byte[] by = shortToBytes(minY);
		 byte[] bx2 = shortToBytes(maxX);
		 byte[] by2 = shortToBytes(maxY);
		 sendToServer(id, btile, bx, by, bx2, by2);
	 }
	 
	 public void setEntityInServer(short entityId, float x, float y){
		 byte[] id = shortToBytes(MESSAGE_SPAWN_ENTITY);
		 byte[] entity = shortToBytes(entityId);
		 byte[] ax = ByteBuffer.allocate(4).putFloat(x).array();
		 byte[] ay = ByteBuffer.allocate(4).putFloat(y).array();
		 sendToServer(id, entity, ax, ay);
	 }
	 
	 public void sendWalkMessage(int i){
		 byte[] id = shortToBytes(MESSAGE_WALK);
		 sendToServer(id[0], id[1],(byte) i);
	 }
	 
	 public void disconnect(){
		 sendToServer(shortToBytes(MESSAGE_DISCONNECT));
	 }
	 
	 public void receiveMessage(byte[] data){
		 int t = 0;
		 while(t < data.length){
			 short size = bytesToShort(data[t], data[t + 1]);
			 byte[] bytes = new byte[size];
			 System.arraycopy(data, t + 2, bytes, 0, size);
			 readMessage(bytes);
			 t += size + 2;
		 }
	 }
	 
	 public void readMessage(byte[] bytes){
		 short id = bytesToShort(bytes[0], bytes[1]);
		 if(id == MESSAGE_WORLD){
			 client.world = Resources.loadWorldData(bytes, client, "");
		 }
		 if(id == MESSAGE_SET_TILE)
			 loadTile(bytes);
		 if(id == MESSAGE_DISCONNECT){
			 client.connection = null;
			 isStopping = true;
			 client.console.println("client is about to dispose() and client.connection = " + client.connection);
			 client.dispose();
		 }
		 if(id == MESSAGE_MOTION_X)
			 changeMotionX(bytes);
		 if(id == MESSAGE_MOTION_Y)
			 changeMotionY(bytes);
		 if(id == MESSAGE_MOTION)
			 changeMotion(bytes);
		 if(id == MESSAGE_WALK){
			 receiveWalkMessage(bytes);
		 }
		 if(id == MESSAGE_SPAWN_ENTITY){
			 Entity entity = getEntityFromID(bytesToShort(bytes[2], bytes[3]));
			 Resources.loadEntityData(bytes, 4, entity);
			 entity.world = client.world;
			 client.world.spawnEntity(entity);
		 }
		 if(id == MESSAGE_REMOVE_ENTITY)
			 client.world.removeEntity(ByteBuffer.wrap(bytes).getInt(2));
		 if(id == MESSAGE_UPDATE_ENTITIES)
			 Resources.loadEntities(client.world, 2, bytes);
		 if(id == MESSAGE_CHAT)
			 receiveChatMessage(bytes);
		 if(id == MESSAGE_ENABLE_BUILD_MODE)
			 client.enableBuildMode();
		 if(id == MESSAGE_DISABLE_BUILD_MODE)
			 client.disableBuildMode();
		 if(id == MESSAGE_FILL_TILES)
			 loadTiles(bytes);
	 }
	 
	 public void loadTile(byte[] data){
		short tile = bytesToShort(data[2], data[3]);
		short x = bytesToShort(data[4], data[5]);
		short y = bytesToShort(data[6], data[7]);
		client.world.setTile(tile, new Point(x, y));
	 }
	 
	 public void loadTiles(byte[] data){
		short tile = bytesToShort(data[2], data[3]);
		short minX = bytesToShort(data[4], data[5]);
		short minY = bytesToShort(data[6], data[7]);
		short maxX = bytesToShort(data[8], data[9]);
		short maxY = bytesToShort(data[10], data[11]);
		client.world.setTiles(tile, minX, minY, maxX, maxY);
	}
	 
	 public void changeMotionX(byte[] data){
		 int entityID = ByteBuffer.wrap(data, 2, 4).getInt();
		 float motionX = ByteBuffer.wrap(data, 6, 4).getFloat();
		 client.world.entities.get(entityID).motionX = motionX;
	 }
	 
	 public void receiveWalkMessage(byte[] data){
		 int entityID = ByteBuffer.wrap(data, 2, 4).getInt();
		 byte movement = data[6];
		 EntityPlayer player = (EntityPlayer) client.world.entities.get(entityID);
		 if(movement == 0){
			 player.moveX = 0;
			 player.moveY = 0;
		 }
		 if(movement == 1 || movement == 3)
			 player.moveX = 0.4F;
		 if(movement == -1 || movement == -3)
			 player.moveX = -0.4F;
		 if(movement == 2 || Math.abs(movement) == 3)
			 player.moveY = 5;
		 if(player.moveX > 0)
			 player.facingLeft = false;
		 if(player.moveX < 0)
			 player.facingLeft = true;
	 }
	 
	 public void changeMotionY(byte[] data){
		 int entityID = ByteBuffer.wrap(data, 2, 4).getInt();
		 float motionY = ByteBuffer.wrap(data, 6, 4).getFloat();
		 client.world.entities.get(entityID).motionY = motionY;
	 }
	 
	 public void changeMotion(byte[] data){
		 int entityID = ByteBuffer.wrap(data, 2, 4).getInt();
		 float motionX = ByteBuffer.wrap(data, 6, 4).getFloat();
		 float motionY = ByteBuffer.wrap(data, 10, 4).getFloat();
		 client.world.entities.get(entityID).motionX = motionX;
		 client.world.entities.get(entityID).motionY = motionY;
	 }
	 
	 public void sendChatMessage(String message){
		 byte[] data = new byte[2 + message.length() * 2];
		 byte[] id = shortToBytes(MESSAGE_CHAT);
		 data[0] = id[0];
		 data[1] = id[1];
		 int t = 0;
		 while(t < message.length()){
			 byte[] token = ByteBuffer.allocate(2).putChar(message.charAt(t)).array();
			 data[2 + t * 2] = token[0];
			 data[3 + t * 2] = token[1];
			 ++t;
		 }
		 sendToServer(data);
	 }
	 
	 public void sendUserNameMessage(){
		 byte[] data = new byte[2 + client.name.length() * 2];
		 byte[] id = shortToBytes(MESSAGE_USERNAME);
		 data[0] = id[0];
		 data[1] = id[1];
		 int t = 0;
		 while(t < client.name.length()){
			 byte[] chars = ByteBuffer.allocate(2).putChar(client.name.charAt(t)).array();
			 data[2 + t * 2] = chars[0];
			 data[3 + t * 2] = chars[1];
			 ++t;;
		 }
		 sendToServer(data);
	 }
	 
	 public void sendShootMessage(int type, float x, float y){
		 byte[] data = new byte[11];
		 byte[] id = shortToBytes(MESSAGE_SHOOT);
		 data[0] = id[0];
		 data[1] = id[1];
		 data = ByteBuffer.wrap(data).put(2, (byte) type).putFloat(3, x).putFloat(7, y).array();
		 sendToServer(data);
	 }
	 
	 public void receiveChatMessage(byte[] data){
		 String message = "";
		 int t = 2;
		 while(t < data.length){
			 message += ByteBuffer.wrap(data, t, 2).getChar();
			 t += 2;
		 }
		 client.chatHistory.add(message);
	 }
}
