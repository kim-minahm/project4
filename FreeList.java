import java.util.LinkedList;

//import structure.LinkedList;

public class FreeList {
	LinkedList<FreeSpace> list = new LinkedList<FreeSpace>();
	int currPos = 0;

	public MemHandle getFreeSpace(int size) {
		boolean f = false;
		int start = currPos;
		if (list.size() != 0) {
			while (true) {
				if (size < list.get(currPos).getSize()) {
					// the object fits in the current block so insert
					MemHandle ret = list.get(currPos).getHandle();
					int s = list.get(currPos).getSize();
					list.get(currPos).getHandle()
							.setHandle((short) (ret.getHandle() + s));
					list.get(currPos).setSize(
							list.get(currPos).getSize() - size);
					return ret;
				} else if (size == list.get(currPos).getSize()) {
					MemHandle ret = list.get(currPos).getHandle();
					list.remove(currPos);
					return ret;
				} else if (start == currPos && f) {
					// the loop has iterated through the entire freeList
					// write a block of size buffSize to data pool
					return null;
				} else {
					// freespace doesnt fit and its not finished so get next obj
					// on list
					f = true;
					// gets next position and wraps to front
					currPos = (currPos++) % list.size();
				}
			}
		}
		return null;
	}

	public void add(MemHandle pos, int size) {
		FreeSpace sp = new FreeSpace(size, pos);
		for (int i = 0; i < list.size(); i++) {
			if (sp.getHandle().getHandle() < list.get(i).getHandle()
					.getHandle()) {
				list.add(i, sp);
			}
		}
	}

	public void cleanUp() {
		for (int i = 0; i < list.size(); i++) {
			// this means that the handle+size = handle of next node meaning
			// they are adjacent
			// so merge together
			if (list.get(i).getHandle().getHandle() + list.get(i).getSize() == list
					.get(i + 1).getHandle().getHandle()) {
				list.get(i).setSize(
						list.get(i).getSize() + list.get(i + 1).getSize());
				list.remove(i + 1);
				i--;
			}
		}
	}
}
