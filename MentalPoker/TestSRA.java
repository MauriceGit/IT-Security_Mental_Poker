import java.math.BigInteger;

public class TestSRA {
    public static void main(String[] args) {
        
        // Alice und Bob einigen sich auf ein p und ein q:
        BigInteger  p = new BigInteger("f75e80839b9b9379f1cf1128f321639757dba514642c206bbbd99f9a4846208b3e93fbbe5e0527cc59b1d4b929d9555853004c7c8b30ee6a213c3d1bb7415d03", 16);
        BigInteger  q = new BigInteger("b892d9ebdbfc37e397256dd8a5d3123534d1f03726284743ddc6be3a709edb696fc40c7d902ed804c6eee730eee3d5b20bf6bd8d87a296813c87d3b3cc9d7947", 16);
        
        CoinFlipping alice = new CoinFlipping(p, q);
        CoinFlipping bob   = new CoinFlipping(p, q);
        
        // Alice macht die Challenge:
        bob.checkResult(alice.solveChallenge(bob.getChallenge(alice.createChallenge())));        
    }

}
