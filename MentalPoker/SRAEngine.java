
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class SRAEngine 
    implements AsymmetricBlockCipher 
{
    
    private SRACoreEngine core;
   
    /**
     * initialize the SRA engine.
     * 
     * @param forEncryption brauchen wir das überhaupt?
     * @param param the necessary SRA key parameters.
     */
    public void init(boolean forEncryption, CipherParameters param) {
        if (core == null) {
            core = new SRACoreEngine();
        }
        core.init(forEncryption, param);
    }

    public int getInputBlockSize() {
        return core.getInputBlockSize();
    }

    public int getOutputBlockSize() {
        return core.getOutputBlockSize();
    }

    /**
     * Process a single block using the basic SRA algorithm.
     *
     * @param in the input array.
     * @param inOff the offset into the input buffer where the data starts.
     * @param inLen the length of the data to be processed.
     * @return the result of the RSA process.
     * @exception DataLengthException the input block is too large.
     */
    public byte[] processBlock(
            byte[]  in, 
            int     inOff, 
            int     inLen)
    {
        if (core == null) {
            throw new IllegalStateException("SRA engine not initialised");
        }
        return core.convertOutput(core.processBlock(core.convertInput(in, inOff, inLen)));
    }


}
