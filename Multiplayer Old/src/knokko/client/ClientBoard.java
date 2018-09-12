package knokko.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import knokko.tile.Tiles;

public class ClientBoard extends JPanel {
	
	public final ClientMain client;

	/**
	 * The serial version ID
	 */
	private static final long serialVersionUID = 6890604917363881446L;
	
	private Graphics graphics;

	public ClientBoard(ClientMain clientMain) {
		client = clientMain;
	}
	
	@Override
	public void paint(Graphics gr){
		graphics = gr;
		fillRect(0, 0, 100, 100, Color.BLUE, true);
		if(client.world != null)
			client.world.paint(this);
		gr.setColor(Color.BLACK);
		int i = Math.max(client.chatHistory.size() - 20, 0);
		while(i < client.chatHistory.size()){
			String line = client.chatHistory.get(i);
			drawString(line, gr, 10, 100 - (client.chatHistory.size() - i + 4) * 4);
			++i;
		}
		if(client.chat != null && client.connection != null && client.isChatting){
			if(client.chat.length() > 0)
				drawString(client.chat, 10, 100 - (client.chatHistory.size() - i + 4) * 4, true);
			else
				drawString("Type something...", 10, 100 - (client.chatHistory.size() - i + 4) * 4, true, Color.YELLOW);
		}
		if(client.connection == null){
			gr.setFont(new Font("TimesRoman", 0, 40));
			if(client.name.isEmpty())
				gr.drawString("Enter name here and press enter.", client.getWidth() / 2, client.getHeight() / 2);
			else
				gr.drawString(client.name, client.getWidth() / 2, client.getHeight() / 2);
		}
		if(client.isBuilding){
			fillRect(0, 0, 100, 10, Color.YELLOW, true);
			drawRect(0, 0, 100, 10, Color.BLACK, true);
			drawRect(1, 1, 15, 5, Color.BLACK, true);
			drawString("tool: " + getTool(client.currentBuildTool), 2, 4, true);
			if(client.currentBuildTool == 2)
				drawString("tile: " + Tiles.fromId(client.currentTile).name, 2, 9, true);
			if(client.currentBuildTool == 2 || client.currentBuildTool == 3)
				drawRect(1, 6, 15, 10, Color.BLACK, true);
		}
	}
	
	public void fillRect(int x1, int y1, int x2, int y2, Color color, boolean applyFactor){
		int xmin = x1 > x2 ? x2 : x1;
		int ymin = y1 > y2 ? y2 : y1;
		int width = Math.abs(x1 - x2);
		int height = Math.abs(y1 - y2);
		graphics.setColor(color);
		if(applyFactor)
			graphics.fillRect(factorX(xmin), factorY(ymin), factorX(width), factorY(height));
		else
			graphics.fillRect(xmin, ymin, width, height);
	}
	
	public void drawRect(int x1, int y1, int x2, int y2, Color color, boolean applyFactor){
		int xmin = x1 > x2 ? x2 : x1;
		int ymin = y1 > y2 ? y2 : y1;
		int width = Math.abs(x1 - x2);
		int height = Math.abs(y1 - y2);
		graphics.setColor(color);
		if(applyFactor)
			graphics.drawRect(factorX(xmin), factorY(ymin), factorX(width), factorY(height));
		else
			graphics.drawRect(xmin, ymin, width, height);
	}
	
	public void drawImage(int x1, int y1, int x2, int y2, Image image, boolean applyFactor){
		int xmin = x1 > x2 ? x2 : x1;
		int ymin = y1 > y2 ? y2 : y1;
		int width = Math.abs(x1 - x2);
		int height = Math.abs(y1 - y2);
		if(applyFactor)
			graphics.drawImage(image, factorX(xmin), factorY(ymin), factorX(width), factorY(height), null);
		else
			graphics.drawImage(image, xmin, ymin, width, height, null);
	}
	
	public void drawImage(int x, int y, float rotateX, float rotateY, float rotation, BufferedImage image){
		AffineTransform tx = new AffineTransform();
		tx.setToRotation(Math.toRadians(rotation), rotateX * client.zoom, rotateY * client.zoom);
		AffineTransformOp op = new AffineTransformOp(tx, 2);
		((Graphics2D)graphics).drawImage(image, op, x, y);
	}
	
	public void drawString(String string, int x, int y, boolean applyFactor, Font font, Color color){
		if(applyFactor)
			graphics.setFont(new Font(font.getName(), font.getStyle(), factorX(font.getSize())));
		else
			graphics.setFont(font);
		graphics.setColor(color);
		if(applyFactor)
			graphics.drawString(string, factorX(x), factorY(y));
		else
			graphics.drawString(string, x, y);
	}
	
