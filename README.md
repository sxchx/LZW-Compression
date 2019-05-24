# LZW-Compression
LZW compression and decompression with four parts - Encoder, Bit-Packer, Bit-Unpacker, and Decoder.

## Encoder
Takes a stream of bytes from standard input and uses LZW to output phrase numbers. Accepts as a commandline argument one integer specifying the maximum number of bits that can be used to encode a phrase number (i.e. a limit on the size of the dictionary). 

## Bit Packer
Accepts phrase numbers in the format expected as output from the encoder,	one number per line, and outputs them using the minimum number of bits given the number of phrases known to be in the dictionary at that moment. Output is the bit sequence in a format that can be subsequently read as a byte stream. 

## Bit Unpacker
Accept as input a stream of bytes assumed to encode phrase numbers in the minimum number of bits given the size of the dictionary in an LZW trie assumed to have generated them. Given the output of the bit-packer as input, the output of the bit-unpacker matches the input to the bit-packer.

## Decoder
Reads phrase numbers from standard input, one per line, and outputs the corresponding sequence of bytes assuming an LZW encoding.  Given the output of the encoder as input, the output from the decoder exactly matches the input to the encoder. 

## Usage
```bash
$ javac *.java
$ java Encoder <maximum number of bits used to encode phrase number> <file> | java BitPacker | java BitUnpacker | java Decoder > <output file name>
```
