import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class BufferPool {
	private int blocksize;

	private int numCacheHits; // stats
	private int numCacheMisses;
	private int numDiskReads;
	private int numDiskWrites;

	// BufferQueue buffers;
	private long filesize; // filesize won't change
	LinkedList<Buffer> file = new LinkedList<Buffer>();

	public BufferPool(String filename, int blockSize, int numberOfBuffers)
			throws IOException {

		blocksize = blockSize;

		// stats
		numCacheHits = 0;
		numCacheMisses = 0;
		numDiskReads = 0;
		numDiskWrites = 0;
	}

	public long fileSize() {
		return filesize;
	}

	public void insert(byte[] space, MemHandle mem) {
		Buffer current = new Buffer(blocksize);
		// checks for block.
		if (mem.block < file.size() - 1) {
			// it fits within the buffer
			if (offset(mem.pos) + space.length < blocksize) {
				current = file.get(mem.block);
				ByteBuffer dat = current.buff;
				dat.position(offset(mem.getHandle()));
				dat.put(space);
			} else {
				current = file.get(mem.block);
				Buffer current2 = file.get(mem.block + 1);
				ByteBuffer dat = current.buff;
				ByteBuffer dat2 = current2.buff;
				int diff = offset(mem.pos) + space.length - blocksize;
				dat.position(offset(mem.getHandle()));
				dat.put(space, 0, space.length - diff);
				dat2.position(0);
				dat2.put(space, space.length - diff, diff);
			}
		} else {
			while (file.size() - 1 <= mem.block) {
				file.add(new Buffer(blocksize));
				file.get(file.size() - 1).blockNum = file.size() - 1;
			}
			current = file.get(mem.block);
			ByteBuffer dat = current.buff;
			dat.position(offset(mem.getHandle()));
			dat.put(space);
		}
	}

	/**
	 * gets the byte array representing a watcher
	 * 
	 * @param mem
	 * @return
	 */
	public byte[] getWatcherData(MemHandle mem) {
		Buffer current = file.get(mem.block);
		byte[] b = new byte[2];
		current.buff.position(offset(mem.pos));
		current.buff.get(b, 0, 2);
		ByteBuffer temp = ByteBuffer.wrap(b);
		int s = temp.getShort();
		if (offset(mem.pos) + s < blocksize) {
			byte[] ret = new byte[1000];
			current.buff.position(offset(mem.pos) + 2);
			current.buff.get(ret, 0, s);
			return ret;
		} else {
			int diff = blocksize - offset(mem.pos);
			byte[] ret = new byte[1000];
			byte[] ret2 = new byte[1000];
			current.buff.position(offset(mem.pos) + 2);
			current.buff.get(ret, 0, s - diff);
			current = file.get(mem.block + 1);
			current.buff.position(0);
			current.buff.get(ret2, 0, diff);
			int base = ret.length;
			for (int i = 0; i < ret2.length; i++) {
				ret[base + i] = ret2[i];
			}
			return ret;
		}
	}

	/**
	 * 0 is internal, 1 is leaf, -1 is empty
	 * 
	 * @param m
	 * @return
	 */
	public byte[] getNodeData(MemHandle mem) {
		Buffer current = file.get(mem.block);
		byte[] b = new byte[10];
		current.buff.get(b, offset(mem.pos), 1);
		ByteBuffer temp = ByteBuffer.wrap(b);
		int s = temp.get();
		int sz;
		if (s == 1) {
			sz = 5;
		} else {
			sz = 9;
		}
		if (offset(mem.pos) + sz < blocksize) {
			if (s == 1) {// 5 bytes, 1 sig, 4 handle
				current.buff.get(b, offset(mem.pos), 5);
			} else {// 9 bytes, 1 sig, 4 leftchild, 4 rightchile
				current.buff.get(b, offset(mem.pos), 9);
			}
			return b;
		} else {
			int diff;
			byte[] b2 = new byte[10];
			if (s == 1) {// 5 bytes, 1 sig, 4 handle
				diff = blocksize - (offset(mem.pos)+5);
				current.buff.position(offset(mem.pos));
				current.buff.get(b,0,offset(mem.pos)-diff );
				current = file.get(mem.block+1);
				current.buff.position(0);
				current.buff.get(b2,0,offset(mem.pos)-diff );
			} else {// 9 bytes, 1 sig, 4 leftchild, 4 rightchile
				diff = blocksize - (offset(mem.pos)+9);
				current.buff.position(offset(mem.pos));
				current.buff.get(b,0,offset(mem.pos)-diff );
				current = file.get(mem.block+1);
				current.buff.position(0);
				current.buff.get(b2,0,offset(mem.pos)-diff );
				int base = b.length;
				for (int i = 0; i < b2.length; i++) {
					b[base + i] = b2[i];
				}
			}
			return b;
		}
	}

	public void removeWatcher(MemHandle mem) {
		Buffer current = file.get(mem.block);
		byte[] b = new byte[2];
		current.buff.get(b, offset(mem.pos), 2);
		ByteBuffer temp = ByteBuffer.wrap(b);
		int s = temp.getShort();
		byte[] ret = new byte[s];
		current.buff.position(offset(mem.pos));
		current.buff.put(ret, offset(mem.pos), s);
	}

	public void removeNode(MemHandle mem) {
		Buffer current = file.get(mem.block);
		byte[] b = new byte[10];
		current.buff.get(b, offset(mem.pos), 1);
		ByteBuffer temp = ByteBuffer.wrap(b);
		int s = temp.get();
		current.buff.position(offset(mem.pos));
		if (s == 1) {// 5 bytes, 1 sig, 4 handle
			current.buff.put(new byte[5], offset(mem.pos), 5);
		} else {// 9 bytes, 1 sig, 4 leftchild, 4 rightchile
			current.buff.put(new byte[9], offset(mem.pos), 9);
		}
	}

	/**
	 * Write a buffer from memory to disk.
	 * 
	 * @param b
	 *            buffer to write to disk
	 */

	private int offset(short b) {
		return (int) b % blocksize;
	}

	public int getNumCacheHits() {
		return numCacheHits;
	}

	public int getNumCacheMisses() {
		return numCacheMisses;
	}

	public int getNumDiskReads() {
		return numDiskReads;
	}

	public int getNumDiskWrites() {
		return numDiskWrites;
	}
}
