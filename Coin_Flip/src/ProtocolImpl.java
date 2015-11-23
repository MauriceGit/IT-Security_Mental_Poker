import java.math.BigInteger;
import java.util.LinkedList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtocolImpl {

    private ObjectMapper mapper;
    private Protocol protocol;

    public ProtocolImpl() {
        protocol = new Protocol();
        mapper = new ObjectMapper();
    }

    private void p(String s) {
        //System.out.println(s);
    }

    /**
     * Test, ob ProtokollID aufsteigend ist.
     * 
     * @param protocol
     * @param before
     * @return
     */
    private boolean validateGeneralAttributes(Protocol protocol, Protocol before) {
        return protocol.getProtocolId() == before.getProtocolId() + 1
                && protocol.getStatusId() == 0 && before.getStatusId() == 0;
    }

    /**
     * Ob sinnvolle Daten mitgeschickt wurden! Es kann gut sein, dass hier
     * NullPointerExceptions fliegen!!!! Die werden ausgewertet zu: false,
     * Validierung fehlgeschlagen!
     * 
     * @param protocol
     * @param before
     * @return
     */
    private boolean validateNewProtocolNegotiation(Protocol protocol) {
        return protocol.getProtocolNegotiation().getAvailableVersions().size() > 0
                && protocol.getProtocolNegotiation().getAvailableVersions()
                        .get(0).getVersions().size() > 0;
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
                && protocol.getProtocolNegotiation().getVersion() == before
                        .getProtocolNegotiation().getVersion();
        // Steht jeweils das Gleiche drin?
        for (int i = 0; i < protocol.getProtocolNegotiation()
                .getAvailableVersions().size(); i++) {
            Protocol.Version v1 = protocol.getProtocolNegotiation()
                    .getAvailableVersions().get(i);
            Protocol.Version v2 = before.getProtocolNegotiation()
                    .getAvailableVersions().get(i);
            for (int j = 0; j < v1.getVersions().size(); j++) {
                Integer vs1 = v1.getVersions().get(j);
                Integer vs2 = v2.getVersions().get(j);
                res = res && vs1 == vs2;
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
        return protocol.getKeyNegotiation().getAvailableSids().size() != 0;
    }

    private boolean validateNewKeyNegotiationLong(Protocol protocol) {
        return !protocol.getKeyNegotiation().getP().equals(BigInteger.ZERO)
                && !protocol.getKeyNegotiation().getQ().equals(BigInteger.ZERO)
                && protocol.getKeyNegotiation().getSid() >= 0;
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
                && protocol.getPayload().getEncryptedCoin().get(0) != ""
                && protocol.getPayload().getEncryptedCoin().get(1) != "";
    }

    private boolean validateNewPayloadSecond(Protocol protocol) {
        return (protocol.getPayload().getDesiredCoin().equals(protocol.getPayload()
                .getInitialCoin().get(0)) || protocol.getPayload()
                .getDesiredCoin().equals(protocol.getPayload().getInitialCoin()
                .get(1)))
                && !protocol.getPayload().getEnChosenCoin().equals("");
    }

    private boolean validateNewPayloadThird(Protocol protocol) {
        return protocol.getPayload().getDeChosenCoin() != ""
                && protocol.getPayload().getKeyA().get(0) != BigInteger.ZERO
                && protocol.getPayload().getKeyA().get(1) != BigInteger.ZERO;
    }

    private boolean validateNewPayloadForth(Protocol protocol) {
        return protocol.getPayload().getKeyB().get(0) != BigInteger.ZERO
                && protocol.getPayload().getKeyB().get(1) != BigInteger.ZERO;
    }

    /**
     * Sinnvolle Version ausgewählt?
     * 
     * @param protocol
     * @return
     */
    private boolean validateChosenVersion(Protocol protocol) {
        return protocol.getProtocolNegotiation().getVersion() > 0;
    }

    private void probablyFunnyMessage(Protocol protocol) {
        if (protocol.getStatusMessage() != "OK") {
            System.out.println(protocol.getProtocolNegotiation()
                    .getAvailableVersions().get(0).getName()
                    + " says: " + protocol.getStatusMessage() + ".");
        }
    }

    private boolean iCanHandleTheChosenVersion(Protocol protocol) {
        return protocol.getProtocolNegotiation().getVersion() == 1;
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
                p(protocolStep + " == " + everythingOK);
            case 6:
                everythingOK = everythingOK
                        && validateOldPayloadSecond(protocol, before);
                p(protocolStep + " == " + everythingOK);
            case 5:
                everythingOK = everythingOK
                        && validateOldPayloadFirst(protocol, before);
                p(protocolStep + " == " + everythingOK);                
            case 4:
                everythingOK = everythingOK
                        && validateOldKeyNegotiation(protocol, before);
                p(protocolStep + " == " + everythingOK);
            case 3:
            case 2:
                everythingOK = everythingOK
                        && validateOldProtocolNegotiation(protocol, before);
                p(protocolStep + " == " + everythingOK);
            case 1:
            case 0:
                everythingOK = everythingOK
                        && validateGeneralAttributes(protocol, before);
                p(protocolStep + " == " + everythingOK);
            }

            // Tests, die nur in dem jeweiligen Schritt einmal getestet werden
            // müssen!!!
            switch (protocolStep) {
            case 7:
                everythingOK = everythingOK
                        && validateNewPayloadForth(protocol);
                p(protocolStep + " == " + everythingOK);
                break;
            case 6:
                everythingOK = everythingOK
                        && validateNewPayloadThird(protocol);
                p(protocolStep + " == " + everythingOK);
                break;
            case 5:
                everythingOK = everythingOK
                        && validateNewPayloadSecond(protocol);
                p(protocolStep + " == " + everythingOK);
                break;
            case 4:
                everythingOK = everythingOK
                        && validateNewPayloadFirst(protocol);
                p(protocolStep + " == " + everythingOK);
                break;
            case 3:
                everythingOK = everythingOK
                        && validateNewKeyNegotiationLong(protocol);
                p(protocolStep + " == " + everythingOK);
                break;
            case 2:
                everythingOK = everythingOK
                        && validateNewKeyNegotiation(protocol);
                p(protocolStep + " == " + everythingOK);
                break;
            case 1:
                everythingOK = everythingOK && validateChosenVersion(protocol)
                        && validateNewProtocolNegotiation(protocol);
                everythingOK = everythingOK
                        && iCanHandleTheChosenVersion(protocol);
                p(protocolStep + " == " + everythingOK);
                break;
            case 0:
                everythingOK = everythingOK
                        && validateNewProtocolNegotiation(protocol);
                p(protocolStep + " == " + everythingOK);
            }

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
        v.getVersions().add(1);
        protocol.getProtocolNegotiation().getAvailableVersions().add(v);
    }

    /**
     * Ich machs mir einfach. Ich schreib einfach meine Protokoll-Version da
     * rein. Wenn der andere die nicht kann, soll er selber Fehler werfen!
     */
    private void addProtocolNegotiationDataSecond() {
        Protocol.Version v = new Protocol.Version();
        v.setName("Maurice");
        v.getVersions().add(1);
        protocol.getProtocolNegotiation().getAvailableVersions().add(v);
        // Später hier ansetzen zur Korrektur!
        // TODO: Auf Gleichheit prüfen von dem Anderen und eine gleiche Version
        // auswählen!
        protocol.getProtocolNegotiation().setVersion(1);
    }

    private void addKeyNegotiationFirst() {
        Protocol.Sids sids = new Protocol.Sids();
        sids.getSids().add(15);
        protocol.getKeyNegotiation().getAvailableSids().add(sids);
    }

    /**
     * Ich machs mir einfach. Ich schreib einfach meine SID da rein. Wenn der
     * andere die nicht kann, soll er selber Fehler werfen!
     */
    private void addKeyNegotiationSecond() {
        protocol.getKeyNegotiation().setP(new BigInteger("1234"));
        protocol.getKeyNegotiation().setQ(new BigInteger("5678"));
        protocol.getKeyNegotiation().setSid(15);
    }

    private void addPayloadFirst() {
        protocol.getPayload().getInitialCoin().add("HEAD");
        protocol.getPayload().getInitialCoin().add("TAIL");

        // TODO: Tatsächlich Dinge verschlüsseln...
        LinkedList<String> ec = new LinkedList<String>();
        ec.add("encryptedHEAD");
        ec.add("encryptedTAIL");
        protocol.getPayload().setEncryptedCoin(ec);
    }

    private void addPayloadSecond() {
        protocol.getPayload().setDesiredCoin("TAIL");
        // TODO: Tatsächlich Dinge verschlüsseln...
        protocol.getPayload().setEnChosenCoin("EnChosenCoin");
    }

    private void addPayloadThird() {
        // TODO: Tatsächlich Dinge verschlüsseln...
        protocol.getPayload().setDeChosenCoin("DeChosenCoin");
        LinkedList<BigInteger> keyA = new LinkedList<BigInteger>();
        keyA.add(new BigInteger("80081"));
        keyA.add(new BigInteger("80082"));
        protocol.getPayload().setKeyA(keyA);
    }

    private void addPayloadForth() {
        // TODO: Tatsächlich Dinge verschlüsseln...
        LinkedList<BigInteger> keyB = new LinkedList<BigInteger>();
        keyB.add(new BigInteger("800811"));
        keyB.add(new BigInteger("800822"));
        protocol.getPayload().setKeyB(keyB);
        protocol.getPayload().setSignatureA("BigAsSignature");
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

        protocol.setProtocolId(protocolStep);

        switch (protocolStep) {
        case 7:
            addPayloadForth();
            break;
        case 6:
            addPayloadThird();
            break;
        case 5:
            addPayloadSecond();
            break;
        case 4:
            addPayloadFirst();
            break;
        case 3:
            addKeyNegotiationSecond();
            break;
        case 2:
            addKeyNegotiationFirst();
            break;
        case 1:
            addProtocolNegotiationDataSecond();
            break;
        case 0:
            addGeneralData();
            addProtocolNegotiationDataFirst();
        }

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
            p("Error");
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
