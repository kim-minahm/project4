import java.nio.ByteBuffer;


/**
 * emptyLeaf node for flyweight
 * @author Miles
 *
 */
public class emptyLeaf extends binNode{

	/**
	 * Does Nothing: Empty Leaf. No coords, no key, no children
	 */
	public emptyLeaf(MemManager m) {
		
		super.id = -1;
		super.byteArray = ByteBuffer.allocate(5);
		super.byteArray.putShort(0, super.id);
		
		super.handle = m.insert(super.byteArray.array(), super.byteArray.array().length);
		
	}

	@Override
	boolean isEmpty() {
		return true;
	}

	@Override
	boolean isLeaf() {
		return true;
	}
}
