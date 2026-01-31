import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;

public class CheckmateDetector {
    private Board b;
    private LinkedList<Piece> whitePieces;
    private LinkedList<Piece> blackPieces;
    private LinkedList<Square> movableSquares;
    private final LinkedList<Square> squares;
    private Shah blackShah;
    private Shah whiteShah;
    private HashMap<Square, List<Piece>> whiteMoves;
    private HashMap<Square, List<Piece>> blackMoves;

    public CheckmateDetector(Board b, LinkedList<Piece> whitePieces,
                             LinkedList<Piece> blackPieces, Shah whiteShah, Shah blackShah) {
        this.b = b;
        this.whitePieces = whitePieces;
        this.blackPieces = blackPieces;
        this.blackShah = blackShah;
        this.whiteShah = whiteShah;

        squares = new LinkedList<>();
        movableSquares = new LinkedList<>();
        whiteMoves = new HashMap<>();
        blackMoves = new HashMap<>();

        Square[][] brd = b.getSquareArray();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                squares.add(brd[y][x]);
                whiteMoves.put(brd[y][x], new LinkedList<Piece>());
                blackMoves.put(brd[y][x], new LinkedList<Piece>());
            }
        }

    }

    public void update() {

        Iterator<Piece> wIter = whitePieces.iterator();
        Iterator<Piece> bIter = blackPieces.iterator();

        for (List<Piece> pieces : whiteMoves.values()) {
            pieces.removeAll(pieces);
        }

        for (List<Piece> pieces : blackMoves.values()) {
            pieces.removeAll(pieces);
        }

        movableSquares.removeAll(movableSquares);

        while (wIter.hasNext()) {
            Piece p = wIter.next();

            if (!p.getClass().equals(Shah.class)) {
                if (p.getPosition() == null) {
                    wIter.remove();
                    continue;
                }

                List<Square> mvs = p.getLegalMoves(b);
                for (Square mv : mvs) {
                    List<Piece> pieces = whiteMoves.get(mv);
                    pieces.add(p);
                }
            }
        }

        while (bIter.hasNext()) {
            Piece p = bIter.next();

            if (!p.getClass().equals(Shah.class)) {
                if (p.getPosition() == null) {
                    wIter.remove();
                    continue;
                }

                List<Square> mvs = p.getLegalMoves(b);
                Iterator<Square> iter = mvs.iterator();
                while (iter.hasNext()) {
                    List<Piece> pieces = blackMoves.get(iter.next());
                    pieces.add(p);
                }
            }
        }
    }

    public boolean blackInCheck() {
        update();
        Square sq = blackShah.getPosition();
        if (whiteMoves.get(sq).isEmpty()) {
            movableSquares.addAll(squares);
            return false;
        } else return true;
    }

    public boolean whiteInCheck() {
        update();
        Square sq = whiteShah.getPosition();
        if (blackMoves.get(sq).isEmpty()) {
            movableSquares.addAll(squares);
            return false;
        } else return true;
    }

    public boolean blackCheckMated() {
        boolean checkmate = true;

        if (!this.blackInCheck()) return false;

        if (canEvade(whiteMoves, blackShah)) checkmate = false;

        List<Piece> threats = whiteMoves.get(blackShah.getPosition());
        if (canCapture(blackMoves, threats, blackShah)) checkmate = false;

        if (canBlock(threats, blackMoves, blackShah)) checkmate = false;

        return checkmate;
    }

    public boolean whiteCheckMated() {
        boolean checkmate = true;

        if (!this.whiteInCheck()) return false;

        if (canEvade(blackMoves, whiteShah)) checkmate = false;

        List<Piece> threats = blackMoves.get(whiteShah.getPosition());
        if (canCapture(whiteMoves, threats, whiteShah)) checkmate = false;

        if (canBlock(threats, whiteMoves, whiteShah)) checkmate = false;

        return checkmate;
    }

    private boolean canEvade(Map<Square, List<Piece>> tMoves, Shah tShah) {
        boolean evade = false;
        List<Square> shahMoves = tShah.getLegalMoves(b);
        Iterator<Square> iterator = shahMoves.iterator();

        while (iterator.hasNext()) {
            Square sq = iterator.next();
            if (!testMove(tShah, sq)) continue;
            if (tMoves.get(sq).isEmpty()) {
                movableSquares.add(sq);
                evade = true;
            }
        }

        return evade;
    }

    private boolean canCapture(Map<Square, List<Piece>> poss,
                               List<Piece> threats, Shah k) {

        boolean capture = false;
        if (threats.size() == 1) {
            Square sq = threats.get(0).getPosition();

            if (k.getLegalMoves(b).contains(sq)) {
                movableSquares.add(sq);
                if (testMove(k, sq)) {
                    capture = true;
                }
            }

            List<Piece> caps = poss.get(sq);
            ConcurrentLinkedDeque<Piece> capturers = new ConcurrentLinkedDeque<Piece>();
            capturers.addAll(caps);

            if (!capturers.isEmpty()) {
                movableSquares.add(sq);
                for (Piece p : capturers) {
                    if (testMove(p, sq)) {
                        capture = true;
                    }
                }
            }
        }

        return capture;
    }

    private boolean canBlock(List<Piece> threats,
                             Map<Square, List<Piece>> blockMoves, Shah sh) {
        boolean blockable = false;

        if (threats.size() == 1) {
            Square ts = threats.get(0).getPosition();
            Square shs = sh.getPosition();
            Square[][] brdArray = b.getSquareArray();

            if (shs.getXNum() == ts.getXNum()) {
                int max = Math.max(shs.getYNum(), ts.getYNum());
                int min = Math.min(shs.getYNum(), ts.getYNum());

                for (int i = min + 1; i < max; i++) {
                    List<Piece> blks =
                            blockMoves.get(brdArray[i][shs.getXNum()]);
                    ConcurrentLinkedDeque<Piece> blockers =
                            new ConcurrentLinkedDeque<Piece>();
                    blockers.addAll(blks);

                    if (!blockers.isEmpty()) {
                        movableSquares.add(brdArray[i][shs.getXNum()]);

                        for (Piece p : blockers) {
                            if (testMove(p, brdArray[i][shs.getXNum()])) {
                                blockable = true;
                            }
                        }

                    }
                }
            }

            if (shs.getYNum() == ts.getYNum()) {
                int max = Math.max(shs.getXNum(), ts.getXNum());
                int min = Math.min(shs.getXNum(), ts.getXNum());

                for (int i = min + 1; i < max; i++) {
                    List<Piece> blks =
                            blockMoves.get(brdArray[shs.getYNum()][i]);
                    ConcurrentLinkedDeque<Piece> blockers =
                            new ConcurrentLinkedDeque<Piece>();
                    blockers.addAll(blks);

                    if (!blockers.isEmpty()) {

                        movableSquares.add(brdArray[shs.getYNum()][i]);

                        for (Piece p : blockers) {
                            if (testMove(p, brdArray[shs.getYNum()][i])) {
                                blockable = true;
                            }
                        }

                    }
                }
            }

            Class<? extends Piece> tC = threats.get(0).getClass();

            if (tC.equals(Queen.class) || tC.equals(Bishop.class)) {
                int shX = shs.getXNum();
                int shY = shs.getYNum();
                int tX = ts.getXNum();
                int tY = ts.getYNum();

                if (shX > tX && shY > tY) {
                    for (int i = tX + 1; i < shX; i++) {
                        tY++;
                        List<Piece> blks =
                                blockMoves.get(brdArray[tY][i]);
                        ConcurrentLinkedDeque<Piece> blockers =
                                new ConcurrentLinkedDeque<Piece>();
                        blockers.addAll(blks);

                        if (!blockers.isEmpty()) {
                            movableSquares.add(brdArray[tY][i]);

                            for (Piece p : blockers) {
                                if (testMove(p, brdArray[tY][i])) {
                                    blockable = true;
                                }
                            }
                        }
                    }
                }

                if (shX > tX && tY > shY) {
                    for (int i = tX + 1; i < shY; i++) {
                        tY--;
                        List<Piece> blks =
                                blockMoves.get(brdArray[tY][i]);
                        ConcurrentLinkedDeque<Piece> blockers =
                                new ConcurrentLinkedDeque<Piece>();
                        blockers.addAll(blks);

                        if (!blockers.isEmpty()) {
                            movableSquares.add(brdArray[tY][i]);

                            for (Piece p : blockers) {
                                if (testMove(p, brdArray[tY][i])) {
                                    blockable = true;
                                }
                            }
                        }
                    }
                }

                if (tX > shX && shY > tY) {
                    for (int i = tX - 1; i > shX; i--) {
                        tY++;
                        List<Piece> blks =
                                blockMoves.get(brdArray[tY][i]);
                        ConcurrentLinkedDeque<Piece> blockers =
                                new ConcurrentLinkedDeque<Piece>();
                        blockers.addAll(blks);

                        if (!blockers.isEmpty()) {
                            movableSquares.add(brdArray[tY][i]);

                            for (Piece p : blockers) {
                                if (testMove(p, brdArray[tY][i])) {
                                    blockable = true;
                                }
                            }
                        }
                    }
                }

                if (tX > shX && tY > shY) {
                    for (int i = tX - 1; i > shX; i--) {
                        tY--;
                        List<Piece> blks =
                                blockMoves.get(brdArray[tY][i]);
                        ConcurrentLinkedDeque<Piece> blockers =
                                new ConcurrentLinkedDeque<Piece>();
                        blockers.addAll(blks);

                        if (!blockers.isEmpty()) {
                            movableSquares.add(brdArray[tY][i]);

                            for (Piece p : blockers) {
                                if (testMove(p, brdArray[tY][i])) {
                                    blockable = true;
                                }
                            }
                        }
                    }
                }
            }
        }

        return blockable;
    }

    public List<Square> getAllowableSquares(boolean whiteTurn) {
        movableSquares.clear();
        LinkedList<Piece> pieces = whiteTurn ? whitePieces : blackPieces;

        for (Piece p : pieces) {
            for (Square sq : p.getLegalMoves(b)) {
                if (testMove(p, sq)) {
                    movableSquares.add(sq);
                }
            }
        }
        return movableSquares;
    }

    public boolean testMove(Piece p, Square sq) {
        Piece c = sq.getOccupyingPiece();
        Square init = p.getPosition();

        p.move(sq);
        update();
        boolean moveTest = true;
        if (p.getColor() == 0 && blackInCheck()) moveTest = false;
        else if (p.getColor() == 1 && whiteInCheck()) moveTest = false;

        p.move(init);
        if (c != null) sq.put(c);

        update();
        movableSquares.addAll(squares);
        return moveTest;
    }

}
