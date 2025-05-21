import java.awt.*;
import java.awt.event.KeyEvent;
import  java.util.*;

public class GameBoard {

    private final int rows, cols, tileSize;
    private final Set<GameEntity> walls = new HashSet<>();
    private final Set<GameEntity> foods = new HashSet<>();
    private final Set<Ghost> ghosts = new HashSet<>();
    private final PacMan pacman;

    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean scareMode = false;
    private boolean levelComplete = false;
    private int scaredTimer = 0;


   
    private GameEntity bonusFood = null;
    private int bonusTimer = 0;
    private final int BONUS_INTERVAL = 300;  
    private final int BONUS_DURATION = 180;  
    private final int BONUS_SCORE = 100;


    private int currentLevel = 1;
    private float ghostSpeedMultiplier = 1.0f;

    
    private final String[] layout1 = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X  s     X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "X   X  r    X X   X",
        "X X X XX XX X X X X",
        "X       P         X",
        "X X X XXXXX X X X X",
        "X   X       X X   X",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     p     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX"
    };

private final String[] layout2 = {
    "XXXXXXXXXXXXXXXXXXX",
    "X p   X   s  X   pX",
    "X XX XX XXX XX XX X",
    "X   X    X    X   X",
    "XXX X XXXXXXX X XXX",
    "X   X    X     X  X",
    "X XXXXX XX XX XXX X",
    "X     X  r X    X X",
    "X XXX XXXXXX XX X X",
    "X X   X P     X   X",
    "X X XXXXXXX XX XXX X",
    "X   X   X     X   X",
    "X XXXXX X XXX X X X",
    "X   X   X X   X X X",
    "X X X XXXXX X X X X",
    "X X X   p   X X X X",
    "X X XXX XXXXX XXX X",
    "X   X    X    X   X",
    "XXX XXXXXXXXXXX XXX",
    "X   s     r    p  X",
    "XXXXXXXXXXXXXXXXXXX"
};