	public void drawCenteredString(String string, int midX, int midY, boolean applyFactor, Font font, Color color){
		if(applyFactor)
			graphics.setFont(new Font(font.getName(), font.getStyle(), factorX(font.getSize())));
		else
			graphics.setFont(font);
		graphics.setColor(color);
		double width = graphics.getFont().getStringBounds(string, ((Graphics2D) graphics).getFontRenderContext()).getWidth();
		double height = graphics.getFont().getStringBounds(string, ((Graphics2D) graphics).getFontRenderContext()).getHeight();
		int x = (int) ((applyFactor ? factorX(midX) : midX) - width / 2);
		int y = (int) ((applyFactor ? factorY(midY) : midY) - height / 2);
		graphics.drawString(string, x, y);
	}
	
	public void drawCenteredString(String string, int x, int y, boolean applyFactor){
		drawCenteredString(string, x, y, applyFactor, new Font("TimesRoman", 0, applyFactor ? 2 : 20), Color.BLACK);
	}
	
	public void drawCenteredString(String string, int x, int y, boolean applyFactor, Color color){
		drawCenteredString(string, x, y, applyFactor, new Font("TimesRoman", 0, applyFactor ? 2 : 20), color);
	}
	
	public void drawCenteredString(String string, int x, int y, boolean applyFactor, Font font){
		drawCenteredString(string, x, y, applyFactor, font, Color.BLACK);
	}
	
	public void drawString(String string, int x, int y, boolean applyFactor){
		drawString(string, x, y, applyFactor, new Font("TimesRoman", 0, applyFactor ? 2 : 20), Color.BLACK);
	}
	
	public void drawString(String string, int x, int y, boolean applyFactor, Color color){
		drawString(string, x, y, applyFactor, new Font("TimesRoman", 0, applyFactor ? 2 : 20), color);
	}
	
	public void drawString(String string, int x, int y, boolean applyFactor, Font font){
		drawString(string, x, y, applyFactor, font, Color.BLACK);
	}
	
	public void drawLine(Color color, int x, int y, int x2, int y2) {
		graphics.setColor(color);
		graphics.drawLine(x, y, x2, y2);
	}
	
	public void drawLine(Color color, Point p1, Point p2){
		drawLine(color, p1.x, p1.y, p2.x, p2.y);
	}
	
	public int factorX(int x){
		return x * client.getWidth() / 100;
	}
	
	public int factorY(int y){
		return y * client.getHeight() / 100;
	}
	
	public int factorAX(int x){
		return x * 100 / client.getWidth();
	}
	
	public int factorAY(int y){
		return y * 100 / client.getHeight();
	}
	
	public void drawString(String string, Graphics gr, int x, int y){
		try {
			int width = 0;
			boolean bool = string.contains("|");
			while(string.contains("|")){
				int index1 = string.indexOf("|");
				int index2 = string.indexOf("|", index1 + 1);
				String sub = string.substring(index1 + 1, index2);
				String first = string.substring(0, index1);
				drawString(first, x + factorAX(width), y, true, new Font("TimesRoman", 0, 2));
				width += (int) gr.getFont().getStringBounds(first, ((Graphics2D) gr).getFontRenderContext()).getWidth();
				int index3 = string.indexOf("|", index2 + 1);
				String after = index3 > 0 ? string.substring(index2 + 1, index3) : string.substring(index2 + 1);
				int red = Math.min(sub.charAt(0), 255);
				int green = Math.min(sub.charAt(1), 255);
				int blue = Math.min(sub.charAt(2), 255);
				int alpha = 255;
				if(sub.length() == 4)
					alpha = sub.charAt(3);
				drawString(after, x + factorAX(width), y, true, new Font("TimesRoman", 0, 2), new Color(red, green, blue, alpha));
				string = string.substring(index3 > 0 ? index3 - 1 : index2 + 1);
				width += (int) gr.getFont().getStringBounds(after, ((Graphics2D) gr).getFontRenderContext()).getWidth();
			}
			gr.setColor(Color.BLACK);
			if(bool)
				return;
		} catch(Exception ex){
			ex.printStackTrace(client.console);
			return;
		}
		drawString(string, x, y, true, new Font("TimesRoman", 0, 2));
	}
	
	public static String getTool(byte buildTool){
		if(buildTool == 1)
			return "selector";
		if(buildTool == 2)
			return "tiles";
		if(buildTool == 3)
			return "entities";
		return "none";
	}
}
