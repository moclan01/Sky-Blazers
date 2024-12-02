package view;

import Fonts.CFont;
import Fonts.CharInfo;
import background.TextureData;
import background.TextureLoad;
import controller.MoveController;
import draw.DrawPlaneEnemy;
import draw.DrawPlanePlayer;
import model.Bullet;
import model.HealthBar;
import model.Jet;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import shader.Batch;
import shader.Shader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnd;

public class OpenGLRenderer {
    public static long window;
    public static int windowWidth = 600;
    public static int windowHeight = 1000;

    private CFont font;


    private List<TextureData> textureList = new ArrayList<>(); // Danh sách các tấm ảnh
    private int currentTextureIndex = 0; // Chỉ số của ảnh hiện tại

    private float offsetY = 0.0f; // Vị trí cuộn theo trục Y
    private float scrollSpeed = 0.05f; // Tốc độ cuộn

    public static Jet playerJet,enemyJet1, enemyJet2, enemyJet3, enemyJet4 ;
    public ArrayList<Jet> enemyJets1, enemyJets2;
    //  public float xPlayerJet, yPlayerJet;

    public static HealthBar playerHeathBar;

    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    public static Bullet playerBullet, enemyBullet;
    private ArrayList<Bullet> playerBullets;

    public  int size =0 ;
    public static boolean movingRight = true; // Di chuyen phai
    public static boolean movingLeft = true;

    public DrawPlanePlayer drawPlanePlayer;
    public DrawPlaneEnemy drawPlaneEnemy;
    public MoveController moveController;

    public boolean gameOver = false;
    Shader fontShader;
    Shader sdfShader;
    Batch batch;

