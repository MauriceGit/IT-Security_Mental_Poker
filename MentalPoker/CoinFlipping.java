import java.math.BigInteger;
import java.util.Random;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class CoinFlipping {
    
    private BigInteger e; 
    private SRAKeyParameters pubParams;
    private SRAKeyParameters privParams;
    private AsymmetricBlockCipher encrEngine = new SRAEngine();
    private AsymmetricBlockCipher decrEngine = new SRAEngine();
    
    // My challenge:
    private BigInteger myChallenge;
    private BigInteger myChallengeEncr;
    
    // Oh dear, I got a challenge my own to solve:
    private BigInteger otherChallenge;
    private Coin myGuess;

    public CoinFlipping(BigInteger p, BigInteger q) {
        generateE();
        pubParams = new SRAKeyParameters(false, p, q, e);
        privParams = new SRAKeyParameters(true, p, q, e);
        encrEngine.init(true, pubParams);
        decrEngine.init(false, privParams);        
    }
    
    private void generateE()
    {
        // Ja, ich weiß, ist nicht so toll, weil Random wahrscheinlich nicht so doll ist...
        e = BigInteger.probablePrime(1024, new Random());
    }
    
    // ========================================================================================
    // My challenge:
    // ========================================================================================
    
    public BigInteger createChallenge()
    {
        myChallenge = new BigInteger(256, new Random());
        try {
            myChallengeEncr = new BigInteger(encrEngine.processBlock(myChallenge.toByteArray(), 0, myChallenge.toByteArray().length));
        } catch (InvalidCipherTextException e) {
        }                
        return myChallengeEncr;
    }
    
    public AsymmetricBlockCipher solveChallenge(Coin guess)
    {
        // % 2 == 0 --> HEAD
        Coin correctAnswer = (myChallenge.mod(new BigInteger("2")).intValue() == 0 ? Coin.HEAD : Coin.TAILS); 
        System.out.println("According to the challenger, the other guy guessed: " + (guess == correctAnswer) );
        return decrEngine;
    }
    
    // ========================================================================================
    // Oh dear, I got a challenge my own to solve:
    // ========================================================================================
    
    public Coin getChallenge(BigInteger challenge)
    {
        Random rand = new Random();
        otherChallenge = challenge;
        myGuess = rand.nextInt(2) == 0 ? Coin.HEAD : Coin.TAILS;
        return myGuess;
    }
    
    public void checkResult(AsymmetricBlockCipher decrypter)
    {
        BigInteger decryptedChallenge = null;
        try {
            decryptedChallenge = new BigInteger(decrypter.processBlock(otherChallenge.toByteArray(), 0, otherChallenge.toByteArray().length));
        } catch (InvalidCipherTextException e) {
        }
        Coin result = decryptedChallenge.mod(new BigInteger("2")).intValue() == 0 ? Coin.HEAD : Coin.TAILS;
        
        System.out.println("My conclusion is, that I guessed: " + (result == myGuess));
        
    }
    
}
