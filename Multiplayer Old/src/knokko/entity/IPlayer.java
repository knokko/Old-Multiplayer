package knokko.entity;

/**
 * Any entity that implements this interface, will be seen as player.
 * Enemies will attack this entity.
 * @author knokko
 *
 */
public interface IPlayer {
	
	public void attack(float damage);
	
	public java.awt.geom.Point2D.Float getPosition();
}
