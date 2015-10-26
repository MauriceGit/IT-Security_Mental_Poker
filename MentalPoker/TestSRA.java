import java.math.BigInteger;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class TestSRA {
    public static void main(String[] args) {
        
        // Alice und Bob einigen sich auf ein p und q --> n:
        BigInteger  p = new BigInteger("f75e80839b9b9379f1cf1128f321639757dba514642c206bbbd99f9a4846208b3e93fbbe5e0527cc59b1d4b929d9555853004c7c8b30ee6a213c3d1bb7415d03", 16);
        BigInteger  q = new BigInteger("b892d9ebdbfc37e397256dd8a5d3123534d1f03726284743ddc6be3a709edb696fc40c7d902ed804c6eee730eee3d5b20bf6bd8d87a296813c87d3b3cc9d7947", 16);
        BigInteger n = p.multiply(q);
        
        // Alice initialisiert sich selbst und denkt sich ein privates e aus.
        BigInteger alicePrivateE = new BigInteger("a892d9eedbfc37e397256dd8a5d3123534d1f03726284743ddc6be3a709edb696fc40c7d902ed804c6eee730eee3d5b20bf6bd8d87a296813c87d3b3cc9d7947", 16); 
        SRAKeyParameters alicePubParams = new SRAKeyParameters(false, p, q, alicePrivateE);
        SRAKeyParameters alicePrivParams = new SRAKeyParameters(true, p, q, alicePrivateE);
        AsymmetricBlockCipher aliceEncrEngine = new SRAEngine();
        AsymmetricBlockCipher aliceDecrEngine = new SRAEngine();
        aliceEncrEngine.init(true, alicePubParams);
        aliceDecrEngine.init(false, alicePrivParams);
        
        // Alice denkt sich eine total zufällige Zahl aus, die Bob raten soll (obs gerade ist):
        // Die Challenge ist gewonnen, wenn Bob ungerade (False) rät.
        // und verloren, wenn Bob gerade (True) rät.
        BigInteger challengeFromAlice = new BigInteger("7", 16);
        
        // Alice verschlüsselt die Zahl:
        byte[] challengeForBob = null;
        try {
            challengeForBob = aliceEncrEngine.processBlock(challengeFromAlice.toByteArray(), 0, challengeFromAlice.toByteArray().length);
        } catch (InvalidCipherTextException e) {}
        
        // Bob rät, ob es gerade oder ungerade ist:
        // Bob rät: Ungerade!
        boolean bobsGuess = true;
        
        // Alice schickt Bob jetzt ihren privaten Schlüssel, so dass Bob es nachprüfen kann:
        AsymmetricBlockCipher bobTestEngine = aliceDecrEngine;
        
        // Bob nimmt jetzt die challenge und entschlüsselt sie:
        byte[] bobsResult = null;
        try {
            bobsResult = bobTestEngine.processBlock(challengeForBob, 0, challengeForBob.length);
        } catch (InvalidCipherTextException e) {}
        
        // Und? Hat Bob recht gehabt?
        BigInteger challengeResult = new BigInteger(bobsResult);
        BigInteger isOdd = challengeResult.mod(new BigInteger("2"));
        boolean correct = (bobsGuess == (isOdd.intValue() == 0));
        System.out.println("Bobs guess is --> " + (correct ? "correct" : "incorrect"));
        
    }

}
