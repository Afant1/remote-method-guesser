package de.qtc.rmg.networking;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocketFactory;

import de.qtc.rmg.io.Logger;

/**
 * Remote objects bound to an RMI registry are usually pointing to remote endpoints
 * on the same host. In order to protect from unauthorized access, some developers
 * suggest setting these references to localhost or 127.0.0.1 explicitly. This will
 * indeed cause hickups in most RMI clients, as they try to call to 127.0.0.1 after
 * fetching a remote object. However, when the TCP ports of the corresponding remote
 * objects are open, it is still possible to communicate with them.
 *
 * The LoopbackSslSocketFactory class extends the default SSLSocketFactory and can be set
 * as a replacement. The class uses static variables to define configuration parameters and
 * the actual target of the RMI communication (usually the registry host). All other RMI
 * connections are then expected to target the same host. This is implemented by overwriting
 * the createSocket function. If the specified host value does not match the expected value,
 * it is replaced by the expected value and the connection is therefore redirected.
 *
 * During a redirect, the class prints a warning to the user to inform about the
 * redirection. If redirection is a desired behavior, the user can use the --follow option
 * with rmg, which sets the followRedirect attribute to true. In these cases, a warning
 * is still printed, but the connection goes to the specified target.
 *
 * @author Tobias Neitzel (@qtc_de)
 */
public class LoopbackSslSocketFactory extends SSLSocketFactory {

    public static String host = "";
    public static SSLSocketFactory fac = null;
    public static boolean printInfo = true;
    public static boolean followRedirect = false;

    /**
     * Overwrites the default implementation of createSocket. Checks whether host matches the expected
     * value and changes the value if required. After the host check was done, the default socket factory
     * is used to create the real socket.
     */
    @Override
    public Socket createSocket(String target, int port) throws IOException {
        if(!host.equals(target)) {
            printInfos("RMI object tries to connect to different remote host: " + target);

            if( followRedirect ) {
                printInfos("\tFollowing ssl connection to new target... ");
            } else {
                printInfos("\tRedirecting the ssl connection back to " + host + "... ");
                target = host;
            }
            printInfos("\tThis is done for all further requests. This message is not shown again. ");
            printInfo = false;
        }
        return fac.createSocket(target, port);
    }

    @Override
    public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3) throws IOException {
        return fac.createSocket(arg0, arg1, arg2, arg3);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return fac.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return fac.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
        return fac.createSocket(arg0, arg1);
    }

    @Override
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException {
        return fac.createSocket(arg0, arg1, arg2, arg3);
    }

    @Override
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException {
        return fac.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * Especially during guessing, the number of warnings can go out of control. Therefore, redirection warnings
     * are only printed once. This helper function checks whether a warning was already printed and only prints
     * a new warning if this was not the case.
     *
     * @param info user information about redirects
     */
    private void printInfos(String info) {
        if( printInfo )
            Logger.eprintlnBlue(info);
    }
}
