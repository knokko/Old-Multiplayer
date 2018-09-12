package knokko.server;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JPanel;

public class ServerBoard extends JPanel {

	/**
	 * The serial version ID
	 */
	private static final long serialVersionUID = 6953269109757746292L;
	
	public final ServerMain server;
	
	ArrayList<String> console = new ArrayList<String>();
	
	public ServerBoard(ServerMain serverMain) {
		server = serverMain;
	}
	
	@Override
	public void paint(Graphics gr){
		gr.setColor(Color.BLACK);
		gr.fillRect(0, 0, server.getWidth(), server.getHeight());
		gr.setColor(Color.GREEN);
		int i = Math.max(console.size() - 20, 0);
		while(i < console.size()){
			String line = console.get(i);
			String time = line.substring(0, line.indexOf(":"));
			String message = line.substring(line.indexOf(":") + 1);
			drawString(time, gr, 20, server.getHeight() - (console.size() - i + 4) * 12);
			drawString(message, gr, 150, server.getHeight() - (console.size() - i + 4) * 12);
			++i;
		}
		gr.drawString(server.command, 150, server.getHeight() - (console.size() - i + 4) * 12);
		gr.drawString("random number: " + new Random().nextInt(100), 500, 100);
	}
	
	public void printLine(String line){
		if(line != null)
			console.add(((System.nanoTime() - ServerMain.startTime) / 1000000000.0) + ": " + line);
	}
	
	public void print(String string){
		int t = console.size() - 1;
		if(t >= 0){
			String line = console.get(t);
			line += string;
			console.set(t, line);
		}
		else
			printLine(string);
	}
	
	public PrintStream getConsoleStream(){
		return new PrintStream(new ConsoleStream(this));
	}
	
	private void drawString(String string, Graphics gr, int x, int y){
		try {
			int width = 0;
			boolean bool = string.contains("|");
			while(string.contains("|")){
				int index1 = string.indexOf("|");
				int index2 = string.indexOf("|", index1 + 1);
				String sub = string.substring(index1 + 1, index2);
				String first = string.substring(0, index1);
				gr.drawString(first, x + width, y);
				width += (int) gr.getFont().getStringBounds(first, ((Graphics2D) gr).getFontRenderContext()).getWidth();
				int index3 = string.indexOf("|", index2 + 1);
				String after = index3 > 0 ? string.substring(index2 + 1, index3) : string.substring(index2 + 1);
				int red = Math.min(sub.charAt(0), 255);
				int green = Math.min(sub.charAt(1), 255);
				int blue = Math.min(sub.charAt(2), 255);
				int alpha = 255;
				if(sub.length() == 4)
					alpha = sub.charAt(3);
				gr.setColor(new Color(red, green, blue, alpha));
				gr.drawString(after, x + width, y);
				string = string.substring(index3 > 0 ? index3 - 1 : index2 + 1);
				width += (int) gr.getFont().getStringBounds(after, ((Graphics2D) gr).getFontRenderContext()).getWidth();
			}
			gr.setColor(Color.GREEN);
			if(bool)
				return;
		} catch(Exception ex){
			ex.printStackTrace();
			return;
		}
		gr.drawString(string, x, y);
	}
	
	public class ConsoleStream extends OutputStream {
		
		public final ServerBoard board;

		public ConsoleStream(ServerBoard serverBoard) {
			super();
			board = serverBoard;
		}

		@Override
		public void write(int b) throws IOException {
			if((char)b != '\r')
				board.print((char)b + "");
			else
				board.printLine("");
		}
		
	}
}
