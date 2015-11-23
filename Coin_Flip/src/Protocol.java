import java.math.BigInteger;
import java.util.LinkedList;

import com.fasterxml.jackson.annotation.JsonFilter;

public class Protocol {

    /**
     * Wird immer übertragen!
     */
    private int protocolId = -1;
    private int statusId = 0;
    private String statusMessage = "OK";
    /**
     * Wird in den ersten Schritten ausgehandelt!
     */
    private ProtocolNegotiation protocolNegotiation = new ProtocolNegotiation();
    /**
     * Aushandeln danach!
     */
    private KeyNegotiation keyNegotiation = new KeyNegotiation();
    /**
     * Das eigentliche Spiel.
     */
    private Payload payload = new Payload();
    
    public static class ProtocolNegotiation {
        private LinkedList<Version> availableVersions = new LinkedList<Version>();
        private int version = 0;

        public LinkedList<Version> getAvailableVersions() {
            return availableVersions;
        }

        public void setAvailableVersions(LinkedList<Version> availableVersions) {
            this.availableVersions = availableVersions;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        @Override
        public String toString() {
            return "ProtocolNegotiation [availableVersions="
                    + availableVersions + ", version=" + version + "]";
        }
    }

    public static class Version {
        @Override
        public String toString() {
            return "Version [name=" + name + ", versions=" + versions + "]";
        }

        private String name = "Maurice";
        private LinkedList<Integer> versions = new LinkedList<Integer>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LinkedList<Integer> getVersions() {
            return versions;
        }

        public void setVersions(LinkedList<Integer> versions) {
            this.versions = versions;
        }
    }
    
    public static class KeyNegotiation {
        private LinkedList<Sids> availableSids = new LinkedList<Sids>();
        private int sid = 0;
        private BigInteger p = BigInteger.ZERO;
        private BigInteger q = BigInteger.ZERO;

        @Override
        public String toString() {
            return "KeyNegotiation [avalableSids=" + availableSids + ", sid="
                    + sid + ", p=" + p + ", q=" + q + "]";
        }

        public LinkedList<Sids> getAvailableSids() {
            return availableSids;
        }

        public void setAvailableSids(LinkedList<Sids> avalableSids) {
            this.availableSids = avalableSids;
        }

        public int getSid() {
            return sid;
        }

        public void setSid(int sid) {
            this.sid = sid;
        }

        public BigInteger getP() {
            return p;
        }

        public void setP(BigInteger p) {
            this.p = p;
        }

        public BigInteger getQ() {
            return q;
        }

        public void setQ(BigInteger q) {
            this.q = q;
        }

        
    }

    public static class Sids {
        private LinkedList<Integer> sids = new LinkedList<Integer>();

        @Override
        public String toString() {
            return "Sids [sids=" + sids + "]";
        }

        public LinkedList<Integer> getSids() {
            return sids;
        }

        public void setSids(LinkedList<Integer> sids) {
            this.sids = sids;
        }
    }
    
    public static class Payload {
        private LinkedList<String> initialCoin = new LinkedList<String>();
        private String desiredCoin = "";
        private LinkedList<String> encryptedCoin = new LinkedList<String>();
        private String enChosenCoin = "";
        private String deChosenCoin = "";
        private LinkedList<BigInteger> keyA = new LinkedList<BigInteger>();
        private LinkedList<BigInteger> keyB = new LinkedList<BigInteger>();
        private String signatureA = "";
        public LinkedList<String> getInitialCoin() {
            return initialCoin;
        }
        public void setInitialCoin(LinkedList<String> initialCoin) {
            this.initialCoin = initialCoin;
        }
        @Override
        public String toString() {
            return "Payload [initialCoin=" + initialCoin + ", desiredCoin="
                    + desiredCoin + ", encryptedCoin=" + encryptedCoin
                    + ", enChosenCoin=" + enChosenCoin + ", deChosenCoin="
                    + deChosenCoin + ", keyA=" + keyA + ", keyB=" + keyB
                    + ", signatureA=" + signatureA + "]";
        }
        public String getDesiredCoin() {
            return desiredCoin;
        }
        public void setDesiredCoin(String desiredCoin) {
            this.desiredCoin = desiredCoin;
        }
        public LinkedList<String> getEncryptedCoin() {
            return encryptedCoin;
        }
        public void setEncryptedCoin(LinkedList<String> encryptedCoin) {
            this.encryptedCoin = encryptedCoin;
        }
        public String getEnChosenCoin() {
            return enChosenCoin;
        }
        public void setEnChosenCoin(String enChosenCoin) {
            this.enChosenCoin = enChosenCoin;
        }
        public String getDeChosenCoin() {
            return deChosenCoin;
        }
        public void setDeChosenCoin(String deChosenCoin) {
            this.deChosenCoin = deChosenCoin;
        }
        public LinkedList<BigInteger> getKeyA() {
            return keyA;
        }
        public void setKeyA(LinkedList<BigInteger> keyA) {
            this.keyA = keyA;
        }
        public LinkedList<BigInteger> getKeyB() {
            return keyB;
        }
        public void setKeyB(LinkedList<BigInteger> keyB) {
            this.keyB = keyB;
        }
        public String getSignatureA() {
            return signatureA;
        }
        public void setSignatureA(String signatureA) {
            this.signatureA = signatureA;
        }
    }

    public KeyNegotiation getKeyNegotiation() {
        return keyNegotiation;
    }

    public void setKeyNegotiation(KeyNegotiation keyNegotiation) {
        this.keyNegotiation = keyNegotiation;
    }

    public ProtocolNegotiation getProtocolNegotiation() {
        return protocolNegotiation;
    }

    public void setProtocolNegotiation(ProtocolNegotiation protocolNegotiation) {
        this.protocolNegotiation = protocolNegotiation;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public int getStatusId() {
        return statusId;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @Override
    public String toString() {
        return "Protocol [protocolId=" + protocolId + ", statusId=" + statusId
                + ", statusMessage=" + statusMessage + ", protocolNegotiation="
                + protocolNegotiation + ", keyNegotiation=" + keyNegotiation
                + ", payload=" + payload + "]";
    }

}
