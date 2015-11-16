import static java.lang.Math.toIntExact;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ProtocolImpl {
    
    /**
     * Alle für das Protokoll wichtigen Felder!
     */
    public static final String PROTOCOL_ID = "protocolId";
    public static final String STATUS_ID = "statusID";
    public static final String STATUS_MESSAGE = "statusMessage";
    
    public static final String AVAILABLE_VERSIONS = "availableVersions";
    public static final String VERSION = "version";
    
    public static final String AVAILABLE_SIDS = "availableSIDs";
    public static final String SID = "SID";
    public static final String P = "P";
    public static final String Q = "Q";
    
    public static final String INITIAL_COIN = "initialCoin";
    public static final String DESIRED_COIN = "desiredCoin";
    public static final String ENCRYPTED_COIN = "encryptedCoin";
    public static final String EN_CHOSEN_COIN = "enChosenCoin";
    public static final String DE_CHOSEN_COIN = "deChosenCoin";
    public static final String KEY_A = "keyA";
    public static final String KEY_B = "keyB";
    public static final String SIGNATURE_A = "signatureA";
    
    /**
     * Hier stehen aus dem letzten Protokoll-Schritt alle Werte drin!
     */
    private Map<String, Serializable> protocolValues;
//    private JSONParser parser;
//    private ContainerFactory containerFactory;
    
    public ProtocolImpl() {
        protocolValues = new LinkedHashMap<String, Serializable>();
        initEmptyProtocol();
//        parser = new JSONParser();
//        containerFactory = new ContainerFactory() {
//            public List<?> creatArrayContainer() {
//                return new LinkedList<Object>();
//            }
//            public Map<?, ?> createObjectContainer() {
//                return new LinkedHashMap<Object, Object>();
//            }
//        };
    }
    
    /**
     * Initialisiert die Map mit leeren Werten.
     */
    private void initEmptyProtocol() {
        /**
         * Allgemeine, veränderbare Daten:
         */
        protocolValues.put(PROTOCOL_ID, 0);
        protocolValues.put(STATUS_ID,   0);
        protocolValues.put(STATUS_MESSAGE, "Alles gut.");
        
        /**
         * ProtocolImpl-Negotiation:
         */
        protocolValues.put(AVAILABLE_VERSIONS, new LinkedList<Object>());
        protocolValues.put(VERSION, "0.0.0");
        
        /**
         * Key-Negotiation:
         */
        protocolValues.put(AVAILABLE_SIDS, new LinkedList<Object>());
        protocolValues.put(SID, 0);
        protocolValues.put(P, 0x000000);
        protocolValues.put(Q, 0x000000);
        
        /**
         * Payload:
         */
        LinkedList<Integer> coin = new LinkedList<Integer>();
        coin.add(0);
        coin.add(1);
        protocolValues.put(INITIAL_COIN, coin);
        protocolValues.put(DESIRED_COIN, 0);
        LinkedList<Integer> enCoin = new LinkedList<Integer>();
        enCoin.add(0x000);
        enCoin.add(0x000);
        protocolValues.put(ENCRYPTED_COIN, enCoin);
        protocolValues.put(EN_CHOSEN_COIN, 0x000);
        protocolValues.put(DE_CHOSEN_COIN, 0x000);
        HashMap<String, Integer> keyA = new HashMap<String, Integer>();
        keyA.put("private", 0x000);
        keyA.put("public" , 0x000);
        protocolValues.put(KEY_A, keyA);
        HashMap<String, Integer> keyB = new HashMap<String, Integer>();
        keyB.put("private", 0x000);
        keyB.put("public" , 0x000);
        protocolValues.put(KEY_B, keyB);
        protocolValues.put(SIGNATURE_A, 0x000);
    }
    
    /**
     * Verifiziert, dass die übergebenen Werte sich nicht verändert haben!
     * 
     * @param toTest Werte, die sich nicht verändern sollten.
     * @param newValues Map mit Werten aus dem aktuellen Json-File.
     * @return ob alles OK ist und nichts verändert wurde.
     */
    private boolean validateSetValues(LinkedList<String> toTest, Map<?, ?> newValues) {
        
        for (String test : toTest) {
            Object now = newValues.get(test);
            Object before = protocolValues.get(test);
            if (now.equals(before)) {
                System.out.println("Error! " + test + " ---> " + now + " != " + before);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Verifiziert, dass die Protokollversion sich immer um genau 1 erhöht und der Status korrekt ist!
     * 
     * @param newValues Map mit Werten aus dem aktuellen Json-File.
     * @return ob die ID korrekt inkrementiert wurde und der Status == 0 ist.
     */
    private boolean validateGeneralValues(Map<?, ?> newValues) {
        Integer protocolIDNow = toIntExact((long)newValues.get(PROTOCOL_ID));
        Integer protocolIDBefore = (Integer) protocolValues.get(PROTOCOL_ID);
        
        Integer status = toIntExact((long)newValues.get(STATUS_ID));
        
        if (status != 0) {
            System.out.println("Status is not 0, Exception-Message:");
            System.out.println((String)protocolValues.get("statusMessage"));
            return false;
        }
        
        if (protocolIDNow != protocolIDBefore+1) {
            System.out.println("Protocol ID is not properly incremented.");
            System.out.println("It should be: " + (protocolIDBefore+1) + ", but is: " + protocolIDNow);
            return false;
        }
        
        return true;
    }
    
    /**
     * Verifiziert einen Protokoll-Schritt!
     * 
     * @param newValues
     * @return
     */
    public boolean validateProtocolStep(Protocol protocol, Protocol before) {
        boolean everythingOK = true;
        
        LinkedList<String> toTest = new LinkedList<String>();
        
        switch (protocol.getProtocolId()) {
        case 8:
            everythingOK = protocol.getPayload().getDeChosenCoin() == before.getPayload().getDeChosenCoin();
            everythingOK = protocol.getPayload().getKeyA() == before.getPayload().getKeyA();
            everythingOK = protocol.getPayload().getSignatureA() == before.getPayload().getSignatureA();
        case 7:
            everythingOK = protocol.getPayload().getEnChosenCoin() == before.getPayload().getEnChosenCoin()
            everythingOK = protocol.getPayload().getDesiredCoin() == before.getPayload().getDesiredCoin();
            toTest.add(DESIRED_COIN);
        case 6:
            toTest.add(ENCRYPTED_COIN);
        case 5:
            toTest.add(SID);
            toTest.add(P);
            toTest.add(Q);
        case 4: 
        case 3:
            toTest.add(VERSION);
        case 2:
        case 1:
        case 0:
        }
        
        /* Gleichheitstest auf syntaktischer Ebene! */
        everythingOK = everythingOK && validateSetValues(toTest, newValues);
        System.out.println("Const Values   valid: " + everythingOK);
        /* Check der allgemeinen, veränderbaren Werte! */
        everythingOK = everythingOK && validateGeneralValues(newValues);
        System.out.println("General Values valid: " + everythingOK);
        
        return everythingOK;
    }
    
    public void jsonToMap (String json) {
        
        ObjectMapper mapper = new ObjectMapper();
        Protocol protocol = null;
        
        try {
            protocol = (Protocol) mapper.readValue(new File("data.json"), Protocol.class);
        } catch (Exception e) {
            System.out.println("Fehler : " + e);
        }
        
        
        
        System.out.println(protocol);

    }

}
