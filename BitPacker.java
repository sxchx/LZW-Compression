import java.io.DataOutputStream;
import java.util.Scanner;


/**
 *  LZW Compression
 *  BitPacker.java
 *  Purpose: accepts phrase numbers in the format expected as output from the encoder,
 *	one number per line, and outputs them using the minimum number of bits given the 
 *	number of phrases known to be in the dictionary at that moment. Output is the bit
 * 	sequence in a format that can be subsequently read as a byte stream. 
 *  
 *  Authors: Sacha Raman and Elizabeth Macken
 *  
 */

public class BitPacker {
	
	// This takes a integer B which we will compute operations on and holds our bits,
	// a DataOutputStream to output with and a int counter of number of times to repeat.
	// Returns the integer B.
	public static long printByte(long B, DataOutputStream dos, long counter) {		
		
		try {			
			//the mask: we only want to keep the left most 8 bits
			long mask = 0b1111111100000000000000000000000000000000000000000000000000000000L;	
			long output = 0L;	
			for (int i = 0; i < counter; i++) {		
				//mask out everything except the 8 left-most bits of B
				output = B & mask;
				//discard the left eight bits from B
				B = B << 8L;
				//Right shift zero fill to shuffle the value to the right end
				output = output >>> 56L;
				int intOutput = (int) (long) output;
				//then output it
				dos.write(intOutput);
			}
		}
		catch (Exception ex) {
			System.err.println("Error: " + ex);
		}
		return B;
	}
	
	// Prints out the final lot of bits left (< 8 bits).
	// Takes a long with what to print out and another int with the number of bits left.
	// And a DataOutputStream object to print out to
	public static void endBits(long bitsLeft, long B, DataOutputStream dos) {
		
		try {
			//shift the contents of B all the way to the right
			long temp = B >>> (64L - bitsLeft);
			//Adjust the bits into a 8bit space by shifting them left 
			temp = temp << (8L - bitsLeft);
			int intTemp = (int) (long) temp;
			//output the last 8 bits
			dos.write(intTemp);	
		}
		catch(Exception ex) {
			System.err.println("Error: " + ex);
		}		
	}
	
	// This method prints out any remaining bits from B once we have finished reading in lines
	public static void outputRemaining(long bitsInB, long B, DataOutputStream dos) {
		
		long byteCount = bitsInB / 8L;		
		//call printByte to output any whole bytes there are
		B = printByte(B, dos, byteCount);
		//adjust the count of bits in B
		bitsInB -= (8L * byteCount);
		//output any remaining bits by calling endBits
		endBits(bitsInB, B, dos);			
	}
	
	
	
	public static void main(String[] args) {
		
		try {
			//DECLARE VARIABLES
			//counts the number of lines read in
			int lineCounter = 0;
			//how many bits to output in
			long bitCounter = 9L;
			//the size of our interval before we need to inc our bit counter
			int interval = 255;
			//how many lines can we read in with our current bitCounter
			int bitBoundary = 255;
			//our 54 bit long for all operations
			long B = 0L;			
			//the number of bits used in our 64bit long we are doing all operations on
			long bitsInB = 0L;
			//how many bits to offset our main long B before storing the next value in
			long offset = 64L;			
			//an integer to store a read in phrase number
			long input = 0L;
			//RESET flag
			boolean reset = false;
			//The reader we will use to read in input from standard input
			Scanner sc = new Scanner(System.in);			
			//The writer we will use to output our byte stream to standard output
			DataOutputStream dos = new DataOutputStream(System.out);			
			
			//while there are still phrase numbers to read in
			while (sc.hasNextLong() == true) {
				//read in a value
				input = sc.nextLong();
				lineCounter++;		
				
				//if our long B is too full to take our next value, remove and output some bytes from it.
				if((bitCounter + bitsInB) > 64L) {
					//figure out how many bytes we must remove
					long bytesToRemove = (((bitCounter + bitsInB) - 64L) / 8L) + 1L;
					//output the bytes from B
					B = printByte(B, dos, bytesToRemove);
					//we have removed some bits from B so adjust our B bit counter
					bitsInB -= bytesToRemove * 8L;
					//reset the offset for our input
					offset = 64L - bitsInB;
				}
				
				//check if input is the reset phrase 257
				if(input == 256L) {
					reset = true;	
				}
				offset -= bitCounter;
				//shift input left by offset
				input = input << offset;
				//stick input into B using OR
				B = B|input;
				//adjust number of bits in B
				bitsInB += bitCounter;
				//if we have filled up B
				if(bitsInB == 64) {
					//output the contents of B in 8 bytes
					B = printByte(B, dos, 8);					
					//we have removed 64bits from B so adjust bitsInB
					bitsInB -= 64;
					//reset the offset for our input
					offset = 64 - bitsInB;		
				}
				//check if there is another line to read in
				sc.hasNextLong();
				//if we have exceeded the bitBoundary
				if(lineCounter > bitBoundary) {
					//now we will output with 1 more bit
					bitCounter++;
					//double the size of our interval
					interval = interval * 2;
					//update our bit boundary by adding the new interval to it
					bitBoundary = bitBoundary + interval;
				}
				//if the reset value has been encountered, reset values
				if(reset == true) {
					lineCounter = 1;
					bitCounter = 9L;
					interval = 255;
					bitBoundary = 255;
					reset = false;
				}
			}
			sc.close();
			//if there are still bits to output	call outputRemaining to do so		
			if(bitsInB > 0) {
				outputRemaining(bitsInB, B, dos);			
			}
			//close our DataOutputStream
			dos.close();			
		}
		catch (Exception ex) {
			System.err.println("Error: " + ex);
		}
	}	
}
