package knokko.server;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;

import javax.swing.JFrame;

import knokko.connection.Connection;
import knokko.connection.ConnectionServer;
import knokko.entity.EntityPlayer;
import knokko.world.World;
import knokko.world.WorldServer;

public class ServerMain extends JFrame implements KeyListener{

	/**
	 * The serial version ID
	 */
	private static final long serialVersionUID = 3876918528633288282L;
	
	public static final ServerMain instance = new ServerMain();
	public static final long startTime = System.nanoTime();
	
	public static void main(String[] arguments){
		instance.init();
		instance.start();
	}
	
	public final ServerBoard board;
	public WorldServer world;
	public Connection connection = new Connection(this);
	
	public boolean isBuilding;
	
	String command = "";
	
	private boolean isStopping;
	private int timer;
	private int maxTimer = 500;

	public ServerMain() {
		board = new ServerBoard(this);
		world = (WorldServer) World.loadWorld("world 1.level", this);
	}
	
	@Override
	public void dispose(){
		connection.disconnect();
		try {
			Thread.sleep(50);
		} catch(Exception ex){}
		try {
			int t = 0;
			PrintWriter writer = new PrintWriter("server log.txt");
			while(t < board.console.size()){
				writer.println(board.console.get(t));
				++t;
			}
			writer.close();
		} catch(Exception ex){}
		System.exit(0);
	}
	
	public void printLine(String line){
		board.printLine(line);
	}
	
	public EntityPlayer spawnPlayer(){
		EntityPlayer player = new EntityPlayer(world, world.getSpawn());
		world.spawnEntity(player);
		return player;
	}
	
	private void init(){
		printLine("Init");
		add(board);
		setUndecorated(false);
		setSize(800, 450);
		setTitle("Server");
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		addKeyListener(this);
		connection.connections.add(Connection.openServerConnection(this));
	}
	
	private void start(){
		try {
			printLine("Start");
			System.out.println("nano time of start = " + System.nanoTime() / 1000000000.0 + " and world.startTime = " + world.startTime / 1000000000.0);
			while(!isStopping){
				update();
				board.repaint();
				long passedTime = System.nanoTime() - world.startTime;
				long expectedTime = world.ticks * 20000000L;
				long sleepTime = expectedTime - passedTime;
				try {
					if(sleepTime > 0)
						Thread.sleep(sleepTime / 1000000);
					else
						System.out.println("Can't keep up! sleepTime = " + sleepTime / 1000000000.0 + " and world.startTime = " + world.startTime / 1000000000.0 + " and expectedTime = " + expectedTime / 1000000000.0 + " and passedTime = " + passedTime / 1000000000.0 + " and System.nanoTime() = " + ((world.startTime + passedTime) / 1000000000.0));
				} catch (InterruptedException e) {
					e.printStackTrace(board.getConsoleStream());
				}
			}
		} catch(Exception ex){
			ex.printStackTrace(board.getConsoleStream());
		}
	}
	
	private void update(){
		if(!isBuilding)
			world.update();
		else
			++world.ticks;
		if(timer == 1)
			connection.updateClientEntities();
		if(timer > maxTimer)
			timer = 0;
		++timer;
	}
	
	public String executeCommand(String command){
		if(command.startsWith("c ")){
			connection.sendChatMessage(command.substring(2));
			printLine(command.substring(2));
			return null;
		}
		else {
			try {
				if(command.matches("stop")){
					printLine("The stop command has been used; The server will stop now.");
					dispose();
					return null;
				}
				if(command.startsWith("kick ")){
					ConnectionServer target = getConnectionByName(command.substring(5));
					if(target != null){
						target.disconnect();
						printLine(command.substring(5) + " has been kicked.");
						return "|0ÿ0|Kicked player " + command.substring(5) + " succesfully.";
					}
					else
						return "|ÿ00|Couldn't find player " + command.substring(5) + ".";
				}
				if(command.startsWith("promote ")){
					ConnectionServer target = getConnectionByName(command.substring(8));
					if(target != null){
						target.promote();
						printLine(command.substring(8) + " has been promoted.");
						return "|0ÿ0|Promoted player " + command.substring(8) + " succesfully.";
					}
					else
						return "|ÿ00|Couldn't find player " + command.substring(8) + ".";
				}
				if(command.startsWith("load")){
					try {
						world = (WorldServer) World.loadWorld(command.substring(5), this);
						connection.updateClientWorlds();
						return "|0ÿ0|Loaded world " + command.substring(5) + " succesfully.";
					} catch(Exception ex){
						return "|ÿ00|Couldn't load world " + command.substring(5);
					}
				}
				if(command.startsWith("save")){
					if(command.length() == 4){
						if(world.save()){
							printLine("Saved current world.");
							return "|0ÿ0|Saved this world succesfully.";
						}
						else {
							printLine("Failed so save the world:");
							return "|ÿ00|Couldn't save this world!";
						}
					}
					else {
						String fileName = command.substring(5);
						if(world.save(fileName)){
							printLine("Saved world " + world.worldName + " as " + fileName);
							return "|0ÿ0|Saved this world as " + fileName;
						}
						else {
							printLine("|ÿ00|Couldn't save world " + world.worldName + " as " + fileName);
							return "|ÿ00|Failed to save this world as " + fileName;
						}
					}
				}
				if(command.equals("set build mode")){
					isBuilding = true;
					printLine("mode has been set to build mode");
					connection.sendToClients(Connection.shortToBytes(Connection.MESSAGE_ENABLE_BUILD_MODE));
					return null;
				}
				if(command.equals("set play mode")){
					isBuilding = false;
					printLine("mode has been set to play mode");
					connection.sendToClients(Connection.shortToBytes(Connection.MESSAGE_DISABLE_BUILD_MODE));
					return null;
				}
				return "|ÿ00|There is no such command.";
			} catch(Exception ex){
				ex.printStackTrace(board.getConsoleStream());
				return "|ÿ00|Couldn't execute command.";
			}
		}
	}
	
	private String executeCommand(){
		return executeCommand(command);
	}

	@Override
	public void keyTyped(KeyEvent event) {
		if(event.getKeyChar() != '\b' && event.getKeyChar() != '\n')
			command += event.getKeyChar();
	}

	@Override
	public void keyPressed(KeyEvent event) {
		if(event.getKeyCode() == KeyEvent.VK_BACK_SPACE && command.length() > 0)
			command = command.substring(0, command.length() - 1);
		if(event.getKeyCode() == KeyEvent.VK_ENTER && command.length() > 0){
			printLine(executeCommand());
			command = "";
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public ConnectionServer getConnectionByName(String name){
		int t = 0;
		while(t < connection.connections.size()){
			ConnectionServer server = connection.connections.get(t);
			if(server.player != null && server.player.name.equals(name))
				return server;
			++t;
		}
		return null;
	}
}
