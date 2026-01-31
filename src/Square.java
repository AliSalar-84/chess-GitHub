import java.awt.Color;
import java.awt.Graphics;

import javax.swing.*;

@SuppressWarnings("serial")
public class Square extends JComponent {

    private Board b;
    private final int color;
    private Piece occupyingPiece;
    private boolean displayPiece;
    private final int xNum, yNum;
    
    public Square(Board b, int c, int xNum, int yNum) {
        
        this.b = b;
        this.color = c;
        this.displayPiece = true;
        this.xNum = xNum;
        this.yNum = yNum;
        
        
        this.setBorder(BorderFactory.createEmptyBorder());
    }
    
    public int getColor() {
        return this.color;
    }
    
    public Piece getOccupyingPiece() {
        return occupyingPiece;
    }
    
    public boolean isOccupied() {
        return (this.occupyingPiece != null);
    }
    
    public int getXNum() {
        return this.xNum;
    }
    
    public int getYNum() {
        return this.yNum;
    }
    
    public void setDisplay(boolean v) {
        this.displayPiece = v;
    }
    
    public void put(Piece p) {
        this.occupyingPiece = p;
        p.setPosition(this);
    }
    
    public Piece removePiece() {
        Piece p = this.occupyingPiece;
        this.occupyingPiece = null;
        return p;
    }
    
    public void capture(Piece p) {
        Piece k = getOccupyingPiece();
        if (k.getColor() == 0) b.blackPieces.remove(k);
        if (k.getColor() == 1) b.whitePiece.remove(k);
        this.occupyingPiece = p;
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor((color == 1) ? new Color(238, 238, 210) :
                new Color(118, 150, 86));
        
        g.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        
        if(occupyingPiece != null && displayPiece) {
            occupyingPiece.draw(g);
        }
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + xNum;
        result = prime * result + yNum;
        return result;
    }
    
}
