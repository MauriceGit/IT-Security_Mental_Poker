import CoinFlip.Client;


public class CoinFlipThread implements Runnable {
    
    private Client coinFlip;
    
    public CoinFlipThread() {
        this.coinFlip = new Client();
    }

    @Override
    public void run() {
       this.coinFlip.playCoinFlip();        
    }

    public boolean isFinished() {
        return coinFlip.isFinished();
    }
    

    public boolean isWon() {
        return coinFlip.isWon();
    }
    
}
