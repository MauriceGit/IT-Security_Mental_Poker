package CoinFlip;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    /**
     * Spielt keine Rolle mehr, ob man Server oder Client ist!
     * @param socket
     * @throws Exception 
     */
    private static void handleProtocol(ProtocolImpl protocolImpl, Socket socket, BufferedReader in, PrintWriter out) throws Exception {
        String json;
        while (true) {
            json = in.readLine();
            
            if (json == "" || json == null) {
                break;
            }
            
            if (protocolImpl.statusAndRegister(json) == Status.PROTOCOL_OK) {
                out.println(protocolImpl.calcAndRespondToProtocolStep());
            } else {
                System.out.println("Break?");
                break;
            }
        }
        out.close();
        socket.close();
    }
    
    
    private static Socket createServerSocket() throws IOException {
        ServerSocket server = new ServerSocket(12345);
        return server.accept();
    }
    
    private static Socket createClientSocket() throws UnknownHostException, IOException {
        return new Socket("127.0.0.1", 12345);
    }
    
    
    public static void main(String[] args) {
        
        ProtocolImpl protocolImpl = new ProtocolImpl();
        
        
        /**
         * Genereller Socket, der allgemein für die Kommunikation zuständig ist!
         */
        Socket socket = new Socket();
        PrintWriter out = null;
        BufferedReader in = null;
        boolean isServer = false;
        
        //
        // Über den Parameter wird entschieden, wer das Protocoll anfängt und damit den Server spielt!
        //
        try {
            if (args.length != 0 && args[0].matches("^START$")) {
                socket = createServerSocket();
                isServer = true;
            } else {
                socket = createClientSocket();
            }
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            if (isServer) {
                String initial = protocolImpl.calcAndRespondToProtocolStep();
                out.println(initial);
            }
            
            handleProtocol(protocolImpl, socket, in, out);
        
        } catch (Exception e) {
            System.out.println("Some serious shit is going on ...");
            System.out.println(e);
        }
        
    }


}
