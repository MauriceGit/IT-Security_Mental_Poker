
public class AnimationThread implements Runnable {
    
    private Game game;
    
    public AnimationThread() {
        this.game = new Game();
    }

    @Override
    public void run() {
        this.game.play();
    }

    public void setWinningState(boolean isWon) {
        game.setiWon(isWon);
    }
    
    public void setFinishedState(boolean coinFlipIsFinished) {
        game.setCoinFlipIsFinished(coinFlipIsFinished);
    }
    
}
