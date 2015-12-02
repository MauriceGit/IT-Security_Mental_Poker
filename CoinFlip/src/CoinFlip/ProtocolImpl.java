package CoinFlip;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.LinkedList;

import javax.crypto.Cipher;

import org.bouncycastle.jcajce.provider.asymmetric.sra.SRADecryptionKeySpec;
import org.bouncycastle.jcajce.provider.asymmetric.sra.SRAKeyGenParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtocolImpl {

    private ObjectMapper mapper;
    private Protocol protocol;

    // first get the sra key pair generator instance.
    private KeyPairGenerator generator;
    private KeyPair keyPair;

    
    public ProtocolImpl() {
        protocol = new Protocol();
        mapper = new ObjectMapper();
        
        Security.addProvider(new BouncyCastleProvider());
        try {
            generator = KeyPairGenerator.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void p(String s) {
        //System.out.println(s);
    }

    private boolean validateGeneralAttributes(Protocol protocol, Protocol before) {
        return protocol.getProtocolId() == before.getProtocolId() + 1
                && protocol.getStatusId() == 0 && before.getStatusId() == 0;
    }

    private boolean validateNewProtocolNegotiation(Protocol protocol) {    
        return protocol.getProtocolNegotiation().getAvailableVersions().get(0).getVersions().size() > 0;
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
        System.out.println("1 - " + res);
        // Version gleich?
        res = res
                && protocol.getProtocolNegotiation().getVersion().equals(before
                        .getProtocolNegotiation().getVersion());
        System.out.println("2 - " + res);
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
                System.out.println("3 - " + i + " - " + res);
            }
        }
        return res;
    }

    private boolean validateOldKeyNegotiation(Protocol protocol, Protocol before) {
        boolean res = protocol.getKeyNegotiation().getAvailableSids().size() == before
                .getKeyNegotiation().getAvailableSids().size()
                && protocol.getKeyNegotiation().getP().equals(before
                        .getKeyNegotiation().getP())
                && protocol.getKeyNegotiation().getQ().equals(before
                        .getKeyNegotiation().getQ())
                && protocol.getKeyNegotiation().getSid() == before
                        .getKeyNegotiation().getSid();
        for (int i = 0; i < protocol.getKeyNegotiation().getAvailableSids()
                .size(); i++) {
            LinkedList<Protocol.Sids> s1 = protocol.getKeyNegotiation()
                    .getAvailableSids();
            LinkedList<Protocol.Sids> s2 = before.getKeyNegotiation()
                    .getAvailableSids();
            for (int j = 0; j < s1.size(); j++) {
                res = res
                        && s2.get(i).getSids().get(j) == s2.get(i).getSids()
                                .get(j);
            }
        }

        return res;
    }

    private boolean validateNewKeyNegotiation(Protocol protocol) {
        
        for (int s : protocol.getKeyNegotiation().getAvailableSids().get(0).getSids()) {
            if (s == 0) {
                return true;
            }
        }
        
        return false;
    }

    private boolean validateNewKeyNegotiationLong(Protocol protocol) {
        boolean res = !protocol.getKeyNegotiation().getP().equals(BigInteger.ZERO)
                && !protocol.getKeyNegotiation().getQ().equals(BigInteger.ZERO)
                && protocol.getKeyNegotiation().getSid() == 0;
        
        System.out.println("4 - " + res);
        System.out.println(protocol.getKeyNegotiation().getSid());
        
        try {
            // create specifications for the key generation.
            SRAKeyGenParameterSpec specs = new SRAKeyGenParameterSpec(4096, protocol.getKeyNegotiation().getP(), protocol.getKeyNegotiation().getQ());
    
            this.generator = KeyPairGenerator.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
    
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
        return protocol.getPayload().getInitialCoin().get(0).equals(before
                .getPayload().getInitialCoin().get(0))
                && protocol.getPayload().getInitialCoin().get(1).equals(before
                        .getPayload().getInitialCoin().get(1))
                && protocol.getPayload().getEncryptedCoin().get(0).equals(before
                        .getPayload().getEncryptedCoin().get(0))
                && protocol.getPayload().getEncryptedCoin().get(1).equals(before
                        .getPayload().getEncryptedCoin().get(1));
    }

    private boolean validateOldPayloadSecond(Protocol protocol, Protocol before) {
        return protocol.getPayload().getDesiredCoin().equals(before.getPayload()
                .getDesiredCoin())
                && protocol.getPayload().getEnChosenCoin().equals(before
                        .getPayload().getEnChosenCoin());
    }

    private boolean validateOldPayloadThird(Protocol protocol, Protocol before) {
        return protocol.getPayload().getDeChosenCoin().equals(before.getPayload()
                .getDeChosenCoin())
                && protocol.getPayload().getKeyA().get(0).equals(before
                        .getPayload().getKeyA().get(0))
                && protocol.getPayload().getKeyA().get(1).equals(before
                        .getPayload().getKeyA().get(1));
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
    	System.out.println(protocol.getPayload().getInitialCoin().get(0));
    	System.out.println(protocol.getPayload().getInitialCoin().get(1));
    	System.out.println("EnChosenCoin: '" + protocol.getPayload().getEnChosenCoin() + "'");
        return (protocol.getPayload().getDesiredCoin().equals(protocol.getPayload()
                .getInitialCoin().get(0)) || protocol.getPayload()
                .getDesiredCoin().equals(protocol.getPayload().getInitialCoin()
                .get(1)))
                && !protocol.getPayload().getEnChosenCoin().equals("");
    }

    private boolean validateNewPayloadThird(Protocol protocol) {
        return !protocol.getPayload().getDeChosenCoin().equals("")
                && !protocol.getPayload().getKeyA().get(0).equals(BigInteger.ZERO)
                && !protocol.getPayload().getKeyA().get(1).equals(BigInteger.ZERO);
    }

    private boolean validateNewPayloadForth(Protocol protocol) {
        return !protocol.getPayload().getKeyB().get(0).equals(BigInteger.ZERO)
                && !protocol.getPayload().getKeyB().get(1).equals(BigInteger.ZERO);
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
        if (protocol.getStatusMessage() != "OK") {
            System.out.println(protocol.getProtocolNegotiation()
                    .getAvailableVersions().get(0).getName()
                    + " says: " + protocol.getStatusMessage() + ".");
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
                System.out.println(protocolStep + " - " + everythingOK);
            case 6:
                everythingOK = everythingOK
                        && validateOldPayloadSecond(protocol, before);
                System.out.println(protocolStep + " - " + everythingOK);
            case 5:
                everythingOK = everythingOK
                        && validateOldPayloadFirst(protocol, before);
                System.out.println(protocolStep + " - " + everythingOK);
            case 4:
                everythingOK = everythingOK
                        && validateOldKeyNegotiation(protocol, before);
                System.out.println(protocolStep + " - " + everythingOK);
            case 3:
            case 2:
                everythingOK = everythingOK
                        && validateOldProtocolNegotiation(protocol, before);
                System.out.println(protocolStep + " - " + everythingOK);
            case 1:
            case 0:
                everythingOK = everythingOK
                        && validateGeneralAttributes(protocol, before);
                System.out.println(protocolStep + " - " + everythingOK);
            }
            System.out.println("All right for the first part! --> " + everythingOK);

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
            System.out.println("All right! --> " + everythingOK);
            
            probablyFunnyMessage(protocol);
        } catch (Exception e) {
            System.out.println("");
            System.out
                    .println("Something went wrong and came out with this error:");
            System.out.println(e);
            System.out.println("");
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
        v.setName("Maurice");
        v.getVersions().add("1.0");
        protocol.getProtocolNegotiation().getAvailableVersions().add(v);
    }

    /**
     * Ich machs mir einfach. Ich schreib einfach meine Protokoll-Version da
     * rein. Wenn der andere die nicht kann, soll er selber Fehler werfen!
     */
    private void addProtocolNegotiationDataSecond() {
        Protocol.Version v = new Protocol.Version();
        v.setName("Maurice");
        v.getVersions().add("1.0");
        protocol.getProtocolNegotiation().getAvailableVersions().add(v);
        // Später hier ansetzen zur Korrektur!
        // TODO: Auf Gleichheit prüfen von dem Anderen und eine gleiche Version
        // auswählen!
        protocol.getProtocolNegotiation().setVersion("1.0");
    }

    private void addKeyNegotiationFirst() {
        Protocol.Sids sids = new Protocol.Sids();
        sids.getSids().add(0);
        protocol.getKeyNegotiation().getAvailableSids().add(sids);
    }

    /**
     * Ich machs mir einfach. Ich schreib einfach meine SID da rein. Wenn der
     * andere die nicht kann, soll er selber Fehler werfen!
     */
    private void addKeyNegotiationSecond() {
        
        // provide a bit-size for the key (1024-bit key in this example).
        generator.initialize(4096);
        // generate the key pair.
        keyPair = generator.generateKeyPair();
        
        // get a key factory instance for SRA
        KeyFactory factory = null;
        try {
            factory = KeyFactory.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // extract p and q. you have to use the private key for this, since only the private key contains the information.
        // the key factory fetches the hidden information out of the private key and fills a SRADecryptionKeySpec to provide the information.
        SRADecryptionKeySpec spec = null;
        try {
            spec = factory.getKeySpec(keyPair.getPrivate(), SRADecryptionKeySpec.class);
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        System.out.println("Generiertes P: " + spec.getP());
        System.out.println("Generiertes Q: " + spec.getQ());
        
        protocol.getKeyNegotiation().setP(new BigInteger(spec.getP().toByteArray()));
        protocol.getKeyNegotiation().setQ(new BigInteger(spec.getQ().toByteArray()));
        protocol.getKeyNegotiation().setSid(0);
        
    }

    private void addPayloadFirst() {
        protocol.getPayload().getInitialCoin().add("H");
        protocol.getPayload().getInitialCoin().add("T");

        LinkedList<String> ec = new LinkedList<String>();
        
        try {
            Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
    
            // prepare the engine for encryption.
            engine.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            
            // encrypt something.
            byte[] encryptedHead = engine.doFinal(protocol.getPayload().getInitialCoin().get(0).getBytes("UTF-8"));
            byte[] encryptedTail = engine.doFinal(protocol.getPayload().getInitialCoin().get(1).getBytes("UTF-8"));
            
            ec.add(new String(encryptedHead, "UTF-8"));
            ec.add(new String(encryptedTail, "UTF-8"));
            
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
            Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
    
            // prepare the engine for encryption.
            engine.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            System.out.println("set EnChosenCoin soon... This --> " + protocol.getPayload().getEncryptedCoin().get(1));
            // encrypt something.
            byte[] enChosenCoin = engine.doFinal(protocol.getPayload().getEncryptedCoin().get(1).getBytes("UTF-8"));
            System.out.println("set EnChosenCoin: " + new String(enChosenCoin, "UTF-8"));            
            protocol.getPayload().setEnChosenCoin(new String(enChosenCoin, "UTF-8"));
            System.out.println("set EnChosenCoin: " + new String(enChosenCoin, "UTF-8"));
            
        } catch (Exception e) {
            System.out.println("Oh shit. The encryption is totally blown.");
            System.out.println(e);
        }
        
        
        
        
    }

    private void addPayloadThird() {
        // TODO: Tatsächlich Dinge verschlüsseln...
        LinkedList<BigInteger> keyA = new LinkedList<BigInteger>();
        
        
        try {
            Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
            // prepare for decryption
            engine.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            // decrypt the cipher.
            byte[] recover = engine.doFinal(protocol.getPayload().getEnChosenCoin().getBytes());
            
            protocol.getPayload().setDeChosenCoin(new String(recover, "UTF-8"));
            
            keyA.add(new BigInteger(keyPair.getPublic().getFormat()));
            keyA.add(new BigInteger(keyPair.getPrivate().getFormat()));
            protocol.getPayload().setKeyA(keyA);
            
        } catch (Exception e) {
            System.out.println("Oh shit. The decryption is totally blown.");
            System.out.println(e);
        }
        
        
        
        
        
        
    }

    private void addPayloadForth() {
        
        LinkedList<BigInteger> keyB = new LinkedList<BigInteger>();
        
        try {
            
            Cipher engine = Cipher.getInstance("SRA", BouncyCastleProvider.PROVIDER_NAME);
            // prepare for decryption
            engine.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            // decrypt the cipher.
            byte[] recover = engine.doFinal(protocol.getPayload().getDeChosenCoin().getBytes());
            String result = new String(recover, "UTF-8");
            
            System.out.println("The final Result is: " + result);
            
            System.out.println("And you chose: " + protocol.getPayload().getDesiredCoin());
            
            
            keyB.add(new BigInteger(keyPair.getPublic().getFormat()));
            keyB.add(new BigInteger(keyPair.getPrivate().getFormat()));
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
        System.out.println("1");
        /**
         * Hier Berechnungen durchführen und im protocol rumschreiben :)
         */

        int protocolStep = protocol.getProtocolId() + 1;

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
        System.out.println("Step " + protocolStep + " finished.");

        return mapper.writeValueAsString(protocol);
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
            System.out.println(json);
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
