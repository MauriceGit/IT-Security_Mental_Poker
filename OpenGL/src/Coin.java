public class Coin {

    private final float k_v = 1.5f;
    private float g = -9.81f;
    private final float mass = 10.1f;

    private int bounced;
    private boolean calmDown;
    private boolean coinAnimationFinished;

    // Alle Bewegungen gelten ausschließlich für die Y-Achse (oben/unten).
    private float a, v;

    private float x, y, z;
    private float angle;

    private float bounceHeight;
    private float rotationSpeed;

    public Coin() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.angle = 0;
        this.a = calcA();        
        this.y = 0;
        bounced = 0;
        bounceHeight = 15;
        this.v = bounceHeight;
        rotationSpeed = 25;
        calmDown = false;
        coinAnimationFinished = false;
    }

    private float calcA() {
        // f = m * a; a = f/m
        return g / mass;
    }

    private float calcV(float interval) {
        // v = v + a*t
        return v + a * interval * k_v;
    }

    private float calcP(float p, float interval) {
        // p = p + v*t
        return p + v * interval;
    }
    
    private boolean isNearlyFlat (boolean won) {
        if (won) {
            //return Math.abs(180-(angle%180)) <= 2;
            return Math.abs(360-(angle)) <= 2 || angle <= 2;
        } else {
            return Math.abs(180-(angle)) <= 2;
        }
    }
        
    public void animate(float interval, boolean coinFlipFinished, boolean won) {
        angle = (angle + (interval * rotationSpeed)) % 360;
                
        a = calcA();
        v = calcV(interval / 2.0f);
        
        if (!calmDown && bounced >= 3 && coinFlipFinished && isNearlyFlat(won)) {
            calmDown = true;
            rotationSpeed -= rotationSpeed;
        }
        
        if (calmDown) {
            bounceHeight = 0;
            rotationSpeed /= 1.2f;
            v /= 1.2f;
            g *= 1.1f;
        }
       
        y = calcP(y, interval / 2.0f);
            
        if (y < 0) {
            y = 0;
            v = bounceHeight;
            if (bounced < 3) {
                bounceHeight /= 1.2f;
                rotationSpeed /= 1.2f;
            }
                        
            bounced++;
            
            if (calmDown && rotationSpeed < 0.1) {
                coinAnimationFinished = true;
            }
        }

    }

    public boolean isCoinAnimationFinished() {
        return coinAnimationFinished;
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

    public float getAngle() {
        return angle;
    }

}
