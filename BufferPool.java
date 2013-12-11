
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
    		if(offset(mem.pos) + space.length < blocksize){
    			buff.buff.put(space, offset(mem.getHandle()), space.length);
        		buff.dirty = true;
    		}else{//buffer will not hold it
    			byte[] s = new byte[1000];
    			int diff = offset(mem.pos) - blocksize;
    			for(int i = 0; i < diff; i++){
    				s[i] = space[i];
    			}
    			buff.buff.put(s, offset(mem.getHandle()), space.length);
    			loadBlock(mem.block+1);
    			int count = 0;
    			for(int i = diff; i < space.length; i++){
    				s[i] = space[i];
    				count++;
    			}
    			buff.buff.put(s, offset(mem.getHandle()), count);
    		}
    		
    	}else{
    		buff = new Buffer(blocksize);
    		buff.blockNum = buffers.getFileSize()/blocksize;
    		//needs to write a new chunk of memory
    		numDiskWrites++;
    		//must load the new buffer just added
    		numDiskReads++;
    		buff.buff = ByteBuffer.allocate(blocksize);
    		//load buffer from file
    	}
    }
    
    /**
     * gets the byte array that a MemHandle refers to
     * @param mem
     * @return
     */
    public byte[] getByteArray(MemHandle mem){
    	byte[] b = new byte[2];
    	b = buffers.getBytes(mem,2);
    	ByteBuffer temp = ByteBuffer.wrap(b);
    	int s = temp.getInt();
    	byte[] ret = new byte[1000];
    	ret = buffers.getBytes((short)(mem.pos+2), s);
    	return ret;
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
			buffers.push(buff);
		}else{
			
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
