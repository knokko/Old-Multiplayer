package knokko.connection;

import java.awt.Point;
import java.awt.geom.Point2D.Float;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import knokko.entity.Entity;
import knokko.entity.EntityPlayer;
import knokko.entity.projectile.EntityLaser;
import knokko.server.ServerMain;
import knokko.util.Resources;
import knokko.world.World;
import static knokko.connection.Connection.*;

public class ConnectionServer extends Thread {
	
	public ServerMain server;
	public ServerSocket socket;
	public Socket client;
	public InputStream input;
	public OutputStream output;
	public EntityPlayer player;
	
	public boolean isOperator;
	private boolean isStopping;
	
	public ConnectionServer(ServerMain serverMain){
		this(serverMain, null);
	}
	
	public ConnectionServer(ServerMain serverMain, ServerSocket ssocket){
		socket = ssocket;
		server = serverMain;
	}
	
	@Override
	public void run(){
		try {
			if(socket == null){
				socket = new ServerSocket(40537);
				server.printLine("created server socket " + socket.getLocalSocketAddress());
				server.printLine(InetAddress.getLocalHost().getHostAddress());
			}
			client = socket.accept();
			ConnectionServer connection = new ConnectionServer(server, socket);
			connection.start();
			server.connection.connections.add(connection);
			server.printLine("Accepted client " + client);
			//player = server.spawnPlayer();
			input = client.getInputStream();
			output = client.getOutputStream();
			sendWorld();
			if(server.isBuilding)
				sendToClient(shortToBytes(MESSAGE_ENABLE_BUILD_MODE));
			while(true){
				if(isStopping)
					return;
				byte[] bytes = new byte[100000];
				int i = input.read(bytes);
				try {
					if(i > 0)
						receiveMessage(Arrays.copyOfRange(bytes, 0, i));
				} catch( Exception ex){
					server.printLine("The following error occured:");
					ex.printStackTrace(server.board.getConsoleStream());
				}
			}
		} catch (Exception e) {
			if(!isStopping)
				e.printStackTrace(server.board.getConsoleStream());
		} 
	}
	
	public void sendToClient(byte[] bytes){
		try {
			byte[] data = new byte[bytes.length + 2];
			System.arraycopy(bytes, 0, data, 2, bytes.length);
			byte[] size = shortToBytes((short)bytes.length);
			data[0] = size[0];
			data[1] = size[1];
			output.write(data);
		} catch (Exception e) {
			if(!(e instanceof NullPointerException))
				e.printStackTrace(server.board.getConsoleStream());
		}
	}
	
	public void sendToClient(byte[]... bytes){
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
		 sendToClient(data);
	 }
	
	public void sendWorld(World world){
		byte[] data = Resources.storeWorldData(world);
		sendToClient(data);
		if(player != null){
			String name = player.name;
			player = world.getPlayerByName(player.name);
			if(player == null){
				player = server.spawnPlayer();
				player.name = name;
			}
		}
	}
	
	public void sendEntities(World world){
		byte[] data = shortToBytes(MESSAGE_UPDATE_ENTITIES);
		data = Resources.storeEntities(world, 2, data);
		sendToClient(data);
	}
	
	public void sendEntities(){
		sendEntities(server.world);
	}
	
	public void sendWorld(){
		sendWorld(server.world);
	}
	
	public void setTileInClient(short tile, short x, short y){
		 byte[] id = shortToBytes(MESSAGE_SET_TILE);
		 byte[] btile = shortToBytes(tile);
		 byte[] bx = shortToBytes(x);
		 byte[] by = shortToBytes(y);
		 sendToClient(id, btile, bx, by);
	 }
	
	public void setTilesInClient(short tile, short x1, short y1, short x2, short y2){
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
		sendToClient(id, btile, bx, by, bx2, by2);
	}
	
	public void sendMotionMessage(int entityID, boolean updateX, boolean updateY){
		byte[] entity = ByteBuffer.allocate(4).putInt(entityID).array();
		if(updateX && updateY){
			byte[] id = shortToBytes(MESSAGE_MOTION);
			byte[] motionX = ByteBuffer.allocate(4).putFloat(server.world.entities.get(entityID).motionX).array();
			byte[] motionY = ByteBuffer.allocate(4).putFloat(server.world.entities.get(entityID).motionY).array();
			sendToClient(id, entity, motionX, motionY);
		}
		if(updateX){
			byte[] id = shortToBytes(MESSAGE_MOTION_X);
			byte[] motionX = ByteBuffer.allocate(4).putFloat(server.world.entities.get(entityID).motionX).array();
			sendToClient(id, entity, motionX);
		}
		if(updateY){
			byte[] id = shortToBytes(MESSAGE_MOTION_Y);
			byte[] motionY = ByteBuffer.allocate(4).putFloat(server.world.entities.get(entityID).motionY).array();
			sendToClient(id, entity, motionY);
		}
	}
	
	public void sendWalkMessage(int entityID, byte movement){
		byte[] id = shortToBytes(MESSAGE_WALK);
		byte[] entity = ByteBuffer.allocate(4).putInt(entityID).array();
		byte[] move = new byte[]{movement};
		sendToClient(id, entity, move);
		server.printLine("sent walk message at tick " + server.world.ticks);
	}
	
