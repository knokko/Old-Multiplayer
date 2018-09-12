package knokko.entity.ai;

import knokko.entity.Entity;

public abstract class EntityAI {
	
	public final Entity entity;

	public EntityAI(Entity owner) {
		entity = owner;
	}
	
	public byte[] addExtraData(byte[] data, int index){
		return data;
	}
	
	public int readExtraData(byte[] data, int index){
		return index;
	}
	
	public void update(){}
}
