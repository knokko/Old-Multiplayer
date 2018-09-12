package knokko.client;

import javax.swing.JFrame;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D.Float;
import java.io.PrintWriter;
import java.util.ArrayList;

import knokko.connection.Connection;
import knokko.connection.ConnectionClient;
import knokko.tile.Tiles;
import knokko.util.PointUtils;
import knokko.world.World;

public class ClientMain extends JFrame implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	/**
	 * The serial version ID
	 */
	private static final long serialVersionUID = -7891102876714427357L;
	
	public static final ClientMain instance = new ClientMain();
	
	public static final long startTime = System.nanoTime();
	
	public ConnectionClient connection;
	public PrintWriter console;
	public World world;
	
	public ArrayList<String> chatHistory = new ArrayList<String>();
	public String chat = "";
	public String name = "";
	
	public boolean mousePressed;
	
	public static void main(String[] arguments){
		try {
			instance.init();
			instance.start();
		} catch(Exception ex){
			ex.printStackTrace(instance.console);
		}
	}
	
	public final ClientBoard board;
	public Float camera = new Float(100, 100);
	
	public float zoom = 1;
	
	public long extraTime;
	
	private Float dragStart;
	public byte currentBuildTool = 3;
	public short currentTile;
	public short currentEntity;
	
	public Point tileMark;
	private boolean isStopping;
	
	public boolean isBuilding;
	public boolean isChatting;

	public ClientMain(){
		board = new ClientBoard(this);
	}
	
	@Override
	public void dispose(){
		if(connection != null)
			connection.disconnect();
		if(console != null)
			console.close();
		System.exit(0);
	}
	
	public void enableBuildMode(){
		if(!isBuilding)
			chatHistory.add("|0ÿÿ|The build mode has been enabled.");
		isBuilding = true;
	}
	
	public void disableBuildMode(){
		if(isBuilding)
			chatHistory.add("|0ÿÿ|The build mode has been disabled.");
		isBuilding = false;
	}
	
	private void init(){
		try {
			console = new PrintWriter(System.currentTimeMillis() + ".txt");
		} catch(Exception ex){
			System.exit(-1);
		}
		add(board);
		setUndecorated(false);
		setSize(800, 450);
		setTitle("Client");
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addKeyListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addMouseListener(this);
	}
	
	private void start(){
		while(!isStopping){
			update();
			board.repaint();
			long sleepTime = 20000000;
			if(world != null){
				long passedTime = System.nanoTime() + world.passedTime - world.startTime;
				long expectedTime = (world.ticks) * 20000000;
				sleepTime = expectedTime - passedTime;
			}
			try {
				if(sleepTime > 0)
					Thread.sleep(sleepTime / 1000000);
				else
					console.println("Can't keep up! sleepTime = " + sleepTime + " and world.ticks = " + world.ticks + " and world.startTime = " + world.startTime / 1000000000.0 + " and world.passedTime = " + world.passedTime / 1000000000.0);
			} catch (InterruptedException e) {
				e.printStackTrace(console);
			}
		}
		console.println("Game loop is over!");
	}
	
	private void update(){
		Point mouse = getMousePosition();
		if(mousePressed && mouse != null){
			if(dragStart == null)
				dragStart = PointUtils.screenToGamePoint(this, mouse);
			else {
				Float pointer = PointUtils.screenToGamePoint(this, mouse);
				double rotation = Math.atan2(pointer.y - dragStart.y, pointer.x - dragStart.x);
				float s = 7;
				if(pointer.distance(dragStart) > 0.01)
					camera = new Float((float) (camera.x + Math.cos(rotation) * s), (float) (camera.y + Math.sin(rotation) * s));
				dragStart = new Float(pointer.x, pointer.y);
			}
		}
		else if(dragStart != null)
			dragStart = null;
		if(world != null && !isBuilding)
			world.update();
		else if(world != null)
			++world.ticks;
	}

	@Override
	public void mouseClicked(MouseEvent event) {
		if(connection == null)
			return;
		if(isBuilding){
			int percentX = board.factorAX(event.getPoint().x - 4);
			int percentY = board.factorAY(event.getPoint().y - 32);
			if(percentY <= 10){
				if(percentX >= 1 && percentX <= 15 && percentY >= 1 && percentY <= 5){
					currentBuildTool++;
					if(currentBuildTool > 3)
						currentBuildTool = 0;
				}
			}
			else {
				Float game = PointUtils.screenToGamePoint(this, event.getPoint());
				if(currentBuildTool == 2){
					short x = (short) (game.x / 60);
					short y = (short) (game.y / 60);
					if(tileMark == null && event.getButton() == MouseEvent.BUTTON1)
						connection.setTileInServer(currentTile, x, y);
					if(event.getButton() == MouseEvent.BUTTON3)
						tileMark = new Point(x, y);
					if(tileMark != null && event.getButton() == MouseEvent.BUTTON1)
						connection.setTilesInServer(currentTile, x, y, (short) tileMark.x, (short) tileMark.y); 
				}
				if(currentBuildTool == 3){
					connection.setEntityInServer(getCurrentEntityId(), game.x, game.y);
				}
			}
		}
		else {
			Float target = PointUtils.screenToGamePoint(this, event.getPoint());
			connection.sendShootMessage(0, target.x, target.y);
		}
	}

	@Override
	public void mousePressed(MouseEvent event) {
		mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mousePressed = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseDragged(MouseEvent event) {
		
	}

	@Override
	public void mouseMoved(MouseEvent event) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {
		int scroll = event.getWheelRotation();
		if(scroll > 0)
			zoom /= 1.2;
		else
			zoom *= 1.2;
	}

	@Override
	public void keyTyped(KeyEvent event) {
		if(connection != null){
			if(event.getKeyChar() == '\n'){
				if(isChatting && chat.length() > 0)
					connection.sendChatMessage(chat);
				if(chat.length() == 0 && isChatting)
					isChatting = false;
				else if(!isChatting)
					isChatting = true;
				chat = "";
			}
			else if(event.getKeyChar() == '\b' && chat.length() > 0 && isChatting)
				chat = chat.substring(0, chat.length() - 1);
			else if(isChatting){
				chat += event.getKeyChar();
			}
		}
		else {
			if(event.getKeyChar() == '\n'){
				try {
					connection = Connection.openClientConnection(this, name);
				} catch(Exception ex){
					ex.printStackTrace(console);
				}
			}
			else if(event.getKeyChar() == '\b' && name.length() > 0)
				name = name.substring(0, name.length() - 1);
			else
				name += event.getKeyChar();
		}
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if(connection == null || isChatting)
			return;
		if(isBuilding){
			if(event.getKeyCode() == KeyEvent.VK_RIGHT){
				if(currentBuildTool == 2){
					currentTile++;
					if(currentTile >= Tiles.tiles())
						currentTile = 0;
				}
				if(currentBuildTool == 3){
					currentEntity++;
					if(currentEntity > 0)
						currentEntity = 0;
				}
			}
			if(event.getKeyCode() == KeyEvent.VK_LEFT){
				if(currentBuildTool == 2){
					currentTile--;
					if(currentTile < 0)
						currentTile = (short) (Tiles.tiles() - 1);
				}
				if(currentBuildTool == 3){
					currentEntity--;
					if(currentEntity < 0)
						currentEntity = 0;
				}
			}
		}
		else {
			if(event.getKeyCode() == KeyEvent.VK_LEFT)
				connection.sendWalkMessage(1);
			if(event.getKeyCode() == KeyEvent.VK_RIGHT)
				connection.sendWalkMessage(2);
			if(event.getKeyCode() == KeyEvent.VK_SPACE || event.getKeyCode() == KeyEvent.VK_UP)
				connection.sendWalkMessage(3);
		}
	}

	@Override
	public void keyReleased(KeyEvent event) {
		if(connection == null || isChatting)
			return;
		if(event.getKeyCode() == KeyEvent.VK_LEFT)
			connection.sendWalkMessage(-1);
		if(event.getKeyCode() == KeyEvent.VK_RIGHT)
			connection.sendWalkMessage(-2);
		if(event.getKeyCode() == KeyEvent.VK_SPACE || event.getKeyCode() == KeyEvent.VK_UP)
			connection.sendWalkMessage(-3);
	}
	
	public short getCurrentEntityId(){
		if(currentEntity == 0)
			return 1;
		return -1;
	}
}
