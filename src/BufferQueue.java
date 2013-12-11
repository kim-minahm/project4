

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.LinkedList;

public class BufferQueue {



	/**
	 * node object for queue
	 * 
	 * @author kimminahm
	 * 
	 */
	private class Node {
		Buffer data;
		Node next;
		Node prev;

		public Node(Buffer d) {
			data = d;
		}
	}

	private Node head;
	private Node tail;
	int size;
	private int max;
	private Node current;
	private int buffSize;
	private short filesize;
	
	private LinkedList<Buffer> file = new LinkedList<Buffer>();
	
//    private RandomAccessFile file;
	

	/**
	 * makes a new empty queue
	 * 
	 * @param numBuffs
	 *            number for buffers
	 * @param buffSize
	 *            size of each buffer
	 */
	public BufferQueue(int numBuffs, int buffSizes) {
		head = new Node(null);
		tail = new Node(null);

		max = numBuffs;
		buffSize = buffSizes;
		
//        try {
//			file = new RandomAccessFile("p4bin.dat", "rw");
//	        filesize = 0; 
//		} catch (FileNotFoundException e) {
//			System.out.println("p4bin.dat not found");
//			e.printStackTrace();
//		}
        
        for(int i = 0; i < max; i++){
        	this.push(new Buffer(buffSize));
        }
	}

	/**
	 * adds a new buffer to the front of the queue
	 * 
	 * @param buff
	 */
	public void push(Buffer buff) {
		Node n = new Node(buff);
		if (size == max) {
			pop();
		}
		if (size == 0) {
			head.next = n;
			n.prev = head;
			n.next = tail;
			tail.prev = n;
		} else {
			n.next = head.next;
			n.prev = head;
			n.next.prev = n;
			head.next = n;
		}
		size++;
		current = n;
	}

	public Buffer pop() {
		if (size > 0) {
			if(tail.prev.data.dirty){
//				try {
//					file.seek(tail.prev.data.blockNum*buffSize);
//					file.write(tail.prev.data.buff.array(), 0, tail.prev.data.buff.array().length);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				
			}
			tail.prev.prev.next = tail;
			tail.prev = tail.prev.prev;
			size--;
			return current.data;
		}
		return null;
	}

	/**
	 * gets the LRU
	 * 
	 * @return
	 */
	public Buffer getLeastRecent() {
		return tail.prev.data;
	}

	/**
	 * gets the MRU
	 * 
	 * @return
	 */
	public Buffer getMostRecet() {
		return head.next.data;
	}

	/**
	 * checks if any of the buffers are of block b
	 * 
	 * @param b
	 * @return
	 */
	public boolean containsBlock(int b) {
		if(this.size == 0){
			return false;
		}
		if(b>size){
			return false;
		}
		current = head.next;
		while (current.data != null) {
			if (current.data.blockNum == b) {
				return true;
			}
			current = current.next;
		}
		return false;
	}

	/**
	 * sets block b to the most recent buffer
	 * 
	 * @param b
	 */
	public void setRecent(int b) {
		if (containsBlock(b)) {
			if (current.data.blockNum == b) {
				current.prev.next = current.next;
				current.next.prev = current.prev;
			}
		}
	}

	/**
	 * gets the buffer containing block b of the file
	 * 
	 * @param block
	 *            the block corrosponding
	 * @return
	 */
	public Buffer getBufferByBlock(int block) {
		if (containsBlock(block)) {
			return current.data;
		}
		else{
			this.loadBuffer(block);
			containsBlock(block);
			return current.data;
		}
	}
	
	public int getFileSize()
	{
		return filesize;
	}
	
	private void loadBuffer(int block){
//		if(filesize/buffSize < block){
//			Buffer buff = new Buffer(buffSize);
//			buff.blockNum = block;
//			filesize = (short) (filesize+buffSize);
//			buff.dirty = false;
//			push(buff);
//			return;
//		}
//		short pos = (short) (block * buffSize);
//		byte[] buff = new byte[buffSize];
//		try {
//			file.seek(pos);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		try {
//			file.read(buff, 0, buffSize);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		Buffer b = new Buffer(buffSize);
//		b.buff = ByteBuffer.wrap(buff);
//		this.push(b);
		if(!containsBlock(block)&&filesize/buffSize > block){
			this.push(file.get(block));
		}
	}

	public byte[] getBytes(MemHandle mem, int numBytes){
    	int off = mem.pos%buffSize;    	
		byte[] temp = new byte[numBytes];
		
		if(containsBlock((int)mem.block)){
			for(int i = 0; i < numBytes; i++){
				temp[i] = current.data.buff.get(off);
			}
			return temp;
		}
		return null;
	}
	
	public void grow(){
		filesize =(short) (filesize+buffSize);
		file.add(new Buffer(buffSize));
		file.getLast().blockNum = file.size()-1;
		this.push(file.getLast());
	}
	
	public byte[] getBytes(short pos, int numBytes){
    	int off = pos%buffSize;    	
		byte[] temp = new byte[numBytes];
		
		if(containsBlock((int)pos/buffSize)){
			for(int i = 0; i < numBytes; i++){
				temp[i] = current.data.buff.get(off);
			}
			return temp;
		}
		return null;
	}
	
	public void close(){
//		try {
//			file.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		for(int i = 0; i < size; i++){
			pop();
		}
	}
}
