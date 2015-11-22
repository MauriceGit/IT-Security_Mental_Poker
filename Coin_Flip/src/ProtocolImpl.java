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



    private void setGeneralAttributes(int protocolID, int statusID,
            String message) {
        protocol.setProtocolId(protocolID);
        protocol.setStatusId(statusID);
        protocol.setStatusMessage(message);
    }

    private void setGeneralAttributes() {
        setGeneralAttributes(0, 0, "OK, let's get ready to rumble!");
    }

    private void setProtocolNegotiation(int version,
            LinkedList<Protocol.Version> versions) {
        Protocol.ProtocolNegotiation protocolNegotiation = new Protocol.ProtocolNegotiation();
        protocolNegotiation.setVersion(version);
        protocolNegotiation.setAvailableVersions(versions);
        protocol.setProtocolNegotiation(protocolNegotiation);
    }

    private void setProtocolNegotiation(int version, Protocol.Version oneVersion) {
        LinkedList<Protocol.Version> versions = new LinkedList<Protocol.Version>();
        versions.add(oneVersion);
        setProtocolNegotiation(version, versions);
    }

    private void setProtocolNegotiation() {
        Protocol.Version version = new Protocol.Version();
        version.setName("Maurice");
        LinkedList<Integer> vs = new LinkedList<Integer>();
        vs.add(1);
        version.setVersions(vs);
        setProtocolNegotiation(-1, version);
    }

    public String constructInitialJson() throws JsonProcessingException {
        
        setGeneralAttributes();
        setProtocolNegotiation();
        
        String jsonInString = null;
        jsonInString = mapper.writeValueAsString(protocol);
        
        return jsonInString;
    }
    
    /**
     * Verifiziert einen Protokoll-Schritt!
     * 
     * @param newValues
     * @return
     */
    public boolean validateProtocolStep(Protocol protocol, Protocol before) {
        boolean everythingOK = true;
        
        return everythingOK;
    }
    
    public boolean protocolFinished(Protocol protocol) {
        
        return protocol.getProtocolId() >= 7;
        
    }
    
    /**
     * Geht davon aus, dass Konsistenz korrekt ist und der Wert in protocol der
     * aktuellen Situation entspricht!
     * Berechnet dann den neuen Wert!
     * @return Neues Json.
     * @throws JsonProcessingException 
     */
    public String calcAndRespondToProtocolStep() throws JsonProcessingException {
        
        /**
         * Hier Berechnungen durchführen und im protocol rumschreiben :)
         */
        
        return mapper.writeValueAsString(protocol);
    }
    
    /**
     * Speichert das Json ab und überprüft Konsistenz zu alten Zuständen!
     * @param json der empfangene Json-String.
     * @return der Status, ob alles in Ordnung und konsistent ist.
     */
    public Status status(String json) {
        
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
            return Status.PROTOCOL_ERROR;
        }
        
        if (protocolFinished(newProtocol)) {
            
        }
        
        protocol = newProtocol;
        
        return Status.PROTOCOL_OK;
    }



}