	public void sendEntitySpawnMessage(Entity entity){
		byte[] id = shortToBytes(MESSAGE_SPAWN_ENTITY);
		byte[] data = new byte[4];
		data[0] = id[0];
		data[1] = id[1];
		data = Resources.storeEntityData(entity, data, 2);
		sendToClient(data);
	}
	
	public void sendEntityRemoveMessage(int entityIndex){
		byte[] id = shortToBytes(MESSAGE_REMOVE_ENTITY);
		byte[] index = ByteBuffer.allocate(4).putInt(entityIndex).array();
		byte[] data = new byte[6];
		data[0] = id[0];
		data[1] = id[1];
		data[2] = index[0];
		data[3] = index[1];
		data[4] = index[2];
		data[5] = index[3];
		sendToClient(data);
	}
	
	public void disconnect(){
		removeConnection();
		sendToClient(shortToBytes(MESSAGE_DISCONNECT));
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
		if(id == MESSAGE_SET_TILE)
			receiveTileMessage(bytes);
		if(id == MESSAGE_DISCONNECT)
			removeConnection();
		if(id == MESSAGE_WALK)
			receiveWalkMessage(bytes);
		if(id == MESSAGE_CHAT)
			receiveChatMessage(bytes);
		if(id == MESSAGE_USERNAME)
			receiveUserNameMessage(bytes);
		if(id == MESSAGE_SHOOT)
			receiveShootMessage(bytes);
		if(id == MESSAGE_SPAWN_ENTITY)
			receiveEntityMessage(bytes);
		if(id == MESSAGE_FILL_TILES)
			receiveTilesMessage(bytes);
	}
	
	public void receiveTileMessage(byte[] data){
		if(!server.isBuilding)
			return;
		short tile = bytesToShort(data[2], data[3]);
		short x = bytesToShort(data[4], data[5]);
		short y = bytesToShort(data[6], data[7]);
		server.world.setTile(tile, new Point(x, y));
	}
	
	public void receiveTilesMessage(byte[] data){
		if(!server.isBuilding)
			return;
		short tile = bytesToShort(data[2], data[3]);
		short minX = bytesToShort(data[4], data[5]);
		short minY = bytesToShort(data[6], data[7]);
		short maxX = bytesToShort(data[8], data[9]);
		short maxY = bytesToShort(data[10], data[11]);
		server.world.setTiles(tile, minX, minY, maxX, maxY);
	}
	
	public void receiveEntityMessage(byte[] data){
		if(!server.isBuilding)
			return;
		short entityId = bytesToShort(data[2], data[3]);
		float x = ByteBuffer.wrap(data).getFloat(4);
		float y = ByteBuffer.wrap(data).getFloat(8);
		Entity entity = getEntityFromID(entityId);
		if(entity == null){
			server.printLine("|ÿ00|Failed to find entity with id " + entityId + ".");
			return;
		}
		entity.position = new Float(x, y);
		entity.world = server.world;
		server.world.spawnEntity(entity);
	}
	
	public void removeConnection(){
		isStopping = true;
		server.connection.connections.remove(this);
	}
	
	public void receiveWalkMessage(byte[] data){
		if(player == null)
			return;
		byte movement = data[2];
		float oldX = player.moveX;
		float oldY = player.moveY;
		if(movement == 1){
			player.moveX = -0.4F;
			player.facingLeft = true;
		}
		if(movement == 2){
			player.moveX = 0.4F;
			player.facingLeft = false;
		}
		if(movement == 3)
			player.moveY = 5F;
		if(movement == -1 || movement == -2)
			player.moveX = 0;
		if(movement == -3)
			player.moveY = 0;
		if(oldX != player.moveX || oldY != player.moveY)
			server.connection.updateClientEntities();
	}
	
	public void receiveChatMessage(byte[] data){
		String message = "";
		int t = 2;
		while(t < data.length){
			message += ByteBuffer.wrap(data, t, 2).getChar();
			t += 2;
		}
		if(message.startsWith("/")){
			if(isOperator)
				sendChatMessage(server.executeCommand(message.substring(1)), false);
			else {
				String error = "|ÿ00|You don't have permission to execute commands!";
				sendChatMessage(error, false);
			}
			return;
		}
		message = player + ": " + message;
		server.printLine(message);
		sendChatMessage(message, true);
	}
	
	public void sendChatMessage(String message, boolean sendToAll){
		if(message == null)
			return;
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
		if(sendToAll)
			server.connection.sendToClients(bytes);
		else
			sendToClient(bytes);
	}
	
	public void receiveUserNameMessage(byte[] data){
		String name = "";
		int t = 2;
		while(t < data.length){
			name += ByteBuffer.wrap(data, t, 2).getChar();
			t += 2;
		}
		if(server.getConnectionByName(name) != null){
			disconnect();
			return;
		}
		player = server.world.getPlayerByName(name);
		if(player == null){
			player = server.spawnPlayer();
			player.name = name;
		}
	}
	
	public void receiveShootMessage(byte[] data){
		byte type = data[2];
		float x = ByteBuffer.wrap(data).getFloat(3);
		float y = ByteBuffer.wrap(data).getFloat(7);
		if(type == 0)
			server.world.spawnEntity(new EntityLaser(server.world, player, new Float(x, y)));
		server.connection.updateClientEntities();
	}
	
	public void promote(){
		isOperator = true;
		sendChatMessage("|0ÿÿ|You have been promoted.", false);
	}
}