private String[] currentLayout;


    public GameBoard(int rows, int cols, int tileSize){
        this.rows = rows;
        this.cols = cols;
        this.tileSize = tileSize;
        this.currentLayout = layout1;
        pacman = new PacMan(tileSize, tileSize, tileSize);
        loadMap();
    }

    public void loadMap(){

        walls.clear();
        foods.clear();
        ghosts.clear();

        for (int r = 0; r < rows; r++){
            for (int c = 0; c < cols; c++){
                char ch = currentLayout[r].charAt(c);
                int x = c * tileSize;
                int y = r * tileSize;

                if (ch == 'X') {
                    GameEntity wall = new GameEntity(x, y, tileSize);
                    wall.setImage("images/wall.png");
                    walls.add(wall);
                }
                else if ( ch == ' ') {
                    GameEntity food = new GameEntity(x + (tileSize - 8) / 2, y + (tileSize - 8) / 2, 8);
                    food.setImage("images/food.png");
                    foods.add(food);
                }
                else if (ch == 'P') {
                    pacman.setPosition(x, y);
                }
                else if (ch == 'r') {
                    ghosts.add(new Ghost(x, y, tileSize, Ghost.GhostColor.RED));
                }
                else if (ch == 'p') {
                    ghosts.add(new Ghost(x, y, tileSize, Ghost.GhostColor.PINK));
                }
                else if (ch == 's') {
                    ghosts.add(new Ghost(x, y, tileSize, Ghost.GhostColor.SCARED));
                }
            }
        }
    }



      private void loadNextLevel() {
        currentLevel++;
        ghostSpeedMultiplier += 0.2f;
        
      
        currentLayout = (currentLevel % 2 == 0) ? layout2 : layout1;
        loadMap(); 
        
   
        for (Ghost ghost : ghosts) {
            ghost.setSpeed(2 * ghostSpeedMultiplier);
        }
    }






    public void draw(Graphics g){
        g.setColor(Color.BLACK);
        g.fillRect( 0, 0, cols * tileSize, rows * tileSize);

        for (GameEntity wall : walls) wall.draw(g);
        for (GameEntity food : foods) food.draw(g);
        for (Ghost ghost : ghosts) ghost.draw(g);
        pacman.draw(g);


                if (bonusFood != null) {
            bonusFood.draw(g);
        }

        g.setColor(Color.BLUE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("Lives: " + lives + " Score: " + score, 10, 20);

        g.drawString("Level: " + currentLevel, cols * tileSize / 2 - 30, 20);

        if (scareMode) {
            g.drawString("Scared: " + (scaredTimer / 20), cols * tileSize - 100, 20);
        }

        if (gameOver){
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("GAME OVER", cols * tileSize / 2 - 80, rows * tileSize / 2);
        }
        else if (levelComplete) {
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("LEVEL COMPLETE!", cols * tileSize / 2 - 120, rows * tileSize / 2);
        }

    }



    public void update() {
        if (gameOver || levelComplete) return;

        if (scareMode){
            scaredTimer --;
            if (scaredTimer <= 0){
                scareMode = false;
                for (Ghost ghost : ghosts){
                    ghost.setScared(false);
                }
            }

        }

        pacman.move();
        for( Ghost ghost : ghosts) ghost.move(walls);

        for (GameEntity wall : walls) {
            if (pacman.collidesWith(wall)){
                pacman.undo();
                break;
            }
        }


        for (Ghost ghost : ghosts){
            if (pacman.collidesWith(ghost)){
                if (scareMode){
                    ghost.reset();
                    score += 200;
                }
                else{
                   // lives --;
                   resetPositions();
                    if (--lives <= 0) gameOver = true;
                    break;
                }
                
            }
        }




        if (!levelComplete && !gameOver) {
   
            if (bonusFood == null && bonusTimer++ > BONUS_INTERVAL && !foods.isEmpty()) {
                createBonusFood();
                bonusTimer = 0;
            }
       
            else if (bonusFood != null && bonusTimer++ > BONUS_DURATION) {
                bonusFood = null;
                bonusTimer = 0;
            }
        }


        if (bonusFood != null && pacman.collidesWith(bonusFood)) {
            score += BONUS_SCORE;
            bonusFood = null;
        }

     
        GameEntity foodEaten = null;
        for (GameEntity food : foods) {
            if (pacman.collidesWith(food)) {
                foodEaten = food;
                score += 10;
                break;
            }
        }
        if (foodEaten != null) foods.remove(foodEaten);

        if (foods.isEmpty()) {
       
            loadNextLevel();
        }


        

}
    

    private void createBonusFood() {
    
        int randomIndex = new Random().nextInt(foods.size());
        GameEntity regularFood = new ArrayList<>(foods).get(randomIndex);
        
     
        bonusFood = new GameEntity(
            regularFood.x - 4,  
            regularFood.y - 4,
            16  
        ) {
            @Override
            public void draw(Graphics g) {
              
                if ((bonusTimer / 10) % 2 == 0) { 
                    g.setColor(Color.ORANGE);
                    g.fillOval(x, y, size, size);
                    g.setColor(Color.RED);
                    g.drawOval(x, y, size, size);
                }
            }
        };
        
    
        foods.remove(regularFood);
    }

        


    



    public void activateScareMode(){
        scareMode = true;
        scaredTimer = 400;
        for(Ghost ghost : ghosts){
            ghost.setScared(true);
        }
    }



    public void handleKeyPress(KeyEvent e){
        if ( levelComplete) {
            levelComplete = false;
            loadMap();
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> pacman.setDirection('U');
            case KeyEvent.VK_DOWN -> pacman.setDirection('D');
            case KeyEvent.VK_LEFT -> pacman.setDirection('L');
            case KeyEvent.VK_RIGHT -> pacman.setDirection('R');
            case KeyEvent.VK_SPACE -> activateScareMode();
        }
    }


    public void resetPositions(){
        pacman.reset();
        for (Ghost ghost : ghosts) ghost.reset();
    }
    

    public boolean isGameOver(){
        return gameOver;
    }
    
}
