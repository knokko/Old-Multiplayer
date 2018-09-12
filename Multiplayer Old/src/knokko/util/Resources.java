package knokko.util;

import static knokko.connection.Connection.ENCODING_SHORT_MAP;
import static knokko.connection.Connection.MESSAGE_WORLD;
import static knokko.connection.Connection.byteFromBinair;
import static knokko.connection.Connection.byteToBinair;
import static knokko.connection.Connection.bytesToShort;
import static knokko.connection.Connection.getEntityFromID;
import static knokko.connection.Connection.getEntityID;
import static knokko.connection.Connection.shortToBytes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import knokko.client.ClientMain;
import knokko.entity.Entity;
import knokko.server.ServerMain;
import knokko.world.World;
import knokko.world.WorldClient;
import knokko.world.WorldServer;

public final class Resources {
	
	private static HashMap<String, BufferedImage> paintedImages = new HashMap<String, BufferedImage>();
	private static HashMap<String, BufferedImage> bufferedImages = new HashMap<String, BufferedImage>();
	private static HashMap<String, Image> images = new HashMap<String, Image>();
	
	public static final Image getImage(String url){
		Image image = images.get(url);
		if(image != null)
			return image;
		image = new ImageIcon(Resources.class.getClassLoader().getResource(url)).getImage();
		images.put(url, image);
		return image;
	}
	
	public static final BufferedImage getBuffImage(String url){
		try {
			BufferedImage image = copyImage(bufferedImages.get(url));
			if(image != null)
				return image;
			image = ImageIO.read(Resources.class.getClassLoader().getResource(url.toLowerCase()));
			bufferedImages.put(url, image);
			return image;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("url = " + url);
			return null;
		}
	}
	
	public static final BufferedImage getPaintedImage(String url, Color color){
		BufferedImage image = paintedImages.get("url: " + url + " color: " + color);
		if(image != null)
			return image;
		image = getBuffImage(url);
		float red = color.getRed() / 255.0f;
		float green = color.getGreen() / 255.0f;
		float blue = color.getBlue() / 255.0f;
		int x = 0;
		while(x < image.getWidth()){
			int y = 0;
			while(y < image.getHeight()){
				int[] alpha = image.getAlphaRaster().getPixel(x, y, new int[1]);
				Color pixelColor = new Color(image.getRGB(x, y));
				if(pixelColor.getRed() == pixelColor.getGreen() && pixelColor.getGreen() == pixelColor.getBlue())
					image.setRGB(x, y, new Color((int)(pixelColor.getRed() * red), (int)(pixelColor.getGreen() * green), (int)(pixelColor.getBlue() * blue), alpha[0]).getRGB());
				++y;
			}
			++x;
		}
		paintedImages.put("url: " + url + " color: " + color, image);
		return image;
	}
	
	public static final byte[] storeWorldData(World world){
		byte[] data = new byte[32 + world.tiles.length * world.tiles[0].length * 2];
		byte[] id = shortToBytes(MESSAGE_WORLD);
		byte[] encoding = shortToBytes(ENCODING_SHORT_MAP);
		byte[] width = shortToBytes((short)world.tiles.length);
		byte[] height = shortToBytes((short)world.tiles[0].length);
		byte[] ticks = ByteBuffer.allocate(8).putLong(world.ticks).array();
		byte[] sendtime = ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array();
		byte[] passedtime = ByteBuffer.allocate(8).putLong(System.nanoTime() - world.startTime).array();
		data[0] = id[0];
		data[1] = id[1];
		data[2] = encoding[0];
		data[3] = encoding[1];
		data[4] = width[0];
		data[5] = width[1];
		data[6] = height[0];
		data[7] = height[1];
		data[8] = ticks[0];
		data[9] = ticks[1];
		data[10] = ticks[2];
		data[11] = ticks[3];
		data[12] = ticks[4];
		data[13] = ticks[5];
		data[14] = ticks[6];
		data[15] = ticks[7];
		data[16] = sendtime[0];
		data[17] = sendtime[1];
		data[18] = sendtime[2];
		data[19] = sendtime[3];
		data[20] = sendtime[4];
		data[21] = sendtime[5];
		data[22] = sendtime[6];
		data[23] = sendtime[7];
		data[24] = passedtime[0];
		data[25] = passedtime[1];
		data[26] = passedtime[2];
		data[27] = passedtime[3];
		data[28] = passedtime[4];
		data[29] = passedtime[5];
		data[30] = passedtime[6];
		data[31] = passedtime[7];
		int i = 32;
		int x = 0;
		while(x < world.tiles.length){
			int y = 0;
			while(y < world.tiles[x].length){
				byte[] tile = shortToBytes(world.tiles[x][y]);
				data[i] = tile[0];
				data[1 + i] = tile[1];
				++y;
				i += 2;
			}
			++x;
		}
		data = storeEntities(world, i, data);
		return data;
	}
	
	public static final byte[] storeEntities(World world, int startIndex, byte[] data){
		int i = startIndex;
		int t = 0;
		while(t < world.entities.size()){
			Entity entity = world.entities.get(t);
			data = storeEntityData(entity, data, i);
			i = data.length;
			++t;
		}
		return data;
	}
	
