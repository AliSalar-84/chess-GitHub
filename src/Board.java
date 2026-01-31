import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

@SuppressWarnings("serial")
public class Board extends JPanel implements MouseListener, MouseMotionListener {

	private final Square[][] board;
    private final GameWindow g;

    public final LinkedList<Piece> blackPieces;
    public final LinkedList<Piece> whitePiece;
    public List<Square> movable;
    
    private boolean whiteTurn;

    private Piece currentPiece;
    private int currentX, currentY;
    
    private CheckmateDetector cmd;
    
    public Board(GameWindow g) {
        this.g = g;
        board = new Square[8][8];
        blackPieces = new LinkedList<Piece>();
        whitePiece = new LinkedList<Piece>();
        setLayout(new GridLayout(8, 8, 0, 0));

        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                int xMod = x % 2;
                int yMod = y % 2;

                if ((xMod == 0 && yMod == 0) || (xMod == 1 && yMod == 1)) {
                    board[x][y] = new Square(this, 1, y, x);
                    this.add(board[x][y]);
                } else {
                    board[x][y] = new Square(this, 0, y, x);
                    this.add(board[x][y]);
                }
            }
        }

        initializePieces();

        this.setPreferredSize(new Dimension(400, 400));
        this.setMaximumSize(new Dimension(400, 400));
        this.setMinimumSize(this.getPreferredSize());
        this.setSize(new Dimension(400, 400));

        whiteTurn = true;

    }

    private void initializePieces() {
    	
        for (int x = 0; x < 8; x++) {
            board[1][x].put(new Pawn(0, board[1][x], PiecesConstants.BLACK_PAWN));
            board[6][x].put(new Pawn(1, board[6][x], PiecesConstants.WHITE_PAWN));
        }
        
        board[7][3].put(new Queen(1, board[7][3], PiecesConstants.WHITE_QUEEN));
        board[0][3].put(new Queen(0, board[0][3], PiecesConstants.BLACK_QUEEN));
        
        Shah blackShah = new Shah(0, board[0][4], PiecesConstants.BLACK_SHAH);
        Shah whiteShah = new Shah(1, board[7][4], PiecesConstants.WHITE_SHAH);
        board[0][4].put(blackShah);
        board[7][4].put(whiteShah);

        board[0][0].put(new Castle(0, board[0][0], PiecesConstants.BLACK_CASTLE));
        board[0][7].put(new Castle(0, board[0][7], PiecesConstants.BLACK_CASTLE));
        board[7][0].put(new Castle(1, board[7][0], PiecesConstants.WHITE_CASTLE));
        board[7][7].put(new Castle(1, board[7][7], PiecesConstants.WHITE_CASTLE));

        board[0][1].put(new Knight(0, board[0][1], PiecesConstants.BLACK_KNIGHT));
        board[0][6].put(new Knight(0, board[0][6], PiecesConstants.BLACK_KNIGHT));
        board[7][1].put(new Knight(1, board[7][1], PiecesConstants.WHITE_KNIGHT));
        board[7][6].put(new Knight(1, board[7][6], PiecesConstants.WHITE_KNIGHT));

        board[0][2].put(new Bishop(0, board[0][2], PiecesConstants.BLACK_BISHOP));
        board[0][5].put(new Bishop(0, board[0][5], PiecesConstants.BLACK_BISHOP));
        board[7][2].put(new Bishop(1, board[7][2], PiecesConstants.WHITE_BISHOP));
        board[7][5].put(new Bishop(1, board[7][5], PiecesConstants.WHITE_BISHOP));
        
        
        for(int y = 0; y < 2; y++) {
            for (int x = 0; x < 8; x++) {
                blackPieces.add(board[y][x].getOccupyingPiece());
                whitePiece.add(board[7-y][x].getOccupyingPiece());
            }
        }
        
        cmd = new CheckmateDetector(this, whitePiece, blackPieces, whiteShah, blackShah);
    }

    public Square[][] getSquareArray() {
        return this.board;
    }

    public boolean getTurn() {
        return whiteTurn;
    }

    public void setCurrentPiece(Piece p) {
        this.currentPiece = p;
    }

    public Piece getCurrentPiece() {
        return this.currentPiece;
    }

    @Override
    public void paintComponent(Graphics g) {
        // super.paintComponent(g);

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Square sq = board[y][x];
                sq.paintComponent(g);
            }
        }

        if (currentPiece != null) {
            if ((currentPiece.getColor() == 1 && whiteTurn)
                    || (currentPiece.getColor() == 0 && !whiteTurn)) {
                final Image i = currentPiece.getImage();
                g.drawImage(i, currentX, currentY, null);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        currentX = e.getX();
        currentY = e.getY();

        Square sq = (Square) this.getComponentAt(new Point(e.getX(), e.getY()));

        if (sq.isOccupied()) {
            currentPiece = sq.getOccupyingPiece();
            if (currentPiece.getColor() == 0 && whiteTurn)
                return;
            if (currentPiece.getColor() == 1 && !whiteTurn)
                return;
            sq.setDisplay(false);
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Square sq = (Square) this.getComponentAt(new Point(e.getX(), e.getY()));

        if (currentPiece == null)
            return;

        if ((currentPiece.getColor() == 0 && whiteTurn) || (currentPiece.getColor() == 1 && !whiteTurn)) {
            return;
        }

        cmd.update();

        List<Square> legalMoves = currentPiece.getLegalMoves(this);
        movable = cmd.getAllowableSquares(whiteTurn);


        if (legalMoves.contains(sq) && movable.contains(sq) && cmd.testMove(currentPiece, sq)) {

            currentPiece.move(sq);

            cmd.update();

            if (cmd.blackCheckMated()) {
                currentPiece = null;
                repaint();
                this.removeMouseListener(this);
                this.removeMouseMotionListener(this);
                g.checkmateOccurred(0);
            } else if (cmd.whiteCheckMated()) {
                currentPiece = null;
                repaint();
                this.removeMouseListener(this);
                this.removeMouseMotionListener(this);
                g.checkmateOccurred(1);
            } else {

                currentPiece = null;
                whiteTurn = !whiteTurn;
                movable = cmd.getAllowableSquares(whiteTurn);
            }

        } else {

            currentPiece.getPosition().setDisplay(true);
            currentPiece = null;
        }

        repaint();
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        currentX = e.getX() - 24;
        currentY = e.getY() - 24;

        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}