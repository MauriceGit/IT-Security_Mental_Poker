package CoinFlip;

import gr.planetz.PingingService;
import gr.planetz.impl.HttpPingingService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

public class Client {

    private boolean isFinished = false;
    private boolean isWon = false;

    private void handleProtocolTLS(ProtocolImpl protocolImpl, TLSNetwork network)
            throws Exception {

        String json;
        int listEntries = 0;

        while (true) {

            List<String> tmpList = network.getAllMessages();

            // Dem Thread eine Chance geben, die Liste aufzufüllen.
            if (tmpList.size() <= listEntries) {
                Thread.sleep(100);
                continue;
            }

            json = network.getAllMessages().get(listEntries);
            listEntries++;

            if (json == null) {
                break;
            }

            if (json.equals("")) {
                break;
            }

            // System.out.println(json);
            Status status = protocolImpl.statusAndRegister(json);
            if (status == Status.PROTOCOL_OK) {
                String res = protocolImpl.calcAndRespondToProtocolStep();
                // System.out.println(res);
                network.send(res);
            } else {
                if (status == Status.PROTOCOL_ERROR) {
                    String trace = protocolImpl.calcStateMessage();
                    // System.out.println("outgoing: '" + trace + "'");
                    network.send(trace);
                }
                break;
            }
        }
        // Dem Server mitteilen, dass er auch aufhören kann!
        network.send("");
        network.stop();

        isFinished = true;
        // System.out.println("is it won??? " + protocolImpl.isiWon());
        isWon = protocolImpl.isiWon();
    }

    /**
     * Spielt keine Rolle mehr, ob man Server oder Client ist!
     * 
     * @param socket
     * @throws Exception
     */
    private void handleProtocolSocket(ProtocolImpl protocolImpl, Socket socket,
            BufferedReader in, PrintWriter out) throws Exception {
        String json;

        while (true) {
            json = in.readLine();

            if (json == null) {
                break;
            }

            if (json.equals("")) {
                continue;
            }

            Status status = protocolImpl.statusAndRegister(json);
            if (status == Status.PROTOCOL_OK) {
                String res = protocolImpl.calcAndRespondToProtocolStep();
                // System.out.println("outgoing: '" + res + "'");
                out.println(res);
            } else {
                if (status == Status.PROTOCOL_ERROR) {
                    String trace = protocolImpl.calcStateMessage();
                    System.out.println("outgoing: '" + trace + "'");
                    out.println(trace);
                }
                break;
            }
        }
        out.close();
        socket.close();
    }

    private Socket createServerSocket() throws IOException {
        ServerSocket server = new ServerSocket(4444);
        return server.accept();
    }

    private Socket createClientSocket() throws UnknownHostException,
            IOException {
        // return new Socket("127.0.0.1", 4444);
        return new Socket("54.77.97.90", 4444);
        // return new Socket("192.168.2.187", 4444);
        // return new Socket("192.168.2.152", 4444);
    }

    private String getSomeServer() {

        try {
            final PingingService service2 = new HttpPingingService(
                    "https://52.35.76.130:8443/broker/1.0/players", "", "",
                    "root", "fhwedel");
            Map<String, String> map = service2
                    .getPlayersDirectlyOverHttpGetRequest();
            Map.Entry<String, String> entry = map.entrySet().iterator().next();
            String key = entry.getKey();
            String value = entry.getValue();

            return value;

        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }

    public void playCoinFlip(boolean isServer, boolean useTLS) {
        ProtocolImpl protocolImpl = new ProtocolImpl();

        if (useTLS) {
            BigInteger serialNumberStartsAt = new BigInteger("5");
            // X509CertGenerator gen = new
            // X509CertGenerator(serialNumberStartsAt);
            try {
                X509CertGenerator gen = new X509CertGenerator(
                        serialNumberStartsAt);
                gen.loadRoot("root", "fhwedel", "root");

            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        /**
         * Genereller Socket, der allgemein für die Kommunikation zuständig ist!
         */
        Socket socket = null;
        TLSNetwork networkC = null;
        TLSNetwork networkS = null;
        PrintWriter out = null;
        BufferedReader in = null;

        //
        // Über den Parameter wird entschieden, wer das Protocoll anfängt und
        // damit den Server spielt!
        //

        try {
            if (isServer) {
                if (useTLS) {
                    networkS = new TLSNetwork(TLSNetwork.SERVER);
                    networkS.start(4444, "root", "fhwedel", "server",
                            "fhwedel", OwnTrustManager.NEVER, null, true);
                } else {
                    socket = createServerSocket();
                }

            } else {
                if (useTLS) {

                    String someServer = getSomeServer();

                    String host = someServer.split(":")[0];
                    Integer port = Integer.parseInt(someServer.split(":")[1]);

                    System.out.println("host = " + host);
                    System.out.println("port = " + port);

                    networkC = new TLSNetwork(TLSNetwork.CLIENT);
                    networkC.start(4444, "root", "fhwedel", "client",
                            "fhwedel", OwnTrustManager.NEVER, null, true);
                    // networkC.connect("fluffels.de",50000,
                    // networkC.connect("54.77.97.90", 4444,
                    // networkC.connect("127.0.0.1", 4444,
                    networkC.connect(host, port, "root", "fhwedel", "client",
                            "fhwedel", OwnTrustManager.NEVER, null, true);
                } else {
                    socket = createClientSocket();
                }
            }

            if (!useTLS) {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
            }

            if (!isServer) {
                String initial = protocolImpl.calcAndRespondToProtocolStep();
                // System.out.println("outgoing: '" + initial + "'");
                if (!useTLS) {
                    out.println(initial);
                } else {
                    System.out.println("initial: " + initial);
                    networkC.send(initial);
                }

            }

            if (!useTLS) {
                handleProtocolSocket(protocolImpl, socket, in, out);
            } else {
                handleProtocolTLS(protocolImpl, isServer ? networkS : networkC);
            }

        } catch (Exception e) {
            System.out.println("Some serious shit is going on ...");
            System.out.println(e);
            isFinished = true;
        }
        isFinished = true;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isWon() {
        return isWon;
    }

    public void main(String[] args) {

        boolean isServer = false;
        if (args.length != 0 && args[0].matches("^START$")) {
            isServer = true;
        }

        playCoinFlip(isServer, true);

    }

}