	public static final World loadWorldData(byte[] data, Object clientOrServer, String filename){
		 short encoding = bytesToShort(data[2], data[3]);
		 short width = bytesToShort(data[4], data[5]);
		 short height = bytesToShort(data[6], data[7]);
		 long ticks = ByteBuffer.wrap(data, 8, 8).getLong();
		 long sendTime = ByteBuffer.wrap(data, 16, 8).getLong();
		 long passedTime = ByteBuffer.wrap(data, 24, 8).getLong();
		 World world;
		 if(clientOrServer instanceof ClientMain)
			 world = new WorldClient(width, height, ticks, passedTime, sendTime, (ClientMain) clientOrServer, filename);
		 else
			 world = new WorldServer(width, height, (ServerMain) clientOrServer, filename);
		 int i = 32;
		 if(encoding == ENCODING_SHORT_MAP){
			 int x = 0;
			 while(x < width){
				 int y = 0;
				 while(y < height){
					 world.tiles[x][y] = bytesToShort(data[i], data[1 + i]);
					 ++y;
					 i += 2;
				 }
				 ++x;
			 }
		 }
		 while(i < data.length - 19){
			 Entity entity = getEntityFromID(bytesToShort(data[i], data[i + 1]));
			 entity.world = world;
			 i += 2;
			 i = loadEntityData(data, i, entity);
			 world.spawnEntity(entity);
		 }
		 return world;
	 }
	
	public static final void loadEntities(World world, int startIndex, byte[] data){
		ArrayList<Entity> entities = new ArrayList<Entity>();
		while(startIndex < data.length - 19){
			 Entity entity = getEntityFromID(bytesToShort(data[startIndex], data[startIndex + 1]));
			 entity.world = world;
			 startIndex += 2;
			 startIndex = loadEntityData(data, startIndex, entity);
			 entities.add(entity);
		 }
		world.entities = entities;
	}
	
	public static final byte[] storeEntityData(Entity entity, byte[] data, int startIndex){
		if(!entity.inactive){
			byte[] entityID = shortToBytes(getEntityID(entity));
			byte[] entityX = ByteBuffer.allocate(4).putFloat(entity.position.x).array();
			byte[] entityY = ByteBuffer.allocate(4).putFloat(entity.position.y).array();
			byte[] motionX = ByteBuffer.allocate(4).putFloat(entity.motionX).array();
			byte[] motionY = ByteBuffer.allocate(4).putFloat(entity.motionY).array();
			byte bools = byteFromBinair(entity.onGround != 0, entity.onGround > 0, entity.facingLeft, entity.onWall != 0, entity.onWall > 0, false, false, false);
			data = Arrays.copyOfRange(data, 0, data.length + 19);
			data[startIndex] = entityID[0];
			data[startIndex + 1] = entityID[1];
			data[startIndex + 2] = entityX[0];
			data[startIndex + 3] = entityX[1];
			data[startIndex + 4] = entityX[2];
			data[startIndex + 5] = entityX[3];
			data[startIndex + 6] = entityY[0];
			data[startIndex + 7] = entityY[1];
			data[startIndex + 8] = entityY[2];
			data[startIndex + 9] = entityY[3];
			data[startIndex + 10] = motionX[0];
			data[startIndex + 11] = motionX[1];
			data[startIndex + 12] = motionX[2];
			data[startIndex + 13] = motionX[3];
			data[startIndex + 14] = motionY[0];
			data[startIndex + 15] = motionY[1];
			data[startIndex + 16] = motionY[2];
			data[startIndex + 17] = motionY[3];
			data[startIndex + 18] = bools;
			startIndex += 19;
			data = entity.addExtraData(data, startIndex);
			startIndex = data.length;
		}
		return data;
	}
	
	public static final int loadEntityData(byte[] data, int index, Entity entity){
		 entity.position.x = ByteBuffer.wrap(data, index, 4).getFloat();
		 entity.position.y = ByteBuffer.wrap(data, index + 4, 4).getFloat();
		 entity.motionX = ByteBuffer.wrap(data, index + 8, 4).getFloat();
		 entity.motionY = ByteBuffer.wrap(data, index + 12, 4).getFloat();
		 boolean[] bools = byteToBinair(data[index + 16]);
		 if(bools[0])
			 entity.onGround = (byte) (bools[1] ? 1 : -1);
		 entity.facingLeft = bools[2];
		 if(bools[3])
			 entity.onWall = (byte) (bools[4] ? 1 : -1);
		 index += 17;
		 index = entity.readExtraData(data, index);
		 return index;
	 }
	
	public static final BufferedImage copyImage(BufferedImage source){
		if(source == null)
			return null;
	    BufferedImage b = new BufferedImage(source.getWidth(), source.getHeight(), source.getType());
	    Graphics g = b.getGraphics();
	    g.drawImage(source, 0, 0, null);
	    g.dispose();
	    return b;
	}
}
