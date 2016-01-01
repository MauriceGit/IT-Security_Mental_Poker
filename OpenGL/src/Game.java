import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.swing.JFrame;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

public class Game extends JFrame implements GLEventListener {
    private static final long serialVersionUID = 1L;

    private GLU glu;
    private float distance;
    final private int width = 1080;
    final private int height = 1080;
    private Texture textureUp;
    private Texture textureDown;
    private long lastTime;
    private Coin coin;
    private Camera camera;
    
    private float rotation = 0.0f;
    private float descent = 1.0f;

    private MutableBool winningState = new MutableBool(true);
    
    private MutableBool coinFlipIsFinished = new MutableBool(false);
    private boolean texturesChangedOrGood;
    private boolean switchTextures;

    private TextRenderer textRenderer;


    public synchronized void setiWon(boolean iWon) {
        winningState.setMutableBool(iWon);
    }

    public void setCoinFlipIsFinished(boolean coinFlipIsFinished) {
        this.coinFlipIsFinished.setMutableBool(coinFlipIsFinished);
    }

    public Game() {
    	super("Minimal OpenGL - yay");
    }

    private void drawCoin(GL2 gl) {
        float radius = 6.378f;
        float height = 1.0f;
        int slices = 256;

        gl.glDisable(GL2.GL_CULL_FACE);
        gl.glEnable(GL2.GL_DEPTH_TEST);

        gl.glEnable(GL2.GL_TEXTURE_2D);
        if (!switchTextures) {
            textureUp.enable(gl);
            textureUp.bind(gl);
        } else {
            textureDown.enable(gl);
            textureDown.bind(gl);
        }
        GLUquadric coinUp = glu.gluNewQuadric();
        glu.gluQuadricTexture(coinUp, true);
        glu.gluQuadricDrawStyle(coinUp, GLU.GLU_FILL);
        // glu.gluQuadricDrawStyle(coinUp, GLU.GLU_LINE);
        glu.gluQuadricNormals(coinUp, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(coinUp, GLU.GLU_OUTSIDE);
        gl.glPushMatrix();
        gl.glRotatef(180, 0, 1, 0);
        glu.gluDisk(coinUp, 0, radius, slices, 2);
        gl.glPopMatrix();
        glu.gluDeleteQuadric(coinUp);

        gl.glDisable(GL2.GL_TEXTURE_2D);
        float[] specColor = { 1.0f, 0.84f, 0.0f, 1f };
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, specColor, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, specColor, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 0.99f);

        // Draw sphere (possible styles: FILL, LINE, POINT).
        GLUquadric coin = glu.gluNewQuadric();
        glu.gluQuadricTexture(coin, false);
        glu.gluQuadricDrawStyle(coin, GLU.GLU_FILL);
        // glu.gluQuadricDrawStyle(coin, GLU.GLU_LINE);
        glu.gluQuadricNormals(coin, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(coin, GLU.GLU_OUTSIDE);
        glu.gluCylinder(coin, radius, radius, height, slices, 1);
        glu.gluDeleteQuadric(coin);

        gl.glEnable(GL2.GL_TEXTURE_2D);
        if (!switchTextures) {
            textureDown.enable(gl);
            textureDown.bind(gl);
        } else {
            textureUp.enable(gl);
            textureUp.bind(gl);
        }
        GLUquadric coinDown = glu.gluNewQuadric();
        glu.gluQuadricTexture(coinDown, true);
        glu.gluQuadricDrawStyle(coinDown, GLU.GLU_FILL);
        // glu.gluQuadricDrawStyle(coinDown, GLU.GLU_LINE);
        glu.gluQuadricNormals(coinDown, GLU.GLU_FLAT);
        glu.gluQuadricOrientation(coinDown, GLU.GLU_OUTSIDE);
        gl.glPushMatrix();
        gl.glTranslatef(0, 0, height);
        // gl.glRotatef(180, 1, 0, 0);
        glu.gluDisk(coinDown, 0, radius, slices, 2);
        gl.glPopMatrix();
        glu.gluDeleteQuadric(coinDown);

    }

    private void drawAnimatedCoin(GL2 gl, float interval) {
        gl.glPushMatrix();
        gl.glTranslatef(coin.getX(), coin.getY(), coin.getZ());
        // gl.glRotatef(coin.getAngle(), 1.0f, 0.0f, 3.4f);
        gl.glRotatef(coin.getAngle(), 0.0f, 0.0f, 1.0f);
        gl.glRotatef(rotation%360, 0, 1, 0);
        gl.glRotatef(90f, 1, 0, 0);
        drawCoin(gl);
        gl.glPopMatrix();
    }

    private void drawText(GL2 gl) {
        if (coin.isCoinAnimationFinished() && camera.isFinishedRotating()) {
            String text = winningState.getMutableBool() ? "You Won. Congrats."
                    : "You Lost. Better run now...";

            textRenderer.beginRendering(400, 400);
            textRenderer.setColor(Color.orange);
            textRenderer.draw(text, 100, 350);
            textRenderer.endRendering();

        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        long thisTime = System.currentTimeMillis();
        float interval = (thisTime - lastTime) / 60.0f;
        lastTime = thisTime;
        
        
        
        if (coin.isCoinAnimationFinished()) {
            rotation += interval*4.5 / descent;
            descent += interval / 4.0f;
        } else {
            rotation += interval*4.5f;
        }

        setLight(gl);
        setCamera(gl, distance);

        coin.animate(interval, coinFlipIsFinished.getMutableBool(), winningState.getMutableBool());
  
        if (coinFlipIsFinished.getMutableBool()) {
            camera.animate(coin, interval);
        }

        drawAnimatedCoin(gl, interval);

        drawText(gl);

        gl.glFlush();

    }

    private void setLight(GL2 gl) {
        // Prepare light parameters.
        float SHINE_ALL_DIRECTIONS = 1;
        float[] lightPos = { 18, 7, 5, SHINE_ALL_DIRECTIONS };
        float[] lightColorAmbient = { 0.2f, 0.2f, 0.2f, 1f };
        float[] lightColorSpecular = { 0.8f, 0.8f, 0.8f, 1f };

        // Set light parameters.
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightColorAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, lightColorSpecular, 0);

        // Enable lighting in GL2.
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_LIGHTING);
    }

