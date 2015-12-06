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
	 * 
	 * @param socket
	 * @throws Exception
	 */
	private static void handleProtocol(ProtocolImpl protocolImpl,
			Socket socket, BufferedReader in, PrintWriter out) throws Exception {
		String json;
		System.out.println(socket.isClosed() + ", " + socket.isConnected()
				+ ", " + socket.isInputShutdown() + ", " + in.ready());

		while (true) {
			json = in.readLine();
			if (json == "" || json == null) {
				break;
			}

			if (protocolImpl.statusAndRegister(json) == Status.PROTOCOL_OK) {
				String res = protocolImpl.calcAndRespondToProtocolStep();
				System.out.println(res);
				out.println(res);
			} else {
				break;
			}
		}
		out.close();
		socket.close();
	}

	private static Socket createServerSocket() throws IOException {
		ServerSocket server = new ServerSocket(4444);
		return server.accept();
	}

	private static Socket createClientSocket() throws UnknownHostException,
			IOException {
		return new Socket("127.0.0.1", 4444);
	    //return new Socket("54.77.97.90", 4444);
		// return new Socket("192.168.2.187", 4444);
		// return new Socket("192.168.2.152", 4444);
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
		// Über den Parameter wird entschieden, wer das Protocoll anfängt und
		// damit den Server spielt!
		//
		try {
			if (args.length != 0 && args[0].matches("^START$")) {
				socket = createServerSocket();
				isServer = true;
			} else {
				socket = createClientSocket();
			}

			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			if (!isServer) {
				String initial = protocolImpl.calcAndRespondToProtocolStep();
				System.out.println(initial);
				out.println(initial);
			}

			handleProtocol(protocolImpl, socket, in, out);

		} catch (Exception e) {
			System.out.println("Some serious shit is going on ...");
			System.out.println(e);
		}

	}

}
