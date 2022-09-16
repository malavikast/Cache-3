import java.util.*;
import java.io.*;
import java.math.*;


public class Cache {
	
	static HashMap<String, String> Cache = new HashMap<String, String>();
    static HashMap<String, String> MainMemory = new HashMap<String, String>();
    static File file = new File("C:/Users/Public/CA/output_data.txt");
    static FileWriter fw;
    static BufferedWriter bw;
    
	static String[] size;
    static String[] memory_Data; // main memory data
    static String[] instr_addr; 
    static int totalAccess=0;
    static int totalHits=0;
    static int totalInstructionAddress=0;
    static int totalClockCycles=0;
   
 // To read the bytes starting from the given address, Input(start address to read, first byte address,128 bit data, number of bytes)

    String read_Byte_Data(String startAddress,String readAddress,String data,String size)
    {
       try{
        int i=32;
        int byteNumber=Integer.parseInt(readAddress.substring(28,32),2); // obtaining byte index
        size=size.substring(0,size.length());

        int num_bytes=Integer.parseInt(size)/2;
        int num_digits=(num_bytes)*2; // obtaining no. of bytes and digits to read, considering 1 byte has 2 digits
        totalAccess++;

        if(32-(byteNumber*2)-num_digits>=0)
        {
            return data.substring(32-(byteNumber*2)-num_digits,32-(byteNumber*2));

        }
        else{
            int temp =Integer.parseInt(startAddress.substring(0,28),2);
            temp=temp+1;
            String add1=String.format("%28s", Integer.toBinaryString(temp)).replaceAll(" ", "0");
            return this.read_data_addr(add1+"0000",String.valueOf(num_bytes-(16-byteNumber)))+data.substring(0,32-(byteNumber*2)); // straddle condition
        }
       }
       catch(IOException e)
       {
            e.printStackTrace();  
       }
        return "";
      
    }


    
//Convert hexadecimal value to Binary value
String hexToBinary(String hex)
{
    String binary = "";
    hex = hex.toUpperCase();
    HashMap<Character, String> hashMap = new HashMap<Character, String>();

    hashMap.put('0', "0000");
    hashMap.put('1', "0001");
    hashMap.put('2', "0010");
    hashMap.put('3', "0011");
    hashMap.put('4', "0100");
    hashMap.put('5', "0101");
    hashMap.put('6', "0110");
    hashMap.put('7', "0111");
    hashMap.put('8', "1000");
    hashMap.put('9', "1001");
    hashMap.put('A', "1010");
    hashMap.put('B', "1011");
    hashMap.put('C', "1100");
    hashMap.put('D', "1101");
    hashMap.put('E', "1110");
    hashMap.put('F', "1111");

    int i;
    char ch;

    for (i = 0; i < hex.length()-1; i++) {
        
        ch = hex.charAt(i);
        if (hashMap.containsKey(ch))
            binary += hashMap.get(ch);
        else {
            binary = "Invalid Hexadecimal String";
            return binary;
        }
    }

    return binary;
}



//to obtain data from address. Input(32bit address, number of bytes to read).
String read_data_addr(String address,String num_bytes) throws IOException{

    String tag_index=address.substring(0,28);
        //check the address in cache. 
        if(Cache.containsKey(tag_index)) 
        {
            totalHits++;
            totalClockCycles++;
            return this.read_Byte_Data(tag_index+"0000",address,Cache.get(tag_index),num_bytes);
        }
        //else obtain the data from main memory and update in cache.
        else
        {
            totalClockCycles=totalClockCycles+15;
            String value=MainMemory.get(tag_index);
            String indexBits=tag_index.substring(19,28);
            int j=0;
            String foundindex="";

            //Compare Index bit of given address with index bit of cache line. If present, replace data and change the hashmap key.
            for (Map.Entry<String, String> set : Cache.entrySet()) {
                if(set.getKey().substring(19,28).equals(indexBits))
                {
                    foundindex=set.getKey();
                    break;
                }
                j++;
            }
            if(j==Cache.size())
            {
                Cache.put(tag_index,value);
                return this.read_Byte_Data(tag_index+"0000",address,Cache.get(tag_index),num_bytes);
            }
            else
            {
                Cache.put(foundindex,value);
                return this.read_Byte_Data(foundindex+"0000",address,Cache.get(foundindex),num_bytes);
            }
        }
}

public static void main(String args[]) throws IOException{

    if(!file.exists())
    {
        file.createNewFile();
    }
    Cache ca= new Cache();

    //Reading the address, data size and instruction address from the files
    Scanner addresses= new Scanner(new File("C:/Users/Public/CA/inst_addr_trace_hex_project_1.txt")).useDelimiter("\n");
    Scanner readNumOfBytes= new Scanner(new File("C:/Users/Public/CA/inst_data_size_project_1.txt")).useDelimiter("\n");
    Scanner mainMemoryData= new Scanner(new File("C:/Users/Public/CA/inst_mem_hex_16byte_wide.txt")).useDelimiter("\n");


    List<String> hex_bin_list = new ArrayList<String>();
    List<String> read_numbytes_list = new ArrayList<String>();
    List<String> data_list = new ArrayList<String>();


    String str="";

    //converting and storing the hexadecimal addresses to binary address.
    while (addresses.hasNext()) {
        str = addresses.next();
        hex_bin_list.add(ca.hexToBinary(str));
    }
    addresses.close();

    //Datasize to be read at each address
    while (readNumOfBytes.hasNext()) {
        str = readNumOfBytes.next();
        read_numbytes_list.add(str.substring(0,str.length()-1));
    }
    readNumOfBytes.close();

    //Reading 16 byte data.
    while (mainMemoryData.hasNext()) {
        str = mainMemoryData.next();
        data_list.add(str);
    }
    mainMemoryData.close();

    int i=0;

    instr_addr = hex_bin_list.toArray(new String[0]);
    size=read_numbytes_list.toArray(new String[0]);
    memory_Data=data_list.toArray(new String[0]);

  FileWriter fWriter = new FileWriter("C:/Users/Public/CA/output_data.txt");
  fw=new FileWriter(file.getAbsoluteFile());
  bw= new BufferedWriter(fw);
    
    //Creating main memory hashmap(28 bit address,128bit data).
    for(String memoryData:memory_Data)
    {
        MainMemory.put(String.format("%28s", Integer.toBinaryString(i)).replaceAll(" ", "0"),memoryData);
        i++;
    }
    i=0;


    //Reading the data from the address.
    for(String tarray:instr_addr)
    {
        totalInstructionAddress++;
        bw.write(ca.read_data_addr(tarray,size[i]));
        bw.write("\n");
        i++;
    }
    
    System.out.println("Total number of Instruction addresses="+totalInstructionAddress);
    System.out.println("Total number of clock cycles="+totalClockCycles);
    System.out.println("Instructions per cycle="+ ((float)totalInstructionAddress/totalClockCycles));
    System.out.println("Total number of cache accesses="+totalAccess);
    System.out.println("Total number of hits="+totalHits);
    System.out.println("Hit ratio="+((float)totalHits/totalAccess));
    


    bw.close();
    fWriter.close();

}

}
