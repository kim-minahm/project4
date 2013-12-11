
import java.io.IOException;
import java.nio.ByteBuffer;


public class BufferPool {
    private int blocksize;
    
    private int numCacheHits;       // stats
    private int numCacheMisses;
    private int numDiskReads;
    private int numDiskWrites;

    BufferQueue buffers;
    private long filesize;          // filesize won't change
    
    public BufferPool(String filename, int blockSize, int numberOfBuffers) 
            throws IOException {

        blocksize = blockSize;
        
        // create a set of buffers we can use
        buffers = new BufferQueue(numberOfBuffers, blockSize);

        // stats
        numCacheHits = 0;
        numCacheMisses = 0;
        numDiskReads = 0;
        numDiskWrites = 0;
    }
    
    public long fileSize(){
    	return filesize;
    }
    
    public void insert(byte[] space, MemHandle mem) {
    	Buffer buff = buffers.getBufferByBlock(mem.block);
    	if(buff!= null){
    		if(offset(mem.getHandle()) + space.length < blocksize){
    			buff.buff.position(offset(mem.getHandle()));
    			buff.buff.put(space, 0, space.length);
        		buff.dirty = true;
    		}else{//buffer will not hold it
    			byte[] s = new byte[blocksize];
    			int diff = blocksize - offset(mem.pos);
    			for(int i = 0; i < diff; i++){
    				s[i] = space[i];
    			}
    			buff.buff.put(s, offset(mem.getHandle()), s.length);
    			loadBlock(mem.block+1);
    			int count = 0;
    			for(int i = diff; i < space.length; i++){
    				s[count] = space[i];
    				count++;
    			}
    			buff.buff.position(offset(mem.getHandle()));
    			buff.buff.put(s, 0, count);
    		}
    		
    	}else{
    		buff = new Buffer(blocksize);
    		buff.blockNum = buffers.getFileSize()/blocksize;
    		//needs to write a new chunk of memory
    		numDiskWrites++;
    		//must load the new buffer just added
    		numDiskReads++;
    		buff.buff = ByteBuffer.allocate(blocksize);
    		filesize = filesize + blocksize;
    	}
    }
    
    /**
     * gets the byte array representing a watcher
     * @param mem
     * @return
     */
    public byte[] getWatcherData(MemHandle mem){
    	byte[] b = new byte[4];
    	b = buffers.getBytes(mem,2);
    	ByteBuffer temp = ByteBuffer.wrap(b);
    	int s = temp.getShort();
    	byte[] ret = new byte[1000];
    	ret = buffers.getBytes((short)(mem.pos+2), s);
    	return ret;
    }
    
	/**
	 * 0 is internal, 1 is leaf, -1 is empty
	 * @param m
	 * @return
	 */
	public byte[] getNodeData(MemHandle m){
		
	}
    
    public void remove(MemHandle mem){
    	short pos = mem.getHandle();
    	int block = (int)pos/blocksize;
    	int off = pos%blocksize;
    	Buffer buff = buffers.getBufferByBlock(block);
    	if(buff == null){
    		//load buffer from file
    	}
    	int size = buffers.getBytes(mem,2).length;
    	ByteBuffer empty = ByteBuffer.allocate(size);
    	//+2 because first two bytes represent size
    	buff.buff.put(empty.array(),off,empty.array().length);
    }
    
    public void loadBlock(int block){
		Buffer buff = buffers.getBufferByBlock(block);
		if(buff == null){
			numDiskReads++;
			filesize = filesize + blocksize;
			buffers.push(buff);
		}else{
			buffers.push(buff);
		}

    }
    
    /**
     * Write a buffer from memory to disk.
     * @param b buffer to write to disk
     */
    public void close() {
        buffers.close();
    }
    
    private int offset(short b){
    	return (int)b%blocksize;
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
