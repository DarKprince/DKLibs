package it.dk.libs.helper;

import java.io.*;

/**
 * Helper class for stream tasks
 *
 */
public class StreamHelper {
	//---------- Constructors

	//---------- Private fields

	//---------- Public properties

	//---------- Public methods
    /**
     * Copy a String inside a {@link OutputStream}
     * @param source
     * @param output
     * @throws IOException
     */
	public static void copyStringToOutpuStream(String source, OutputStream output)
    throws IOException {
    	OutputStreamWriter out = new OutputStreamWriter(output);
    	out.write(source);
//    	StringBufferInputStream in = new StringBufferInputStream(source);
//    	copyInToOut(in, output, false);
    }
	
    /**
     * Convert an {@link InputStream} to a string
     * 
     * @param is source {@link InputStream}
     * @return the string
     * @throws IOException
     */
    public static String convertStreamToString(InputStream is)
    throws IOException
    {
        if (null == is) return null;
        
        /*
         * To convert the InputStream to String we use the
         * BufferedReader.readLine() method. We iterate until the BufferedReader
         * return null which means there's no more data to read. Each line will
         * appended to a StringBuilder and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        line = reader.readLine();
        while (line != null) {
            sb.append(line);
            line = reader.readLine();
            if (line != null) sb.append("\n");
        }
        is.close();

        return sb.toString();
    }

    /**
     * Copy a file from one destination to another
     * 
     * @param source
     * @param destination
     * @throws IOException
     */
    public static void copyFile(File source, File destination)
    throws IOException {
        InputStream input = new BufferedInputStream(new FileInputStream(source));
        OutputStream output = new BufferedOutputStream(new FileOutputStream(destination));
        appendToStream(input, output, 0, -1, true);
    }
	
	/**
	 * Streams a file to an {@link OutputStream}
	 * @param output
	 * @param inputFile
	 * @param closeOutput
	 * @throws IOException
	 */
	public static void appendToStream(File inputFile, OutputStream output, boolean closeOutput)
	throws IOException {
		InputStream input = new BufferedInputStream(new FileInputStream(inputFile));
		appendToStream(input, output, closeOutput);
	}
	
	/**
	 * @param inputFile
	 * @param output
	 * @param offset
	 * @param lenght
	 * @param closeOutput
	 */
	public static void appendToStream(
			File inputFile,
			OutputStream output,
			long offset,
			long lenght,
			boolean closeOutput)
	throws IOException {
		InputStream input = new BufferedInputStream(new FileInputStream(inputFile));
		appendToStream(input, output, offset, lenght, closeOutput);
	}

	/**
	 * Streams a string to an {@link OutputStream} with UTF-8 encoding
	 * @param output
	 * @param inputString
	 * @param closeOutput
	 * @throws IOException
	 */
	public static void appendToStream(String inputString, OutputStream output, boolean closeOutput)
	throws IOException {
		InputStream input = new ByteArrayInputStream(inputString.getBytes("UTF-8"));
		appendToStream(input, output, closeOutput);    		
	}

	/**
	 * Copy data from an {@link InputStream} to an {@link OutputStream}
	 * The input is always closed at the end of the process
	 * 
	 * @param input
	 * @param output
	 * @param closeOutput must close the output at the end of the process
	 * @throws IOException
	 */
	public static void appendToStream(InputStream input, OutputStream output, boolean closeOutput)
	throws IOException {
		appendToStream(input, output, 0, -1, closeOutput);
	}

	/**
	 * Copy data from an {@link InputStream} to an {@link OutputStream}
	 * The input is always closed at the end of the process
	 * 
	 * @param input source input stream
	 * @param output destination output stream
	 * @param offset at what byte of the source input the copy must start
	 *        (0 means the begin on the input)
	 * @param lenght how many bytes of the input should be copied to the output
	 *        (-1 for all the entire size of the output)
	 * @param closeOutput must close the output at the end of the process
	 * @throws IOException
	 */
	public static void appendToStream(InputStream input, OutputStream output, long offset, long lenght, boolean closeOutput)
	throws IOException {
        
    	//move to input offset
        if (offset > 0) {
	    	long skipped = input.skip(offset);
	    	//offset is bigger that the inputstream remaining size
	    	if (skipped < offset) {
	    		return;
	    	}
        }

        //copy the entire remaining input to output
        if (-1 == lenght) {
	        int len;
	        
	        //copy the entire input in the output
	    	byte[] buf = new byte[2048];
	        while ((len = input.read(buf)) > 0){
	          output.write(buf, 0, len);
	        }
	        
	    //copy only a portion of the input to the output
        } else {
        	final int bufferSize = 2048;
            int len;
            boolean isAtTheEndOfStream = false;
            
        	long repetition = lenght / bufferSize;
        	int mod = (int) (lenght % bufferSize);
        	
        	byte[] buf = new byte[bufferSize];
        	for (long i = 0; i < repetition; i++) {
    	        if ((len = input.read(buf)) > 0){
    	        	output.write(buf, 0, len);
    	        } else {
    	        	isAtTheEndOfStream = true;
    	        	break;
    	        }
        	}
        	if (!isAtTheEndOfStream) {
	        	buf = new byte[mod];
		        if ((len = input.read(buf)) > 0){
		        	output.write(buf, 0, len);
		        }
        	}
        }
    	
        input.close();
        if (closeOutput) output.close();    		
	}
	
	
    /**
     * Pipes everything from the {@link Reader} to the {@link Writer} via a buffer
     */
    public static void pipe(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            writer.write(buf, 0, read);
        }
        writer.flush();
    }
	

	//---------- Private methods

}