    public void run() {
        init();
        loop();

        // Dọn dẹp tài nguyên
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    private void init() {
        drawPlanePlayer = new DrawPlanePlayer();
        drawPlaneEnemy = new DrawPlaneEnemy();
        //player
        playerJet = new Jet(300f,800f,0f,0f,0.15f,5,0,false,false,false,false);
        playerBullet = new Bullet(playerJet.getX()-100, playerJet.getY()-100, 10, 20,180, 0.15f);
        playerBullets = playerJet.getBullets();

        //enemy1
        enemyJet1 = new Jet(-300f,100f,0f,0f,0.1f,5,0,true,true,true,true);
        enemyJet2 = new Jet(800f,100f,0f,0f,0.1f,5,0,true,true,true,true);



        //enemy2
        enemyJet3 = new Jet(300f,-300f,0f,0f,0.15f,5,0,true,true,true,true);
        enemyJet4 = new Jet(600f,1500f,0f,0f,0.15f,5,0,true,true,true,true);

        enemyBullet = new Bullet(playerJet.getX(), playerJet.getY(), 10, 20,360, 0.20f);

        enemyJets1 = new ArrayList<Jet>();
        enemyJets1.add(enemyJet1);
        enemyJets1.add(enemyJet2);

        enemyJets2 = new ArrayList<Jet>();
        enemyJets2.add(enemyJet3);
        enemyJets2.add(enemyJet4);


        playerHeathBar = new HealthBar(playerJet.getX()+50,playerJet.getY(),playerJet.getHealth());
        moveController = new MoveController();
        // Khởi tạo GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Tạo cửa sổ
        window = GLFW.glfwCreateWindow(windowWidth, windowHeight, "Sky Blazers", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwShowWindow(window);

        // Khởi tạo OpenGL
        GL.createCapabilities();

        // Đặt màu nền
        GL11.glClearColor(1f, 1f, 1f, 1f);

        // Tải danh sách ảnh nền
        textureList.add(TextureLoad.loadTexture("./src/main/resources/images/background1.png"));
        textureList.add(TextureLoad.loadTexture("./src/main/resources/images/background2.png"));
        textureList.add(TextureLoad.loadTexture("./src/main/resources/images/background3.png"));
        textureList.add(TextureLoad.loadTexture("./src/main/resources/images/background4.png"));
        textureList.add(TextureLoad.loadTexture("./src/main/resources/images/background5.png"));
        textureList.add(TextureLoad.loadTexture("./src/main/resources/images/background6.png"));

        // Cấu hình viewport OpenGL
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, windowWidth, windowHeight, 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        for (int i = 0; i < 4; i++) {
            particles.add(new Particle(random));
        }
        if(gameOver) {
            System.out.println("hello");
            // Khởi tạo font và batch
            font = new CFont("./src/main/resources/fonts/Roboto-Regular.ttf", 64);
            fontShader = new Shader("./src/main/resources/shaders/fontShader.glsl");
            sdfShader = new Shader("./src/main/resources/shaders/sdfShader.glsl");
            batch = new Batch();
            batch.shader = fontShader;
            batch.sdfShader = sdfShader;
            batch.font = font;
            batch.initBatch();
        }
    }

    private void loop() {
        Shader fontShader = new Shader("./src/main/resources/shaders/fontShader.glsl");
        Shader sdfShader = new Shader("./src/main/resources/shaders/sdfShader.glsl");
        Batch batch = new Batch();
        batch.shader = fontShader;
        batch.sdfShader = sdfShader;
        batch.font = font;
        batch.initBatch();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        CharInfo oneQuad = new CharInfo(0, 0, 1, 1);
        oneQuad.calculateTextureCoordinates(1, 1);

        while (!GLFW.glfwWindowShouldClose(window)) {
            // Xóa màn hình
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            // Vẽ ảnh nền hiện tại
            renderScrollingBackground();

            if(playerJet.getHealth() >0){
                playerHeathBar.drawHealthBar();
                drawPlanePlayer.drawPlanePlayer(playerJet.getX(),playerJet.getY());
                for (Particle particle : particles) {
                    particle.update();
                    particle.render();
                }
                  processLogic(playerJet, 1,playerBullet);

                for (Bullet bullet : playerJet.getBullets()) {
                    bullet.drawBullet();
                }
                moveController.processMoveInput();



            for (int i = 0; i < enemyJets1.size(); i++) {
                Jet enemy = enemyJets1.get(i);
                 processLogic(enemy, 2,enemyBullet);

                if (i %2 == 0){
                     enemy.autoMove2(1);

                }else{
                      enemy.autoMove2(2);


                }
                calculator(enemy, i);
                if (enemy.getHealth() > 0) {
                    calculator2(enemy, playerJet);

                    for (Bullet bullet : enemy.getBullets()) {
                        bullet.drawBullet();
                    }
                    drawPlaneEnemy.drawPlaneEnemy(enemy.getX(), enemy.getY());
                } else {
                    enemyJets1.remove(i);
                    i--;
                }
            }
            if(enemyJets1.size() == 0){
                for (int i = 0; i < enemyJets2.size(); i++) {
                    Jet enemy = enemyJets2.get(i);
                      processLogic(enemy, 2,enemyBullet);

                    if (i %2 == 0){
                         enemy.autoMove2(1);
                    }else{
                         enemy.autoMove2(2);
                    }
                    calculator(enemy, i);
                    if (enemy.getHealth() > 0) {
                        calculator2(enemy, playerJet);

                        for (Bullet bullet : enemy.getBullets()) {
                            bullet.drawBullet();
                        }
                       drawPlaneEnemy.drawPlaneEnemy(enemy.getX(), enemy.getY());
                    } else {
                        enemyJets2.remove(i);
                        i--;
                    }
                }


            }

            if(enemyJets2.size() == 0){
                gameOver = true;
                System.out.println(gameOver);
                font = new CFont("./src/main/resources/fonts/Roboto-Regular.ttf", 64);
                fontShader = new Shader("./src/main/resources/shaders/fontShader.glsl");
                sdfShader = new Shader("./src/main/resources/shaders/sdfShader.glsl");
                batch = new Batch();
                batch.shader = fontShader;
                batch.sdfShader = sdfShader;
                batch.font = font;
                batch.initBatch();
                // Vẽ text "Game over" với nền trong suốt
                GL11.glEnable(GL11.GL_BLEND);  // Bật chế độ pha trộn
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);  // Chế độ pha trộn alpha (mặc định)
                // Vẽ text "Game over"
                batch.addText("You win!", 220, 400, 1.2f, 0x00FF00);
                batch.addText("Press esc to exit", 150, 310, 1.2f, 0x00FF00);
                batch.flushBatch();

                if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                    break; // Thoát vòng lặp nếu nhấn phím ESC
                }

                // Nếu muốn, bạn cũng có thể kiểm tra trạng thái cửa sổ
                if (glfwWindowShouldClose(window)) {
                    break; // Nếu cửa sổ bị đóng
                }
            }

            }else{
                gameOver = true;
                System.out.println(gameOver);
                font = new CFont("./src/main/resources/fonts/Roboto-Regular.ttf", 64);
                fontShader = new Shader("./src/main/resources/shaders/fontShader.glsl");
                sdfShader = new Shader("./src/main/resources/shaders/sdfShader.glsl");
                batch = new Batch();
                batch.shader = fontShader;
                batch.sdfShader = sdfShader;
                batch.font = font;
                batch.initBatch();

                GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Thiết lập màu nền đen
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT); // Xóa màn hình với màu nền đen
                    // Vẽ text "Game over" với nền trong suốt
                    GL11.glEnable(GL11.GL_BLEND);  // Bật chế độ pha trộn
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);  // Chế độ pha trộn alpha (mặc định)
                    // Vẽ text "Game over"
                    batch.addText("Game over", 220, 400, 1.2f, 0x00FF00);
                    batch.addText("Press esc to exit", 150, 310, 1.2f, 0x00FF00);
                    batch.flushBatch();

                if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
                    break; // Thoát vòng lặp nếu nhấn phím ESC
                }

