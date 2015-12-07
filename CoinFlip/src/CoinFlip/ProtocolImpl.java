package CoinFlip;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jcajce.provider.asymmetric.sra.SRADecryptionKeySpec;
import org.bouncycastle.jcajce.provider.asymmetric.sra.SRAEncryptionKeySpec;
import org.bouncycastle.jcajce.provider.asymmetric.sra.SRAKeyGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class ProtocolImpl {

	private ObjectMapper mapper;
	private Protocol protocol;

	// mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
	// mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);

	// first get the sra key pair generator instance.
	private KeyPairGenerator generator;
	private KeyPair keyPair;

	public ProtocolImpl() {
		protocol = new Protocol();
		mapper = new ObjectMapper();// .configure(Feature.AUTO_CLOSE_SOURCE,
									// false)
		// .configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		// mapper.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

		Security.addProvider(new BouncyCastleProvider());
		try {
			generator = KeyPairGenerator.getInstance("SRA",
					BouncyCastleProvider.PROVIDER_NAME);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean validateGeneralAttributes(Protocol protocol, Protocol before) {
		return protocol.getProtocolId() == before.getProtocolId() + 1
				&& protocol.getStatusId() == 0 && before.getStatusId() == 0;
	}

	private boolean validateNewProtocolNegotiation(Protocol protocol) {
		return protocol.getProtocolNegotiation().getAvailableVersions().get(0)
				.getVersions().size() > 0;
	}

	/**
	 * Ob die wieder geschickten Daten unverändert geblieben sind!!!
	 * 
	 * @param protocol
	 * @param before
	 * @return
	 */
	private boolean validateOldProtocolNegotiation(Protocol protocol,
			Protocol before) {
		// Ist die Länge der beiden Listen identisch?
		boolean res = protocol.getProtocolNegotiation().getAvailableVersions()
				.size() == before.getProtocolNegotiation()
				.getAvailableVersions().size()
				&& protocol.getProtocolNegotiation().getAvailableVersions()
						.get(0).getVersions().size() == before
						.getProtocolNegotiation().getAvailableVersions().get(0)
						.getVersions().size();
		// Version gleich?
		res = res
				&& protocol.getProtocolNegotiation().getVersion()
						.equals(before.getProtocolNegotiation().getVersion());
		// Steht jeweils das Gleiche drin?
		for (int i = 0; i < protocol.getProtocolNegotiation()
				.getAvailableVersions().size(); i++) {
			Protocol.Version v1 = protocol.getProtocolNegotiation()
					.getAvailableVersions().get(i);
			Protocol.Version v2 = before.getProtocolNegotiation()
					.getAvailableVersions().get(i);
			for (int j = 0; j < v1.getVersions().size(); j++) {
				String vs1 = v1.getVersions().get(j);
				String vs2 = v2.getVersions().get(j);
				res = res && vs1.equals(vs2);
			}
		}
		return res;
	}

	private boolean validateOldKeyNegotiation(Protocol protocol, Protocol before) {
		boolean res = protocol.getKeyNegotiation().getAvailableSids().size() == before
				.getKeyNegotiation().getAvailableSids().size()
				&& protocol.getKeyNegotiation().getP()
						.equals(before.getKeyNegotiation().getP())
				&& protocol.getKeyNegotiation().getQ()
						.equals(before.getKeyNegotiation().getQ())
				&& protocol.getKeyNegotiation().getSid() == before
						.getKeyNegotiation().getSid();
		for (int i = 0; i < protocol.getKeyNegotiation().getAvailableSids()
				.size(); i++) {
			LinkedList<Protocol.Sids> s1 = protocol.getKeyNegotiation()
					.getAvailableSids();
			LinkedList<Protocol.Sids> s2 = before.getKeyNegotiation()
					.getAvailableSids();

			for (int j = 0; j < s1.get(i).getSids().size(); j++) {
				res = res
						&& s1.get(i).getSids().get(j) == s2.get(i).getSids()
								.get(j);
			}
		}

		return res;
	}

	private boolean validateNewKeyNegotiation(Protocol protocol) {

		for (int s : protocol.getKeyNegotiation().getAvailableSids().get(0)
				.getSids()) {
			if (s == 20) {
				return true;
			}
		}

		return false;
	}

	private boolean validateNewKeyNegotiationLong(Protocol protocol) {
		boolean res = !protocol.getKeyNegotiation().getP()
				.equals(BigInteger.ZERO)
				&& !protocol.getKeyNegotiation().getQ().equals(BigInteger.ZERO)
				&& protocol.getKeyNegotiation().getSid() == 20;

		try {
			// create specifications for the key generation.
			SRAKeyGenParameterSpec specs = new SRAKeyGenParameterSpec(2048,
					protocol.getKeyNegotiation().getP(), protocol
							.getKeyNegotiation().getQ());

			this.generator = KeyPairGenerator.getInstance("SRA",
					BouncyCastleProvider.PROVIDER_NAME);

			generator.initialize(specs);

			// generate a valid SRA key pair for the given p and q.
			this.keyPair = generator.generateKeyPair();
		} catch (Exception e) {
			System.out.println("Oh shit. Something is totally blown.");
			System.out.println(e);
		}

		return res;
	}

	private boolean validateOldPayloadFirst(Protocol protocol, Protocol before) {
		return protocol.getPayload().getInitialCoin().get(0)
				.equals(before.getPayload().getInitialCoin().get(0))
				&& protocol.getPayload().getInitialCoin().get(1)
						.equals(before.getPayload().getInitialCoin().get(1))
				&& protocol.getPayload().getEncryptedCoin().get(0)
						.equals(before.getPayload().getEncryptedCoin().get(0))
				&& protocol.getPayload().getEncryptedCoin().get(1)
						.equals(before.getPayload().getEncryptedCoin().get(1));
	}

	private boolean validateOldPayloadSecond(Protocol protocol, Protocol before) {
		return protocol.getPayload().getDesiredCoin()
				.equals(before.getPayload().getDesiredCoin())
				&& protocol.getPayload().getEnChosenCoin()
						.equals(before.getPayload().getEnChosenCoin());
	}

	private boolean validateOldPayloadThird(Protocol protocol, Protocol before) {
		return protocol.getPayload().getDeChosenCoin()
				.equals(before.getPayload().getDeChosenCoin())
				&& protocol.getPayload().getKeyA().get(0)
						.equals(before.getPayload().getKeyA().get(0))
				&& protocol.getPayload().getKeyA().get(1)
						.equals(before.getPayload().getKeyA().get(1));
	}

	/**
	 * Schritt 4.
	 * 
	 * @param protocol
	 * @return
	 */
	private boolean validateNewPayloadFirst(Protocol protocol) {
		return protocol.getPayload().getInitialCoin().size() == 2
				&& protocol.getPayload().getEncryptedCoin().size() == 2
				&& !protocol.getPayload().getEncryptedCoin().get(0).equals("")
				&& !protocol.getPayload().getEncryptedCoin().get(1).equals("");
	}

	private boolean validateNewPayloadSecond(Protocol protocol) {
		return (protocol.getPayload().getDesiredCoin()
				.equals(protocol.getPayload().getInitialCoin().get(0)) || protocol
				.getPayload().getDesiredCoin()
				.equals(protocol.getPayload().getInitialCoin().get(1)))
				&& !protocol.getPayload().getEnChosenCoin().equals("");
	}

	private boolean validateNewPayloadThird(Protocol protocol) {
		return !protocol.getPayload().getDeChosenCoin().equals("")
				&& !protocol.getPayload().getKeyA().get(0)
						.equals(BigInteger.ZERO)
				&& !protocol.getPayload().getKeyA().get(1)
						.equals(BigInteger.ZERO);
	}

	private boolean validateNewPayloadForth(Protocol protocol) {
		boolean res = !protocol.getPayload().getKeyB().get(0)
				.equals(BigInteger.ZERO)
				&& !protocol.getPayload().getKeyB().get(1)
						.equals(BigInteger.ZERO);
		try {
			Cipher engine = Cipher.getInstance(
					"SRA/NONE/OAEPWITHSHA512ANDMGF1PADDING",
					BouncyCastleProvider.PROVIDER_NAME);
			KeyFactory factory = KeyFactory.getInstance("SRA",
					BouncyCastleProvider.PROVIDER_NAME);

			BigInteger p = protocol.getKeyNegotiation().getP();
			BigInteger q = protocol.getKeyNegotiation().getQ();
			BigInteger n = p.multiply(q);
			BigInteger e = protocol.getPayload().getKeyB().get(0);
			BigInteger d = protocol.getPayload().getKeyB().get(1);

			PrivateKey privateKey = factory
					.generatePrivate(new SRADecryptionKeySpec(p, q, d, e));
			PublicKey publicKey = factory
					.generatePublic(new SRAEncryptionKeySpec(n, e));

			KeyPair newKeyPair = new KeyPair(publicKey, privateKey);

			// prepare for decryption
			engine.init(Cipher.DECRYPT_MODE, newKeyPair.getPrivate());
			// decrypt the cipher.
			byte[] recover = engine.doFinal(DatatypeConverter
					.parseHexBinary(protocol.getPayload().getDeChosenCoin()));
			String result = Hex.toHexString(recover);

			System.out.println("Coin flip was:" + convertHexToString(result));
			String winningState = "lost";

			if (!protocol.getPayload().getDesiredCoin()
					.equals(convertHexToString(result))) {
				winningState = "won";
			}
			System.out.println("And Koko chose: "
					+ protocol.getPayload().getDesiredCoin());

			System.out.println("That means, that I " + winningState
					+ "!!!!!!!!!!!!11elf");

		} catch (Exception e) {
			System.out.println("Oh no, people are so mean");
			System.out.println(e);
			return false;
		}

		return res;
	}

	/**
	 * Sinnvolle Version ausgewählt?
	 * 
	 * @param protocol
	 * @return
	 */
	private boolean validateChosenVersion(Protocol protocol) {
		return !protocol.getProtocolNegotiation().getVersion().equals("");
	}

	private void probablyFunnyMessage(Protocol protocol) {
		if (false && protocol.getStatusMessage() != "OK") {
			System.out.println("The other guy says: "
					+ protocol.getStatusMessage() + ".");
		}
	}

	private boolean iCanHandleTheChosenVersion(Protocol protocol) {
		return protocol.getProtocolNegotiation().getVersion().equals("1.0");
	}

	/**
	 * Verifiziert einen Protokoll-Schritt!
	 * 
	 * @param newValues
	 * @return
	 */
	private boolean validateProtocolStep(Protocol protocol, Protocol before) {
		boolean everythingOK = true;

		try {
			int protocolStep = protocol.getProtocolId();

			// Allgemeine Tests, die jeden Durchgang erneut geprüft werden
			// müssen!!!
			switch (protocolStep) {
			case 7:
				everythingOK = everythingOK
						&& validateOldPayloadThird(protocol, before);
			case 6:
				everythingOK = everythingOK
						&& validateOldPayloadSecond(protocol, before);
			case 5:
				everythingOK = everythingOK
						&& validateOldPayloadFirst(protocol, before);
			case 4:
				everythingOK = everythingOK
						&& validateOldKeyNegotiation(protocol, before);
			case 3:
			case 2:
				everythingOK = everythingOK
						&& validateOldProtocolNegotiation(protocol, before);
			case 1:
			case 0:
				everythingOK = everythingOK
						&& validateGeneralAttributes(protocol, before);
			}

			// Tests, die nur in dem jeweiligen Schritt einmal getestet werden
			// müssen!!!
			switch (protocolStep) {
			case 7:
				everythingOK = everythingOK
						&& validateNewPayloadForth(protocol);
				break;
			case 6:
				everythingOK = everythingOK
						&& validateNewPayloadThird(protocol);
				break;
			case 5:
				everythingOK = everythingOK
						&& validateNewPayloadSecond(protocol);
				break;
			case 4:
				everythingOK = everythingOK
						&& validateNewPayloadFirst(protocol);
				break;
			case 3:
				// fertig!
				everythingOK = everythingOK
						&& validateNewKeyNegotiationLong(protocol);
				break;
			case 2:
				everythingOK = everythingOK
						&& validateNewKeyNegotiation(protocol);
				break;
			case 1:
				everythingOK = everythingOK && validateChosenVersion(protocol)
						&& validateNewProtocolNegotiation(protocol);
				everythingOK = everythingOK
						&& iCanHandleTheChosenVersion(protocol);
				break;
			case 0:
				everythingOK = everythingOK
						&& validateNewProtocolNegotiation(protocol);
			}

			probablyFunnyMessage(protocol);
		} catch (Exception e) {
			System.out
					.println("Something went wrong and came out with this error:");
			System.out.println(e);
			protocol.setStatusId(100);
			protocol.setStatusMessage(e.getStackTrace().toString());
			return false;
		}

		return everythingOK;
	}

	private boolean protocolFinished(Protocol protocol) {

		return protocol.getProtocolId() >= 7;

	}

	private void addGeneralData() {
		protocol.setStatusId(0);
		protocol.setStatusMessage("Hey, s'up man.");
	}

	private void addProtocolNegotiationDataFirst() {
		Protocol.Version v = new Protocol.Version();
		v.getVersions().add("1.0");
		protocol.getProtocolNegotiation().getAvailableVersions().add(v);
	}

	/**
	 * Ich machs mir einfach. Ich schreib einfach meine Protokoll-Version da
	 * rein. Wenn der andere die nicht kann, soll er selber Fehler werfen!
	 */
	private void addProtocolNegotiationDataSecond() {
		Protocol.Version v = new Protocol.Version();
		v.getVersions().add("1.0");
		protocol.getProtocolNegotiation().getAvailableVersions().add(v);
		// Später hier ansetzen zur Korrektur!
		// TODO: Auf Gleichheit prüfen von dem Anderen und eine gleiche Version
		// auswählen!
		protocol.getProtocolNegotiation().setVersion("1.0");
	}

	private void addKeyNegotiationFirst() {
		Protocol.Sids sids = new Protocol.Sids();
		sids.getSids().add(20);
		protocol.getKeyNegotiation().getAvailableSids().add(sids);
	}

	/**
	 * Ich machs mir einfach. Ich schreib einfach meine SID da rein. Wenn der
	 * andere die nicht kann, soll er selber Fehler werfen!
	 */
	private void addKeyNegotiationSecond() {

		// provide a bit-size for the key (1024-bit key in this example).
		generator.initialize(2048);
		// generate the key pair.
		keyPair = generator.generateKeyPair();

		// get a key factory instance for SRA
		KeyFactory factory = null;
		try {
			factory = KeyFactory.getInstance("SRA",
					BouncyCastleProvider.PROVIDER_NAME);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// extract p and q. you have to use the private key for this, since only
		// the private key contains the information.
		// the key factory fetches the hidden information out of the private key
		// and fills a SRADecryptionKeySpec to provide the information.
		SRADecryptionKeySpec spec = null;
		try {
			spec = factory.getKeySpec(keyPair.getPrivate(),
					SRADecryptionKeySpec.class);
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		protocol.getKeyNegotiation().setP(
				new BigInteger(spec.getP().toByteArray()));
		protocol.getKeyNegotiation().setQ(
				new BigInteger(spec.getQ().toByteArray()));
		protocol.getKeyNegotiation().setSid(20);

	}

	private void addPayloadFirst() {
		protocol.getPayload().getInitialCoin().add("HEAD");
		protocol.getPayload().getInitialCoin().add("TAIL");

		LinkedList<String> ec = new LinkedList<String>();

		try {
			Cipher engine = Cipher.getInstance(
					"SRA/NONE/OAEPWITHSHA512ANDMGF1PADDING",
					BouncyCastleProvider.PROVIDER_NAME);

			// prepare the engine for encryption.
			engine.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());

			// encrypt something.
			byte[] encryptedHead = engine.doFinal(protocol.getPayload()
					.getInitialCoin().get(0).getBytes());
			byte[] encryptedTail = engine.doFinal(protocol.getPayload()
					.getInitialCoin().get(1).getBytes());

			ec.add(Hex.toHexString(encryptedHead));
			ec.add(Hex.toHexString(encryptedTail));

			Collections.shuffle(ec);

			protocol.getPayload().setEncryptedCoin(ec);

		} catch (Exception e) {
			System.out.println("Oh shit. The encryption is totally blown.");
			System.out.println(e);
		}
	}

	private void addPayloadSecond() {
		protocol.getPayload().setDesiredCoin("TAIL");

		try {
			Cipher engine = Cipher.getInstance("SRA",
					BouncyCastleProvider.PROVIDER_NAME);

			// prepare the engine for encryption.
			engine.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
			// encrypt something.
			Random rand = new Random();
			byte[] enChosenCoin = engine.doFinal(DatatypeConverter
					.parseHexBinary(protocol.getPayload().getEncryptedCoin()
							.get(rand.nextInt(2))));
			// System.out.println("set EnChosenCoin: " + new
			// String(enChosenCoin, "UTF-8"));
			protocol.getPayload()
					.setEnChosenCoin(Hex.toHexString(enChosenCoin));
			// System.out.println("set EnChosenCoin: " + new
			// String(enChosenCoin, "UTF-8"));

		} catch (Exception e) {
			System.out.println("Oh shit. The encryption is totally blown.");
			System.out.println(e);
		}

	}

	private void addPayloadThird() {
		// TODO: Tatsächlich Dinge verschlüsseln...
		LinkedList<BigInteger> keyA = new LinkedList<BigInteger>();

		try {
			Cipher engine = Cipher.getInstance("SRA",
					BouncyCastleProvider.PROVIDER_NAME);
			// prepare for decryption
			engine.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
			// decrypt the cipher.
			byte[] recover = engine.doFinal(DatatypeConverter
					.parseHexBinary(protocol.getPayload().getEnChosenCoin()));
			protocol.getPayload().setDeChosenCoin(Hex.toHexString(recover));
			keyA.add(((RSAPublicKey) keyPair.getPublic()).getPublicExponent());
			keyA.add(((RSAPrivateKey) keyPair.getPrivate())
					.getPrivateExponent());
			protocol.getPayload().setKeyA(keyA);

		} catch (Exception e) {
			System.out
					.println("Oh shit. The decryption is totally blown. honestly.");
			System.out.println(e);
		}

	}

	public String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);

			temp.append(decimal);
		}

		return sb.toString();
	}

	private void addPayloadForth() {

		LinkedList<BigInteger> keyB = new LinkedList<BigInteger>();

		try {

			Cipher engine = Cipher.getInstance(
					"SRA/NONE/OAEPWITHSHA512ANDMGF1PADDING",
					BouncyCastleProvider.PROVIDER_NAME);
			KeyFactory factory = KeyFactory.getInstance("SRA",
					BouncyCastleProvider.PROVIDER_NAME);

			BigInteger p = protocol.getKeyNegotiation().getP();
			BigInteger q = protocol.getKeyNegotiation().getQ();
			BigInteger n = p.multiply(q);
			BigInteger e = protocol.getPayload().getKeyA().get(0);
			BigInteger d = protocol.getPayload().getKeyA().get(1);

			PrivateKey privateKey = factory
					.generatePrivate(new SRADecryptionKeySpec(p, q, d, e));
			PublicKey publicKey = factory
					.generatePublic(new SRAEncryptionKeySpec(n, e));

			KeyPair newKeyPair = new KeyPair(publicKey, privateKey);

			// prepare for decryption
			engine.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
			// decrypt the cipher.
			byte[] recover = engine.doFinal(DatatypeConverter
					.parseHexBinary(protocol.getPayload().getDeChosenCoin()));

			String result = Hex.toHexString(recover);

			System.out.println("Coin flip was:" + convertHexToString(result));

			System.out.println("And you chose: "
					+ protocol.getPayload().getDesiredCoin());

			keyB.add(((RSAPublicKey) keyPair.getPublic()).getPublicExponent());
			keyB.add(((RSAPrivateKey) keyPair.getPrivate())
					.getPrivateExponent());
			protocol.getPayload().setKeyB(keyB);
			protocol.getPayload().setSignatureA("BigAsSignature");
		} catch (Exception e) {
			System.out.println("Oh shit. The decryption is totally blown.");
			System.out.println(e);
		}
	}

	/**
	 * Geht davon aus, dass Konsistenz korrekt ist und der Wert in protocol der
	 * aktuellen Situation entspricht! Berechnet dann den neuen Wert!
	 * 
	 * @return Neues Json.
	 * @throws JsonProcessingException
	 */
	public String calcAndRespondToProtocolStep() throws JsonProcessingException {
		/**
		 * Hier Berechnungen durchführen und im protocol rumschreiben :)
		 */

		int protocolStep = protocol.getProtocolId() + 1;
		Set<String> dontFilter = new HashSet<String>();

		protocol.setProtocolId(protocolStep);

		switch (protocolStep) {
		case 7:
			// fertig!
			addPayloadForth();
			break;
		case 6:
			// fertig!
			addPayloadThird();
			break;
		case 5:
			// fertig!
			addPayloadSecond();
			break;
		case 4:
			// fertig!
			addPayloadFirst();
			break;
		case 3:
			// fertig!
			addKeyNegotiationSecond();
			break;
		case 2:
			// fertig!
			addKeyNegotiationFirst();
			break;
		case 1:
			// fertig!
			addProtocolNegotiationDataSecond();
			break;
		case 0:
			// fertig!
			addGeneralData();
			addProtocolNegotiationDataFirst();
		}
		//System.out.println("Step " + protocolStep + " finished.");

		switch (protocolStep) {
		case 7:
		case 6:
		case 5:
		case 4:
		case 3:
		case 2:
			dontFilter.add("keyNegotiation");
			dontFilter.add("payload");
		case 1:
		case 0:
			dontFilter.add("protocolId");
			dontFilter.add("statusId");
			dontFilter.add("statusMessage");
			dontFilter.add("protocolNegotiation");
		}

		FilterProvider filters = new SimpleFilterProvider().addFilter(
				"myFilter",
				SimpleBeanPropertyFilter.filterOutAllExcept(dontFilter));

		ObjectWriter writer = mapper.writer(filters);

		return mapper.writer(filters).writeValueAsString(protocol);

		// return mapper.writeValueAsString(protocol);
	}

	public String calcStateMessage() {
		Set<String> dontFilter = new HashSet<String>();
		dontFilter.add("protocolId");
		dontFilter.add("statusId");
		dontFilter.add("statusMessage");
		FilterProvider filters = new SimpleFilterProvider().addFilter(
				"myFilter",
				SimpleBeanPropertyFilter.filterOutAllExcept(dontFilter));

		ObjectWriter writer = mapper.writer(filters);

		try {
			return mapper.writer(filters).writeValueAsString(protocol);
		} catch (JsonProcessingException e) {
			e.getStackTrace().toString();
			return "";
		}
	}

	/**
	 * Speichert das Json ab und überprüft Konsistenz zu alten Zuständen!
	 * 
	 * @param json
	 *            der empfangene Json-String.
	 * @return der Status, ob alles in Ordnung und konsistent ist.
	 */
	public Status statusAndRegister(String json) {

		Protocol newProtocol = new Protocol();
		try {
			newProtocol = (Protocol) mapper.readValue(json, Protocol.class);
		} catch (Exception e) {
			System.out.println("\n==================================");
			System.out.println("json: '" + json + "'");
			System.out.println("==================================\n");
			return Status.PROTOCOL_ERROR;
		}

		if (!validateProtocolStep(newProtocol, protocol)) {
			System.out.println("What the hell...");
			return Status.PROTOCOL_ERROR;

		}

		protocol = newProtocol;

		if (protocolFinished(newProtocol)) {
			System.out.println("finished");
			return Status.PROTOTOCOL_FINISHED;
		}

		return Status.PROTOCOL_OK;
	}
}