    private void setCamera(GL2 gl, float distance) {
        // Change to projection matrix.
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        // Perspective.
        float widthHeightRatio = (float) getWidth() / (float) getHeight();
        glu.gluPerspective(90.0, widthHeightRatio, 1, 1000);
        glu.gluLookAt(camera.getX(), camera.getY(), camera.getZ(), // eye
                0, 0, 0, // pos
                0, 1, 0); // up

        // Change back to model view matrix.
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0f, 0f, 0f, 1f);
        glu = new GLU();
        distance = (float) 30.0;
        File head = new File("panda.jpg");
        File tail = new File("game_over.png");
        if (textureUp == null && head != null) {
            try {
                textureUp = TextureIO.newTexture(head, true);
            } catch (Exception e) {
                System.out.println("shit.");
            }
        }
        if (textureDown == null && tail != null) {
            try {
                textureDown = TextureIO.newTexture(tail, true);
            } catch (Exception e) {
                System.out.println("shit.");
            }
        }
        lastTime = System.currentTimeMillis();
        textRenderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 12));

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width,
            int height) {
    }

    public void play() {
    	
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities capabilities = new GLCapabilities(profile);
        GLCanvas canvas = new GLCanvas(capabilities);
        canvas.addGLEventListener(this);
        final Animator animator = new Animator(canvas);

        this.setName("Minimal OpenGL - yay");
        this.getContentPane().add(canvas);

        coin = new Coin();
        camera = new Camera();
        texturesChangedOrGood = false;
        switchTextures = false;

        this.setSize(width, height);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setResizable(false);
        canvas.requestFocusInWindow();
        animator.start();
    }
}