                // Nếu muốn, bạn cũng có thể kiểm tra trạng thái cửa sổ
                if (glfwWindowShouldClose(window)) {
                    break; // Nếu cửa sổ bị đóng
                }
            }
            // Hoán đổi bộ đệm
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        System.exit(0);
    }

    private void renderScrollingBackground() {
        if (textureList.isEmpty()) return;

        // Lấy ảnh hiện tại và ảnh kế tiếp
        TextureData currentTexture = textureList.get(currentTextureIndex);
        TextureData nextTexture = textureList.get((currentTextureIndex + 1) % textureList.size());

        // Vẽ ảnh hiện tại
        renderImage(currentTexture.textureID, offsetY);

        // Tính toán tọa độ cho ảnh kế tiếp
        float nextImageOffsetY = offsetY - currentTexture.height;

        // Vẽ ảnh kế tiếp khi cần
        renderImage(nextTexture.textureID, nextImageOffsetY);

        // Cập nhật vị trí cuộn
        offsetY += scrollSpeed;

        // Chuyển sang ảnh kế tiếp khi ảnh hiện tại cuộn hết
        if (offsetY >= currentTexture.height) {
            offsetY -= currentTexture.height;
            currentTextureIndex = (currentTextureIndex + 1) % textureList.size();
        }
    }

    private void renderImage(int textureID, float yOffset) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        // Vẽ ảnh phù hợp với kích thước cửa sổ
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(0, yOffset);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(windowWidth, yOffset);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(windowWidth, yOffset + windowHeight);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(0, yOffset + windowHeight);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }
    public  class Particle {//Khoi
        float x, y, alpha, size;
        float speedX, speedY;
        float life;

        Particle(Random random) {
            reset(random);
        }

        void reset(Random random) {
            // Khởi tạo hạt từ phía trên
            x = playerJet.getX(); // Di chuyển trên toàn bộ chiều ngang
            y = playerJet.getY()+40; // Vị trí cao ở phía trên (tạo cảm giác khói từ trên xuống)
            alpha = 1.0f;
            size = random.nextFloat() * 8 + 10; // Tăng kích thước ban đầu (to hơn)
            speedX = random.nextFloat() * 0.01f - 0.005f; // Chuyển động ngang nhẹ
            speedY = 1f ; // Chuyển động xuống (giảm giá trị để chậm hơn)
            life = random.nextFloat() * 4.0f + 2.0f;       // Kéo dài thời gian sống
        }

        int updateInterval = 10; // Số khung hình giữa mỗi lần cập nhật
        int frameCounter = 0;

        void update() {
            frameCounter++;
            if (frameCounter >= updateInterval) {
                x += speedX; // Di chuyển ngang
                y += speedY; // Di chuyển xuống
                alpha -= 0.005f; // Giảm độ mờ
                size += 0.2f;   // Tăng kích thước
                life -= 0.015f; // Giảm thời gian sống

                if (life <= 0 || alpha <= 0) {
                    reset(new Random()); // Tái tạo hạt mới khi hết thời gian sống
                }

                frameCounter = 0; // Reset bộ đếm
            }
        }

        void render() {
            // Màu sắc xám và độ mờ giảm dần
            glColor4f(0.3f, 0.3f, 0.3f, alpha); // Màu xám
            glPointSize(size);
            glBegin(GL_POINTS);
            glVertex2f(x, y);
            glEnd();
        }
    }

    public void drawBullet(float x, float y) {
        glBegin(GL_QUADS);
        glColor3f(1.0f, 0.0f, 0.0f); // Màu đỏ
        glVertex2f(x - 10f, y - 10f);
        glVertex2f(x + 10f, y - 10f);
        glVertex2f(x + 10f, y + 10f);
        glVertex2f(x - 10f, y + 10f);
        glEnd();
    }
    private void processLogic(Jet jet, int i,Bullet bullets) {
        long currentTime = System.currentTimeMillis();
        float roation = 0;
        if(i == 1){
            roation = playerBullet.getRotation();
        }else{
            roation = enemyBullet.getRotation();
        }

        if(i == 1 ){
            if (currentTime - jet.getLastBulletTime() >= 3000) {
                jet.getBullets().add(new Bullet(jet.getX()-5, jet.getY()-90,bullets.getWidth(),bullets.getHeight(),roation ,bullets.getSpeed()));
                jet.setLastBulletTime(  currentTime);
            }

        }else{
            if (currentTime - jet.getLastBulletTime() >= 3000) {
                jet.getBullets().add(new Bullet(jet.getX()-5, jet.getY()+20,bullets.getWidth(),bullets.getHeight(),roation ,bullets.getSpeed()));
                jet.setLastBulletTime(  currentTime);
            }
        }
        // Bắn đạn mỗi 3 giây

        // Cập nhật tất cả các viên đạn
        for (Bullet bullet : jet.getBullets()) {
            bullet.update2(i);
        }

        // Xóa viên đạn ra khỏi màn hình
        jet.getBullets().removeIf(Bullet::isOutOfBounds2);
    }
    public void calculator(Jet enemyJet,int k){
        for (int i = 0; i < playerBullets.size(); i++) {
            Bullet bullet = playerBullets.get(i);
            float xEnemy = enemyJet.getX();
            float yEnemy = enemyJet.getY();
            float w = 150f;
            float h = 100f;

            if (Math.abs(bullet.getX() - xEnemy) < (0.02 / 2 + w / 2) &&
                    Math.abs(bullet.getY() - yEnemy) < (0.02 / 2 + h / 2)) {
                enemyJet.setHealth(enemyJet.getHealth()-1);
                playerBullets.remove(i);
               // System.out.println("Va chạm  " + enemyJet.getHealth());
            }
        }
        System.out.println(enemyJet.getHealth());
    }
    private void drawEnemyJet(float x, float y) {
        glBegin(GL_QUADS);
        glColor3f(0.0f, 0.5f, 1.0f); // Màu xanh
        glVertex2f(x - 30f, y - 30f);
        glVertex2f(x + 30f, y - 30f);
        glVertex2f(x + 30f, y + 30f);
        glVertex2f(x - 30f, y + 30f);
        glEnd();
    }
    public void calculator2(Jet jet,Jet enemyJet) {
        for (int i = 0; i < jet.getBullets().size(); i++) {
            Bullet bullet = jet.getBullets().get(i);
            float xEnemy = enemyJet.getX();
            float yEnemy = enemyJet.getY();
            float w = 200f;
            float h = 120f;

            if (Math.abs(bullet.getX() - xEnemy) < (0.02 / 2 + w / 2) &&
                    Math.abs(bullet.getY() - yEnemy) < (0.02 / 2 + h / 2)) {
                enemyJet.setHealth(enemyJet.getHealth() - 1);
                jet.getBullets().remove(i);
                playerHeathBar.decreaseHealth();

            }
        }
    }

}
