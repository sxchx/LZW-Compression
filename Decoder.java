//Authors: Elizabeth Macken and Sacha Raman


import java.util.*;
import java.io.*;

public class Decoder {
    // Creating global variables for the methods to access
    private static LinkedList<Node> dataTree;
    private static Node currNode;
    private static Node previousNode;
    private static LinkedList<Byte> currOutput;
    private static LinkedList<Byte> previousOutput;
    private static DataOutputStream out;
    
    public static void main(String[] args) {
        // Checking that no arguments were passed
        if (args.length != 0) {
            System.err.println("ERROR - Correct usage: java Decoder");
            System.exit(1);
        }
        else {
            try {
                // Initialising the phrase dictionary
                dataTree = new LinkedList<Node>();
                primeDictionary(dataTree);
                
                // Creating new reader and writer objects
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                out = new DataOutputStream(System.out);
                
                // Creating index variables
                int currIndex;
                int previousIndex;
                
                // Reading the first phrase number and outputting the corresponding byte
                currIndex = Integer.parseInt(in.readLine());
                outputPhrase(currIndex, true);
                
                // While there are still lines to be read...
                String line;
                while ((line = in.readLine()) != null) {
                    // Set the previous index as the last index read, and then read a new phrase number and store as the current index
                    previousIndex = currIndex;
                    currIndex = Integer.parseInt(line);
                    // Check if the current index number is the reset number
                    if (currIndex == 256) {
                        // If so, clear the current phrase dictionary and prime it with the initail values again
                        dataTree.clear();
                        primeDictionary(dataTree);
                        
                        // Read the next phrase number and output the phrase it corresponds to
                        currIndex = Integer.parseInt(in.readLine());
                        outputPhrase(currIndex, true);
                    }
                    // Otherwise check that the current index does not equal the size of the dictionary (that is, we are not trying to access the node we added immediately previously)
                    else if (currIndex != dataTree.size()) {
                        // Then, output the phrase that corresponds to the current phrase number
                        outputPhrase(currIndex, true);
                        
                        // Add a new node with all the data of the previous phrase and the first byte of the current phrase
                        previousNode = dataTree.get(previousIndex);
                        previousOutput = previousNode.getData();
                        Node toAdd = new Node(previousOutput, currOutput.getFirst());
                        dataTree.add(toAdd);
                    }
                    else {
                        // Otherwise, the phrase is the last one we saw, so output the last phrase, signalling the difference with the false boolean flag
                        outputPhrase(previousIndex, false);
                        
                        // Create and add a new Node with all the data of the previous phrase plus the first byte of the previous phrase
                        Node toAdd = new Node(previousOutput, previousOutput.getFirst());
                        dataTree.add(toAdd);
                    }
                }
                // Closing the reader and writer objects
                out.close();
                in.close();
            }
            catch (Exception ex) {
                // Catching all exceptions in main and printing relevant information
                System.err.println("ERROR - " + ex.toString());
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }
    
    // Public method to initialise the phrase dictionary with the 256 node children with byte values -128 to 127 plus one reset value of index 256
    public static void primeDictionary(LinkedList<Node> dataTree) {
        // Looping through 256 times adding new nodes with byte values starting from -128 and incrementing by 1 each time
        byte b = -128;
        for (int i = 0; i < 256; i++) {
            dataTree.add(new Node(b));
            b++;
        }
        // Adding the reset node at index 256
        dataTree.add(new Node((byte)0));
    }
    
    // Public method to print the phrase associated with a given index, given a flag so it knows whether it is to read from the currOutput or the previousOutput list
    public static void outputPhrase(int index, boolean current) {
        try {
            // Check if we are dealing with the current or previous output
            if (current == true) {
                // If current, set the currNode to the node specified by the index passed and get the linked list of the phrase that encodes
                currNode = dataTree.get(index);
                currOutput = currNode.getData();
                // For each byte in that list, output it
                for (int i = 0; i < currOutput.size(); i++) {
                    out.write(currOutput.get(i));
                }
            }
            else {
                // If previous, set the previousNode to the node specified by the index passed, and get the linked list of the phrase that encodes
                previousNode = dataTree.get(index);
                previousOutput = previousNode.getData();
                // For each byte in that list, output it
                for (int i = 0; i < previousOutput.size(); i++) {
                    out.write(previousOutput.get(i));
                }
                // Finally, output the first byte in that list
                out.write(previousOutput.getFirst());
            }
        }
        catch (Exception ex) {
            // Catching all exceptions in main and printing relevant information
            System.err.println("ERROR - " + ex.toString());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

class Node {
    // Variable to store the data this node encodes for
    private LinkedList<Byte> data_;
    
    // First constructor for a node, using just one byte
    public Node(byte b) {
        // Create a new linked list of bytes and add the passed byte to it
        data_ = new LinkedList<Byte>();
        data_.add(b);
    }
    
    // Second constructor for a node, using a linked list of previous bytes and the current byte
    public Node(LinkedList<Byte> previous, byte curr) {
        // Create a new linked list of bytes
        data_ = new LinkedList<Byte>();
        // For each item in the list of previous bytes, add them to the data list for this node
        for (int i = 0; i < previous.size(); i++) {
            data_.add(previous.get(i));
        }
        // Finally add the current byte
        data_.add(curr);
    }
    
    // Public getter for the data variable of this node. Returns a linked list
    public LinkedList<Byte> getData() {
        return data_;
    }
}
