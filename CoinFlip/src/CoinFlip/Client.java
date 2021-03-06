package CoinFlip;

import gr.planetz.PingingService;
import gr.planetz.impl.HttpPingingService;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

    private Socket createClientSocket(String serverIP, Integer serverPort) throws UnknownHostException,
            IOException {
        return new Socket(serverIP, serverPort);
    }

    private String getSomeServer(String brokerPath, String certName,
            String certPw) {

        try {
            final PingingService service2 = new HttpPingingService(brokerPath,
                    "", "", certName, certPw);
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

    public void playCoinFlip() {

        Properties prop = new Properties();
        InputStream input = null;
        boolean isServer = false;
        boolean useTLS = true;
        boolean useBroker = true;
        String brokerURL = "";
        String serverIP = "";
        Integer serverPort = 0;
        String rootCertificateFile = "";
        String rootCertificatePw = "";
        String rootCertificateAlias = "";
        String clientCertificateFile = "";
        String clientCertificatePw = "";
        String clientCertificateAlias = "";
        String serverCertificateFile = "";
        String serverCertificatePw = "";
        String serverCertificateAlias = "";
        String serialNumberStartsAtIn = "5";

        try {

            input = new FileInputStream("coinflip_config.conf");

            // load a properties file
            prop.load(input);

            isServer = Boolean.parseBoolean(prop.getProperty("isServer"));
            useTLS = Boolean.parseBoolean(prop.getProperty("useTLS"));
            useBroker = Boolean.parseBoolean(prop.getProperty("useBroker"));
            brokerURL = prop.getProperty("brokerURL");
            serverIP = prop.getProperty("serverIP");
            serverPort = Integer.parseInt(prop.getProperty("serverPort"));
            rootCertificateFile = prop.getProperty("rootCertificateFile");
            rootCertificatePw = prop.getProperty("rootCertificatePw");
            rootCertificateAlias = prop.getProperty("rootCertificateAlias");
            clientCertificateFile = prop.getProperty("clientCertificateFile");
            clientCertificatePw = prop.getProperty("clientCertificatePw");
            clientCertificateAlias = prop.getProperty("clientCertificateAlias");
            serverCertificateFile = prop.getProperty("serverCertificateFile");
            serverCertificatePw = prop.getProperty("serverCertificatePw");
            serverCertificateAlias = prop.getProperty("serverCertificateAlias");
            serialNumberStartsAtIn = prop.getProperty("serialNumberStartsAt");

        } catch (IOException ex) {
            System.out
                    .println("There went something wrong when reading the config file:");
            System.out.println(ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }

        ProtocolImpl protocolImpl = new ProtocolImpl();

        if (useTLS) {
            BigInteger serialNumberStartsAt = new BigInteger(
                    serialNumberStartsAtIn);
            // X509CertGenerator gen = new
            // X509CertGenerator(serialNumberStartsAt);
            try {
                X509CertGenerator gen = new X509CertGenerator(
                        serialNumberStartsAt);
                gen.loadRoot(rootCertificateFile, rootCertificatePw,
                        rootCertificateAlias);

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

        try {
            if (isServer) {
                if (useTLS) {
                    networkS = new TLSNetwork(TLSNetwork.SERVER);
                    networkS.start(serverPort, serverCertificateFile,
                            serverCertificatePw, serverCertificateAlias,
                            serverCertificatePw, OwnTrustManager.NEVER, null,
                            true);
                } else {
                    socket = createServerSocket();
                }

            } else {
                if (useTLS) {
                    if (useBroker) {
                        String someServer = getSomeServer(brokerURL, rootCertificateFile, rootCertificatePw);
                        serverIP = someServer.split(":")[0];
                        serverPort = Integer.parseInt(someServer.split(":")[1]);
                    }

                    networkC = new TLSNetwork(TLSNetwork.CLIENT);
                    networkC.start(serverPort, clientCertificateFile,
                            clientCertificatePw, clientCertificateAlias,
                            clientCertificatePw, OwnTrustManager.NEVER, null,
                            true);
                    networkC.connect(serverIP, serverPort,
                            clientCertificateFile, clientCertificatePw,
                            clientCertificateAlias, clientCertificatePw,
                            OwnTrustManager.NEVER, null, true);
                } else {
                    socket = createClientSocket(serverIP, serverPort);
                }
            }

            if (!useTLS) {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
            }

            if (!isServer) {
                String initial = protocolImpl.calcAndRespondToProtocolStep();
                if (!useTLS) {
                    out.println(initial);
                } else {
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
        playCoinFlip();
    }

}
