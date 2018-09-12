package knokko.world;

import knokko.client.ClientMain;

public class WorldClient extends World {
	
	public final ClientMain client;

	public WorldClient(int width, int height, long worldTicks, long passedTime, long sendTime, ClientMain clientMain, String name) {
		super(width, height, worldTicks, System.nanoTime(), passedTime, name);
		client = clientMain;
		long lostTime = System.currentTimeMillis() - sendTime;
		long lostTicks = lostTime / 20;
		int t = 0;
		while(t < lostTicks){
			update();
			++t;
		}
	}

	@Override
	public boolean isClientWorld() {
		return true;
	}
}
