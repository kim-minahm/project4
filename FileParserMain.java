import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class FileParserMain {
	//Variables for Reading files
	private static int bufferNum;
	private static int bufferSize;
	private static String inFile;
	private static  FileReader inStream;
	private static BufferedReader fileBuffRead;
	//Data structures
	private static bintree handleTree;
	
	public static void main(String[] args) {
		
		if (args.length == 3) {
			inFile = args[0];
			bufferNum = Integer.parseInt(args[1]);
			bufferSize = Integer.parseInt(args[2]);
			handleTree = new bintree(bufferNum, bufferSize);
			parseFile();
		}
		else{
			System.out.println("Invalid Arguments");
		}
		
	}
	
	public static void parseFile(){
	
		try{
			inStream = new FileReader(inFile);
		}
        catch (IOException e) {
            System.out.println("FILE DOES NOT EXIST");
        }
		
		fileBuffRead = new BufferedReader(inStream);
		String line;
		String[] stringBuffer;
		try {
			while((line = fileBuffRead.readLine()) != null){
				stringBuffer = line.split(" ");
				
				if(stringBuffer.length == 1) //If a one command thing like debug.
				{
					if(stringBuffer[0].equals("debug"))
					{
						//bintree
			            handleTree.inOrder();
			            BufferQueue bq = handleTree.memManage.buffer.buffers;
			            
			            int i = 0;
			            while(i < bq.size){
			            	Buffer n = bq.pop();
			            	int buffNum = n.blockNum;
			            	System.out.println("Block ID of buffer" + i + " is " + buffNum);
			            	i++;
			            	
			            }
					}
				}
				else if(stringBuffer.length == 4)
				{
					double x = Double.parseDouble(stringBuffer[1]);
					double y = Double.parseDouble(stringBuffer[2]);
					double[] searchCoords = {x,y};
					if(stringBuffer[0].equalsIgnoreCase("add")){
						handleTree.insert(searchCoords, stringBuffer[3]); //must make new binTree insert that handles watchers based on x, y
						System.out.println();
					}
					else { //search 

						handleTree.regionSearch(searchCoords, Double.parseDouble(stringBuffer[3]));
					}
				}
				else{ //Delete
				
					double x = Double.parseDouble(stringBuffer[1]);
					double y = Double.parseDouble(stringBuffer[2]);
					double[] searchCoords = {x,y};
					handleTree.remove(searchCoords);
				}
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
