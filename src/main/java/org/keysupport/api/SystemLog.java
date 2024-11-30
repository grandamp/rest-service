package org.keysupport.api;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * The intent of this class is to redirect System.out and System.err to our
 * logging.
 * 
 * This implementation uses the SUN JSSE provider, which offers the follwing tracing mechanism:
 * 
 * -Djava.security.debug=certpath
 * 
 * Perhaps this will be changed in the future to allow for better incorporation into logs.
 * 
 * I.e.,
 * 
 * - https://bugs.openjdk.org/browse/JDK-8202601
 */
public class SystemLog {

    private static final Logger LOG = LoggerFactory.getLogger(SystemLog.class);

    public static void logSystemOutAndErr() {
        System.setOut(loggingProxy(System.out));
        System.setErr(loggingProxy(System.err));
    }

    public static PrintStream loggingProxy(final PrintStream ps) {
        return new PrintStream(ps) {
            public void print(final String str) {
            	LOG.info(str);
            }
        };
    }
}