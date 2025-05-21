import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private final GameBoard board;
    private final Timer timer;

    private boolean isPaused = false;

    public GamePanel (int rows, int cols, int tileSize){
        setPreferredSize(new Dimension(cols * tileSize, rows * tileSize));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this); 
        board = new GameBoard(rows, cols, tileSize);
        timer = new Timer(50,this);
        timer.start();

    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        board.draw(g);
    }

    @Override
    public void actionPerformed(ActionEvent e){
        if(!isPaused){
        board.update();
        repaint();
        }
        if (board.isGameOver()) timer.stop();
    }

    @Override
    public void keyPressed(KeyEvent e){
        if(e.getKeyCode() == KeyEvent.VK_P){
        gamePause();
        }

        board.handleKeyPress(e);
    }

    private void gamePause(){
        isPaused = ! isPaused;
        if(isPaused){
            timer.stop();
        }
        else{
            timer.start();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
