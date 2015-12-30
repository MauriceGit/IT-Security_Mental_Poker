
public class Camera {
    
    private final float tX = 0;
    private final float tY = 20;
    private final float tZ = 0;
    
    private boolean finishedRotating;
    private boolean startRotating;
    private float x, y, z;    
    
    public Camera() {
        this.x = -54;
        this.y = 0;
        this.z = -60;
        finishedRotating = false;
        startRotating = false;
    }
    
    public void animate(Coin coin, float interval) {
        if (coin.isCoinAnimationFinished() && !finishedRotating) {
            startRotating = true;
            float xDiff = tX-x;
            float yDiff = tY-y;
            float zDiff = tZ-z;
            if ((xDiff+yDiff+zDiff) > 1) {
                this.x = x + xDiff*interval*0.1f;
                this.y = y + yDiff*interval*0.1f;
                this.z = z + zDiff*interval*0.1f;
            } else {
                finishedRotating = true;
            }
        }
    }

    public boolean isStartRotating() {
        return startRotating;
    }

    public boolean isFinishedRotating() {
        return finishedRotating;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

}
