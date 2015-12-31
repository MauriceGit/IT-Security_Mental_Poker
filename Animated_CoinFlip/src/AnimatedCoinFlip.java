

public class AnimatedCoinFlip {
    
    public static void main(String[] args) {
        
        CoinFlipThread coinFlip = new CoinFlipThread(false, true);
        AnimationThread animation = new AnimationThread();
        
        Thread coinFlipThread = new Thread(coinFlip);
        Thread animationThread = new Thread(animation);
        
        coinFlipThread.start();
        animationThread.start();
        
        try {
            coinFlipThread.join();
        } catch (InterruptedException e) {
            System.out.println("The main Thread got interrupted... maybe?");
        }
        
        System.out.println("Coin-Flip has finished...");
        
        animation.setWinningState(coinFlip.isWon());
        animation.setFinishedState(coinFlip.isFinished());

    }

}
