package net.nordu.mdx.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

        private static final int BUFSZ = 8196;
        
        public static void copyStream(OutputStream out, InputStream in, int bufsz)
                throws IOException
        {
                byte[] buf = new byte[bufsz];
                int n = 0;
                while ( (n = in.read(buf)) > 0 )
                {
                        out.write(buf,0,n);
                }
        }
        
        public static void copyStream(OutputStream out, InputStream in)
                throws IOException
        {
                copyStream(out, in, BUFSZ);
        }	        
	
}
