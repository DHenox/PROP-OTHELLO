package edu.upc.epsevg.prop.othello.players.hellouda;


import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.Random;
import java.util.ArrayList; 

/**
 * Jugador aleatori
 * @author bernat
 */
public class PlayerID implements IPlayer, IAuto {
    private String playerName;
    private CellType myType;
    private CellType hisType;
    private int cntNodes;
    private int maxDepth;
    private long[][][] zobrist;
    private long N;
    private InfoNode[] tTransp;
    private boolean timeOut;
    private int cntPodes;

    public PlayerID(String name) {
        playerName = name;
        cntNodes = 0;
        maxDepth = 0;
        timeOut = false;
        cntPodes = 0;
        zobrist = new long[8][8][2];
        N = 59652323; //2 gb = 119304599 -- 1 gb = 59652323
        tTransp = new InfoNode[(int)N];
        // Creem un objecte Random
        Random random = new Random();
        // Inicialitzem la matriu amb valors aleatoris
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 2; k++) {
                    zobrist[i][j][k] = random.nextLong();
                }
            }
        }
    }
    
    public long getHash(GameStatus s){
        long hash  = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if(s.getPos(i, j) != CellType.EMPTY){
                    for (int k = 0; k < 2; k++) {
                        hash ^= zobrist[i][j][k];
                    }
                }
            }
        }
        return Math.abs(hash);
    }
    
    private class MyPair{
        Point position;
        int heuristica;

        public MyPair(Point mov, int heuristica) {
            this.position = mov;
            this.heuristica = heuristica;
        }
    }
    
    @Override
    public String getName() {
        return "Hellowda(" + playerName + ")";
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        //System.out.println("TIME OUT -> tallem la cerca ");
        timeOut = true;
    }
    
    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s Tauler i estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        MyGameStatus myGameStatus = new MyGameStatus(s);
        myType = myGameStatus.getCurrentPlayer();
        hisType = CellType.opposite(myType);
        
        cntNodes = 0;
        maxDepth = 0;
        cntPodes = 0;
        timeOut = false;
        
        MyPair millorMov = new MyPair(new Point(), Integer.MIN_VALUE);
        int profIDS=1;
        while(!timeOut){
            MyPair mov = triaPosició(myGameStatus, profIDS);
            if(mov.heuristica > millorMov.heuristica){
                millorMov=mov;
            }
            profIDS++;
        }
        //System.out.println("nº podes: " + cntPodes);
        return new Move(millorMov.position, cntNodes, profIDS, SearchType.MINIMAX_IDS);
    }
    
    MyPair triaPosició(MyGameStatus s, int depth){
        if(timeOut){
            return new MyPair(new Point(), Integer.MIN_VALUE);
        }
        
        int bestIndexStored = -1;
        long hash = getHash(s);
        InfoNode storedResult = tTransp[(int)(hash%N)];
        long currentNum1 = s.getBoard_occupied().toLongArray()[0];
        long currentNum2 = s.getBoard_color().toLongArray()[0];
        if(storedResult != null && storedResult.num1 == currentNum1 && storedResult.num2 == currentNum2){
            bestIndexStored = storedResult.indexMillorFill;
        }
        
        boolean exact = true;
        int bestIndexToStore = 0;
        ArrayList<Point> moves = s.getMoves();
        int maxEval = Integer.MIN_VALUE;
        Point bestMove = new Point();
        boolean bestVisited = false;
        for (int i = 0; i < moves.size(); i++) {
            if(timeOut){
                return new MyPair(new Point(), 0);
            }
            if(bestIndexStored != -1 && !bestVisited){
                i = bestIndexStored;
            }
            MyGameStatus fill = new MyGameStatus(s);
            fill.movePiece(moves.get(i));
            int eval = minValor(fill, depth-1, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if(maxEval < eval){
                maxEval = eval;
                bestMove = moves.get(i);
                bestIndexToStore = i;
            }
            if(bestIndexStored != -1 && !bestVisited){
                i = 0;
                bestVisited = true;
            }
        }
        tTransp[(int)(getHash(s)%N)] = new InfoNode((byte) bestIndexToStore, depth, maxEval, exact, s);
        return new MyPair(bestMove, maxEval);
    }

    int minValor(MyGameStatus s, int depth, int alpha, int beta){
        ++cntNodes;
        if(timeOut){
            return Integer.MAX_VALUE;
        }

        if(s.checkGameOver()){                  //ha guanyat algu
            if(myType == s.GetWinner())             //  Guanyem nosaltres
                return Integer.MAX_VALUE;
            else                                    //  Guanya el contrincant
                return Integer.MIN_VALUE;
        }
        else if(s.isGameOver() || depth==0){    //no hi ha moviments possibles o profunditat es 0
            return heuristica(s);
        }
        
        int bestIndexStored = -1;
        long hash = getHash(s);
        InfoNode storedResult = tTransp[(int)(hash%N)];
        long currentNum1 = s.getBoard_occupied().toLongArray()[0];
        long currentNum2 = s.getBoard_color().toLongArray()[0];
        if(storedResult != null && storedResult.num1 == currentNum1 && storedResult.num2 == currentNum2){
            if(storedResult.nivellsPerSota >= depth){
                if(storedResult.isExact == true){
                    return storedResult.heur;
                }
                else if(storedResult.isExact == false){
                    beta = storedResult.heur;
                }
            }
            bestIndexStored = storedResult.indexMillorFill;
        }
        
        boolean exact = true;
        int bestIndexToStore = 0;
        int minEval = Integer.MAX_VALUE;
        ArrayList<Point> moves = s.getMoves();
        boolean bestVisited = false;
        for (int i = 0; i < moves.size(); i++) {
            if(timeOut){
                return 0;
            }
            if(bestIndexStored != -1 && !bestVisited){
                i = bestIndexStored;
            }
            MyGameStatus fill = new MyGameStatus(s);
            fill.movePiece(moves.get(i));
            
            minEval = Math.min(minEval, maxValor(fill, depth-1, alpha, beta));
            //beta = Math.min(beta, minEval);
            if(beta > minEval){
                beta = minEval;
                bestIndexToStore = i;
            }
            if(bestIndexStored != -1 && !bestVisited){
                i = 0;
                bestVisited = true;
            }
            if(alpha>=beta){
                ++cntPodes;
                exact = false;
                break;
            }
        }
        tTransp[(int)(getHash(s)%N)] = new InfoNode((byte) bestIndexToStore, depth, minEval, exact, s);
        return minEval;
    }

    int maxValor(MyGameStatus s, int depth, int alpha, int beta){
        ++cntNodes;
        if(timeOut){
            return Integer.MIN_VALUE;
        }
        if(s.checkGameOver()){                  //ha guanyat algu
            if(myType == s.GetWinner())             //  Guanyem nosaltres
                return Integer.MAX_VALUE;
            else                                    //  Guanya el contrincant
                return Integer.MIN_VALUE;
        }
        else if(s.isGameOver() || depth==0){    //no hi ha moviments possibles o profunditat es 0
            return heuristica(s);
        }
        
        int bestIndexStored = -1;
        long hash = getHash(s);
        InfoNode storedResult = tTransp[(int)(hash%N)];
        long currentNum1 = s.getBoard_occupied().toLongArray()[0];
        long currentNum2 = s.getBoard_color().toLongArray()[0];
        if(storedResult != null && storedResult.num1 == currentNum1 && storedResult.num2 == currentNum2){
            if(storedResult.nivellsPerSota >= depth){
                if(storedResult.isExact == true){
                    return storedResult.heur;
                }
                else if(storedResult.isExact == false){
                    alpha = storedResult.heur;
                }
            }
            bestIndexStored = storedResult.indexMillorFill;
        }
        
        boolean exact = true;
        int bestIndexToStore = 0;
        int maxEval = Integer.MIN_VALUE;
        ArrayList<Point> moves = s.getMoves();
        boolean bestVisited = false;
        for (int i = 0; i < moves.size(); i++) {
            if(timeOut){
                return 0;
            }
            if(bestIndexStored != -1 && !bestVisited){
                i = bestIndexStored;
            }
            
            MyGameStatus fill = new MyGameStatus(s);
            fill.movePiece(moves.get(i));
            maxEval = Math.max(maxEval, minValor(fill, depth-1, alpha, beta));
            //alpha = Math.max(alpha, maxEval);
            if(alpha < maxEval){
                alpha = maxEval;
                bestIndexToStore = i;
            }
            if(bestIndexStored != -1 && !bestVisited){
                i = 0;
                bestVisited = true;
            }
            if(alpha>=beta){
                ++cntPodes;
                exact = false;
                break;
            }
        }
        tTransp[(int)(getHash(s)%N)] = new InfoNode((byte) bestIndexToStore, depth, maxEval, exact, s);
        return maxEval;
    }
    
    public int heuristica(MyGameStatus s) {
        int myTiles = 0, oppTiles = 0, myFrontTiles = 0, oppFrontTiles = 0;
        double p = 0, c = 0, l = 0, m = 0, f = 0, d = 0;

        CellType player = myType;
        int[] X1 = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] Y1 = {0, 1, 1, 1, 0, -1, -1, -1};
        int[][] V = {
            {20, -3, 11, 8, 8, 11, -3, 20},
            {-3, -7, -4, 1, 1, -4, -7, -3},
            {11, -4, 2, 2, 2, 2, -4, 11},
            {8, 1, 2, -3, -3, 2, 1, 8},
            {8, 1, 2, -3, -3, 2, 1, 8},
            {11, -4, 2, 2, 2, 2, -4, 11},
            {-3, -7, -4, 1, 1, -4, -7, -3},
            {20, -3, 11, 8, 8, 11, -3, 20}
        };
        CellType opponent = CellType.opposite(player);

        // Piece difference, frontier disks and disk squares
        for (int i = 0; i < s.getSize(); i++) {
            if(timeOut){
                return 0;
            }
            for (int j = 0; j < s.getSize(); j++) {
                if(timeOut){
                    return 0;
                }
                if (s.getPos(i,j) == player) {
                    d += V[i][j];
                    myTiles++;
                } else if (s.getPos(i,j) == opponent) {
                    d -= V[i][j];
                    oppTiles++;
                }
                if (s.getPos(i,j) == opponent || s.getPos(i,j) == player) {
                    for (int k = 0; k < 8; k++) {
                        if(timeOut){
                            return 0;
                        }
                        int x = i + X1[k];
                        int y = j + Y1[k];
                        if (x >= 0 && x < 8 && y >= 0 && y < 8 && (s.getPos(x,y) == CellType.EMPTY)) {
                            if (s.getPos(i,j) == player){
                                myFrontTiles++;
                            }
                        } else {
                            oppFrontTiles++;
                        }
                        break;
                    }
                }
            }
        }

        if (myTiles > oppTiles) {
            p = (100.0 * myTiles) / (myTiles + oppTiles);
        } else if (myTiles < oppTiles) {
            p = -(100.0 * oppTiles) / (myTiles + oppTiles);
        } else {
            p = 0;
        }
        
        if (myFrontTiles > oppFrontTiles) {
            f = -(100.0 * myFrontTiles) / (myFrontTiles + oppFrontTiles);
        } else if (myFrontTiles < oppFrontTiles) {
            f = (100.0 * oppFrontTiles) / (myFrontTiles + oppFrontTiles);
        } else {
            f = 0;
        }
        
        // Corner occupancy
        myTiles = oppTiles = 0;
        if (s.getPos(0,0) == player) {
                myTiles += 1;
            }
            else if (s.getPos(0,0) == (opponent)) {
                oppTiles += 1;
            }
            if (s.getPos(0,s.getSize()-1) == player) {
                myTiles += 1;
            }
            else if (s.getPos(0,s.getSize()-1) == (opponent)) {
                oppTiles += 1;
            }
            if (s.getPos(s.getSize()-1,0) == player) {
                myTiles += 1;
            }
            else if (s.getPos(s.getSize()-1,0) == (opponent)) {
                oppTiles += 1;
            }
            if (s.getPos(s.getSize()-1, s.getSize()-1) == player) {
                myTiles += 1;
            }
            else if (s.getPos(s.getSize()-1, s.getSize()-1) == (opponent)) {
                oppTiles += 1;
            }
            
        c = 25 * (myTiles - oppTiles);
        
        if(timeOut){
            return 0;
        }
        
        // Corner closeness
        myTiles = oppTiles = 0;
        if ((s.getPos(0,0) == CellType.EMPTY)) {
            if (s.getPos(0,1) == player) {
                myTiles++;
            } else if (s.getPos(0,1) == opponent) {
                oppTiles++;
            }
            if (s.getPos(1,1) == player) {
                myTiles++;
            } else if (s.getPos(1,1) == opponent) {
                oppTiles++;
            }
            if (s.getPos(1,0) == player) {
                myTiles++;
            } else if (s.getPos(1,0) == opponent) {
                oppTiles++;
            }
        }
        if ((s.getPos(0,s.getSize()-1) == CellType.EMPTY)) {
            if (s.getPos(0,6) == player) {
                myTiles++;
            } else if (s.getPos(0,6) == opponent) {
                oppTiles++;
            }
            if (s.getPos(1,6) == player) {
                myTiles++;
            } else if (s.getPos(1,6) == opponent) {
                oppTiles++;
            }
            if (s.getPos(1,s.getSize()-1) == player) {
                myTiles++;
            } else if (s.getPos(1,s.getSize()-1) == opponent) {
                oppTiles++;
            }
        }
        if ((s.getPos(s.getSize()-1,0) == CellType.EMPTY)) {
            if (s.getPos(s.getSize()-1,1) == player) {
                myTiles++;
            } else if (s.getPos(s.getSize()-1,1) == opponent) {
                oppTiles++;
            }
            if (s.getPos(6,1) == player) {
                myTiles++;
            } else if (s.getPos(6,1) == opponent) {
                oppTiles++;
            }
            if (s.getPos(6,0) == player) {
                myTiles++;
            } else if (s.getPos(6,0) == opponent) {
                oppTiles++;
            }
        }
        if ((s.getPos(s.getSize()-1,s.getSize()-1) == CellType.EMPTY)) {
            if (s.getPos(6,s.getSize()-1) == player) {
                myTiles++;
            } else if (s.getPos(6,s.getSize()-1) == opponent) {
                oppTiles++;
            }
            if (s.getPos(6,6) == player) {
                myTiles++;
            } else if (s.getPos(6,6) == opponent) {
                oppTiles++;
            }
            if (s.getPos(s.getSize()-1,6) == player) {
                myTiles++;
            } else if (s.getPos(s.getSize()-1,6) == opponent) {
                oppTiles++;
            }
        }
        l = -12.5 * (myTiles - oppTiles);
        
        // Mobility
        myTiles = oppTiles = 0;
        for (int i = 0; i < s.getSize(); i++) {
            if(timeOut){
                return 0;
            }
            for (int j = 0; j < s.getSize(); j++) {
                if(timeOut){
                    return 0;
                }
                if (!(s.getPos(0,0) == CellType.EMPTY)) {
                    for (int k = 0; k < s.getSize(); k++) {
                        if(timeOut){
                            return 0;
                        }
                        int x = i + X1[k];
                        int y = j + Y1[k];
                        if (x >= 0 && x < s.getSize() && y >= 0 && y < s.getSize() && s.getPos(x,y) == opponent) {
                            if (myTiles < 64) {
                                myTiles++;
                            }
                           break;
                        }
                    }
                }
            }
        }
        for (int i = 0; i < s.getSize(); i++) {
            if(timeOut){
                return 0;
            }
            for (int j = 0; j < s.getSize(); j++) {
                if(timeOut){
                    return 0;
                }
                if (!(s.getPos(0,0) == CellType.EMPTY)) {
                    for (int k = 0; k < s.getSize(); k++) {
                        if(timeOut){
                            return 0;
                        }
                        int x = i + X1[k];
                        int y = j + Y1[k];
                        if (x >= 0 && x < s.getSize() && y >= 0 && y < s.getSize() && s.getPos(x,y) == player) {
                            if (oppTiles < 64) {
                                oppTiles++;
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (myTiles > oppTiles) {
            m = (100.0 * myTiles) / (myTiles + oppTiles);
        } else if (myTiles < oppTiles) {
            m = -(100.0 * oppTiles) / (myTiles + oppTiles);
        } else {
            m = 0;
        }
        // Final weighted score
        int ret = (int) ((10 * p + 801 * c + 382 * l + 78 * m + 74 * f + 10 * d));
        return ret ;
    }
}