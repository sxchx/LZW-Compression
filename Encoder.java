//Authors: Elizabeth Macken and Sacha Raman


import java.util.*;
import java.io.*;

public class Encoder {
    public static void main(String[] args) {
        // Checking the correct number of arguments was passed
        if (args.length != 1) {
            System.err.println("ERROR - Correct usage: java Encoder [integer]");
            System.exit(1);
        }
        else {
            try {
                // Getting the k-value passed and checking that it is at least 9 (the tree must be primed with 256 values which already uses 9 bits to encode)
                int size = Integer.parseInt(args[0]);
                if (size < 9) {
                    System.err.println("ERROR - max bit size must be at least 9");
                    System.exit(1);
                }
                
                // Creating a reader that will get bytes in and a writer that will write one line at a time
                DataInputStream in = new DataInputStream(System.in);
                PrintWriter out = new PrintWriter(System.out);
                // Creating a MultiWay Trie and passing it the max-bit value
                MultiWayTrie mwt = new MultiWayTrie(size);
                // Creating a linked list to store the array of bytes previously seen that match a pattern
                LinkedList<Byte> previousBytes = new LinkedList<Byte>();
                // Initialising the index values
                int currentIndex = 257;
                int previousIndex = -1;
                
                // While the reader still has a byte available from System.in...
                while (in.available() > 0) {
                    // Get the byte and check if the string of previous bytes that matched + this byte are a match
                    byte currByte = in.readByte();
                    int index = mwt.contains(previousBytes, currByte, currentIndex);
                    // If it does match a phrase in the MWT, and it is not the last byte available, add it to the list of previous byte matches and check the next byte
                    if (index != -1 && in.available() != 0) {
                        previousBytes.add(currByte);
                        previousIndex = index;
                    }
                    // Otherwise we either have a mismatch or we are on the last byte available
                    else {
                        // Print out the phrase number for the node in the trie that corresponds to the previous bytes that were a match
                        out.println(previousIndex);
                        out.flush();
                        // Clear the list of previous matches and add only the current byte
                        previousBytes.clear();
                        previousBytes.add(currByte);
                        previousIndex = mwt.getIndex(currByte);
                        // Increment the index of nodes in the MWT
                        currentIndex++;
                        // Check if adding that last node meant we have reached the MWT capacity
                        if (mwt.isFull() == true) {
                            // Printing out the current byte that was stored in the previous bytes list for a match: we have reached capacity so have to start from scratch
                            out.println(mwt.getIndex(currByte));
                            // Printing the unique reset value
                            out.println("256");
                            out.flush();
                            // Reseting the MWT to hold just the initial data again and reseting the indices and previous bytes linked list
                            mwt.reset();
                            previousBytes.clear();
                            currentIndex = 257;
                            previousIndex = -1;
                        }
                        // Checking if that was the last byte available, if so we need to output the current byte, otherwise we can check for a match with the next byte
                        if (in.available() == 0) {
                            out.println(mwt.getIndex(currByte));
                            out.flush();
                        }
                    }
                    
                }
                // Closing the reader and writer objects
                out.close();
                in.close();
            }
            catch (Exception ex) {
                // Catching all exceptions in main and printing relevant information
                System.err.println("ERROR - " + ex.getMessage());
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }
}

class MultiWayTrie {
    // Creating the initial root Node that will have as its children the initial byte values
    private Node root_;
    // Creating an int value to hold the maxPhraseNumber we will get from the arguments
    private int maxPhraseNum_;
    // Creating a boolean flag that will be set to true when the MWT is full
    private boolean full_ = false;
    
    // MultiWayTrie constructor, passed an integer as an argument for the max-bit size for the phrase number
    public MultiWayTrie(int dictionarySize) {
        // Getting the maximum phrase number for the bit number passed
        maxPhraseNum_ = (int)Math.pow(2, dictionarySize);
        // Creating the root node and priming it with the initial byte values
        root_ = new Node((byte)0, -1);
        root_.primeTree();
    }
    
    // Public method to see whether the phrase given by a list of previous bytes follwed by a current byte exists in the MWT
    public int contains(LinkedList<Byte> previous, byte current, int index) {
        // If the index now equals the maximim phrase number allowed, set the full_ flag to true
        if (index == maxPhraseNum_) {
            full_ = true;
        }
        // Starting from the root, for as many items as are in the previous bytes list...
        Node currNode = root_;
        for (int i = 0; i < previous.size(); i++) {
            // Get the children of the current node
            LinkedList<Node> currChildren = currNode.getChildren();
            for (int j = 0; j < currChildren.size(); j++) {
                if (currChildren.get(j).getData() == previous.get(i)) {
                    // If so, make that child the current node and break out of this inner loop
                    currNode = currChildren.get(j);
                    break;
                }
            }
        }
        // Getting the children of the current node which encodes all bytes in the 'previous' list
        LinkedList<Node> currChildren = currNode.getChildren();
        // Checking each byte in the children list to see if it matches the current byte we are looking for
        for (int j = 0; j < currChildren.size(); j++) {
            //If it does, return the index of that node
            if (currChildren.get(j).getData() == current) {
                return currChildren.get(j).getIndex();
            }
        }
        // Now we know that there is not already a match or else it would have returned already, we create a new node with the given byte value and index, and set the it as a child to the current node
        Node child = new Node(current, index);
        currNode.setChild(child);
        // Return a -1 to indicate no match was found
        return -1;
    }
    
    // Public method to get the index of a byte of data. Returns an int.
    public int getIndex(byte data) {
        // Getting the linked list of children of the root node (the initial byte values)
        LinkedList<Node> rChildren = root_.getChildren();
        // Goes through all the bytes in order  until that byte value matches the byte value passed in.
        for (int i = 0; i < 256; i++) {
            if (rChildren.get(i).getData() == data) {
                // Returns the index of the node whose byte value matches the byte value passed in.
                return rChildren.get(i).getIndex();
            }
        }
        // Only if no byte value was a match should this execute, if the program is run as is intended this should never occur
        return -1;
    }
    
    // Public method to reset the tree by deleting root's current children and replacing them with new nodes that don't have children of their own
    public void reset() {
        root_.primeTree();
        full_ = false;
    }
    
    // Public getter for the full_ variable. Returns a boolean.
    public boolean isFull() {
        return full_;
    }
    
    // Private inner class of a Node, which stores a data value (byte), an index value (int), and a list of children (LinkedList<Node>)
    private class Node {
        // Creating variables to store the data and index of this node, and a list of children
        private byte data_;
        private int index_;
        private LinkedList<Node> children_;
        
        // Node constructor, passed a byte and index value and stores them as internal variables
        public Node(byte data, int index) {
            data_ = data;
            index_ = index;
            children_ = new LinkedList<Node>();
        }
        
        // Only to be run on the root node, creates 256 node children with byte values -128 to 127 plus one reset value of index 256
        public void primeTree() {
            // Removing all current children from the list
            children_.clear();
            // Looping through 256 times adding new nodes with byte values starting from -128 and incrementing by 1 each time
            byte b = -128;
            for (int i = 0; i < 256; i++) {
                children_.add(new Node(b, i));
                b++;
            }
            // Adding the reset node with the unique phrase number 256
            children_.add(new Node((byte)0, 256));
        }
        
        // Getter for the data variable of this node. Returns a byte
        public byte getData() {
            return data_;
        }
        
        // Getter for the index variable of this node. Returns an int
        public int getIndex() {
            return index_;
        }
        
        // Getter of the children variable for this node. Returns a linked list
        public LinkedList<Node> getChildren() {
            return children_;
        }
        
        // Adds a child to this nodes list of children
        public void setChild(Node child) {
            children_.add(child);
        }
    }
}
