import CoinFlip.Client;


public class CoinFlipThread implements Runnable {
    
    private boolean isServer;
    private boolean useTLS;
    private Client coinFlip;
    
    public CoinFlipThread(boolean isServer, boolean useTLS) {
        this.coinFlip = new Client();
    }

    @Override
    public void run() {
       this.coinFlip.playCoinFlip(isServer, useTLS);        
    }

    public boolean isFinished() {
        return coinFlip.isFinished();
    }
    

    public boolean isWon() {
        return coinFlip.isWon();
    }
    
}
