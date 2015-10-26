import java.math.BigInteger;

import org.bouncycastle.crypto.params.AsymmetricKeyParameter;


public class SRAKeyParameters 
    extends AsymmetricKeyParameter
{

    private BigInteger p;
    private BigInteger q;
    private BigInteger exponent;
    
    public SRAKeyParameters(
            boolean isPrivate,
            BigInteger p,
            BigInteger q,
            BigInteger exponent) {
        super (isPrivate);
        this.p = p;
        this.q = q;
        this.exponent = exponent;
    }
    
    public BigInteger getModulus()
    {
        return p.multiply(q);
    }
    
    public BigInteger getExponent()
    {
        return exponent;
    }
    
    public BigInteger getP() 
    {
        return p;
    }
    
    public BigInteger getQ() 
    {
        return q;
    }

}
