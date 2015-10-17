
import java.math.BigInteger;
import java.security.spec.RSAPrivateCrtKeySpec;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.util.encoders.Hex;



public class TestRSA {

    public static void main(String[] args) {
        
        // Warum diese Parameter für das Entschlüsselln benötigt werden und die Reihenfolge etc.
        // ist definiert nach PKCS#1.
        // Seite 42 des Dokumentes dokumentiert die genaue Definition der folgenden Parameter.
        
        // p
        BigInteger  p = new BigInteger("f75e80839b9b9379f1cf1128f321639757dba514642c206bbbd99f9a4846208b3e93fbbe5e0527cc59b1d4b929d9555853004c7c8b30ee6a213c3d1bb7415d03", 16);
        // q
        BigInteger  q = new BigInteger("b892d9ebdbfc37e397256dd8a5d3123534d1f03726284743ddc6be3a709edb696fc40c7d902ed804c6eee730eee3d5b20bf6bd8d87a296813c87d3b3cc9d7947", 16);
        // Der zu verschlüsselnde Input.
        String edgeInput = "ff6f77206973207468652074696d6520666f7220616c6c20676f6f64206d656e";
        // n = p * q
        BigInteger n = p.multiply(q);
        // e
        BigInteger  e = new BigInteger("11", 16);
        
        // d
        BigInteger pMinusOne = p.subtract(new BigInteger("1"));
        BigInteger qMinusOne = q.subtract(new BigInteger("1"));
        BigInteger d = e.modInverse(pMinusOne.multiply(qMinusOne));
        
        // d mod (p-1)
        BigInteger  pExp = d.mod(pMinusOne);
        // d mod (q-1)
        BigInteger  qExp = d.mod(qMinusOne);
        // Koeffizient des Chinese-Remainder-Theorem: q^-1 mod p
        BigInteger  crtCoef = q.modInverse(p);

        byte[]              data = Hex.decode(edgeInput);
        byte[]                encrData = null;
        byte[]                decrData = null;
        
        ////////////////////////////////////////////////////////////////
        // Encryption
        ////////////////////////////////////////////////////////////////
        
        RSAKeyParameters    pubParameters = new RSAKeyParameters(false, n, e);
        AsymmetricBlockCipher   engEn = new RSAEngine();        
        engEn.init(true, pubParameters);        
        
        try {
            encrData = engEn.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException error) {
            error.printStackTrace();
        }
        
        ////////////////////////////////////////////////////////////////
        // Decryption
        ////////////////////////////////////////////////////////////////
        
        RSAKeyParameters    privParameters = new RSAPrivateCrtKeyParameters(n, e, d, p, q, pExp, qExp, crtCoef);
        AsymmetricBlockCipher   engDe = new RSAEngine();        
        engDe.init(false,  privParameters);
        
        try {
            decrData = engDe.processBlock(encrData, 0, encrData.length);
        } catch (InvalidCipherTextException error) {
            error.printStackTrace();
        }
        
        ////////////////////////////////////////////////////////////////
        // Test
        ////////////////////////////////////////////////////////////////
        
        String text1 = Hex.toHexString(data);
        String text2 = Hex.toHexString(decrData);
        
        if (text1.equals(text2)) {
            System.out.println("Yay, erfolgreich ver- und wieder entschlüsselt.");
        } else {
            System.out.println("Nope ... ");
            System.out.println("data:     " + text1);
            System.out.println("decrData: " + text2);
        }
        
    }

}
