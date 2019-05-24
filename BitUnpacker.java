import java.io.DataInputStream;

/**
 *  LZW Compression
 *  BitUnpacker.java
 *  Purpose: accept as input a stream of bytes assumed to encode phrase numbers 
 *	in the minimum number of bits given the size of the dictionary in an LZW 
 *	trie assumed to have generated them. 
 *	Given the output of the bit-packer as input, the output of the bit-unpacker
 * 	should exactly match the input to the bit-packer.
 *  
 *  Authors: Sacha Raman and Elizabeth Macken
 *  
 */ 
public class BitUnpacker {
	
	// This outputs any remaining bits left in B (where we store our bits to be outputted)
	// Takes a count of bits in B, B itself and a bitCounter counting how many bits to output in
	public static void outputRemaining(long bitsInB, long B, long bitCounter) {
		if(bitsInB > 0L && B != 0L) {
			//work out how many more lines to output
			long lines = bitsInB / bitCounter;
			for(int i = 0; i < lines; i++) {
				//create a temporary variable and give it B's value
				long temp = B;
				//shift right zero fill to remove everything but the next value to output
				temp = temp >>> (64L - bitCounter);
				//output the value
				System.out.println(temp);				
				//remove the outputted value from B
				B = B << bitCounter;
				//adjust the number of bits in B counter
				bitsInB -= bitCounter;
				//if we have encountered the RESET phrase, reset bitCounter
				if(temp == 256) {
					bitCounter = 9L;										
				}			
			}
		}
	}
	

	public static void main(String[] args) {
		
		try {		
			
			//the number of lines read in
			int lineCounter = 0;
			//how many bits to output in 
			long bitCounter = 9L;
			//the size of our interval before we need to inc our bitCounter
			int interval = 255;
			//how many lines can we read in with our current bitCounter
			int bitBoundary = 255;
			//a temp long to store the byte just read in
			long byteRead = 0L;
			//the main long to store our read in bytes
			long B = 0L;
			//the number of bits in B
			long bitsInB = 0L;
			//our int to store the return value when we read in bytes
			int s = 0;
			//mask for negative input
			long mask = 0b11111111L;

			DataInputStream dis = new DataInputStream(System.in);
			byte[] byteArray = new byte[1];
			//read in 1 byte from input
			s = dis.read(byteArray, 0, 1);
			
			//while we have not reached EOF
			while(s != -1) {
				//copy our read in value into byteRead
				byteRead = byteArray[0];
				//incase it is a negative number, and with a mask
				byteRead = byteRead & mask;
				//shift contents left so it aligns with the right end of bits in B
				byteRead = byteRead << (64L - bitsInB - 8L);				
				//OR the read in bytes with the contents of B, store in B
				B = B|byteRead;
				//add 8 to our bitsInB value as we have added 8 bits to it
				bitsInB += 8L;
				//while there are 56 or more bits in B
				while(bitsInB >= 56L) {
					//if we have exceeded the bitBoundary
					if(lineCounter > bitBoundary) {
						bitCounter++;
						//double the interval
						interval = interval * 2;
						//add the interval to our bitBoundary
						bitBoundary += interval;
					}
					//create a temporary variable and give it B's value
					long temp = B;
					//shift right zero fill to remove everything but the next value to output
					temp = temp >>> (64L - bitCounter);					
					System.out.println(temp);
					//increment our lineCounter
					lineCounter++;
					//remove the outputted value from B
					B = B << bitCounter;
					//adjust the number of bits in B counter
					bitsInB -= bitCounter;
					//if we have encountered the RESET phrase, reset everything
					if(temp == 256) {
						lineCounter = 1;
						bitCounter = 9L;
						interval = 255;
						bitBoundary = 255;						
					}
				}
				//read in the next value
				s = dis.read(byteArray, 0, 1);
			}
			dis.close();		
			//output any remaining bits
			outputRemaining(bitsInB, B, bitCounter);				
		}
		catch (Exception ex) {
			System.err.println(ex);
		}
	}	
}

