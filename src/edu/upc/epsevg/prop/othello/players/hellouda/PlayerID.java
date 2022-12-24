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
 * Jugador MiniMax amb Iterative Deep Search (IDS)
 * @author Arnau Roca y Henok Argudo
 */
public class PlayerID implements IPlayer, IAuto {
    private CellType myType;
    private CellType opponentType;
    private int cntNodes;
    private int maxDepth;
    private long[][][] zobrist;
    private long N;
    private InfoNode[] tTransp;
    private boolean timeOut;
    private int profmax = 64;

    /**
     * Constructor PlayerID, inicialitzem atributs
     * i generem la zobrist amb numeros aleatoris.
     */
    public PlayerID() {
        cntNodes = 0;
        maxDepth = 0;
        timeOut = false;
        
        zobrist = new long[8][8][2];
        N = 59652323; //2 gb = 119304599 -- 1 gb = 59652323
        tTransp = new InfoNode[(int)N];
        // Creem un objecte Random
        Random random = new Random(N);
        // Inicialitzem la matriu amb valors aleatoris
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 2; k++) {
                    zobrist[i][j][k] = random.nextLong();
                }
            }
        }
    }
    
    /**
     * Funció que retorna el hash únic de un estat de joc.
     * @param s el joc actual.
     * @return un long que representa el hash de l'estat actual de joc.
     */
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
    
    /**
     * Classe auxiliar per emmagatzemar un parell posició i heuristica.
     */
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
        return ("Hellouda");
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
     * Utilitzant IDS obté el millor moviment donat un estat de joc.
     * @param s l'estat de joc actual.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        cntNodes = 0;
        maxDepth = 0;
        timeOut = false;
        
        MyGameStatus myGameStatus = new MyGameStatus(s);
        myType = myGameStatus.getCurrentPlayer();
        opponentType = CellType.opposite(myType);
        
        MyPair millorMov = new MyPair(new Point(), Integer.MIN_VALUE);
        int profIDS=1;
        while(!timeOut && profIDS<profmax){
            MyPair mov = triaPosició(myGameStatus, profIDS);
            if(mov.heuristica > millorMov.heuristica){
                millorMov=mov;
            }
            profIDS++;
        }
        return new Move(millorMov.position, cntNodes, profIDS, SearchType.MINIMAX_IDS);
    }
    
    /**
     * Funció que retorna el millor moviment per a un estat de joc
     * i una profunditat determinada, per a cada node afegeix una nova
     * entrada a la taula de transposició.
     * @param s l'estat de joc actual.
     * @param depth la màxima profunditat de la cerca minimax.
     * @return un Point que representa la posició del moviment.
     */
    MyPair triaPosició(MyGameStatus s, int depth){
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
                break;
            }
            if(i==bestIndexStored && bestVisited && i<moves.size()-1){
                i++;
            }
            if(bestIndexStored != -1 && !bestVisited && bestIndexStored<moves.size()){
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
        
        tTransp[(int)(getHash(s)%N)] = new InfoNode((byte) bestIndexToStore,  currentNum1, currentNum2 ,depth, maxEval, exact);
        return new MyPair(bestMove, maxEval);
    }

    /**
     * Funció que utilitza l'algorisme minimax amb poda alpha-beta
     * per obtenir el valor amb mínima heurística, per a cada node
     * afegeix una nova entrada a la taula de transposició.
     * @param s l'estat de joc actual.
     * @param depth la màxima profunditat de la cerca minimax.
     * @param beta valor beta.
     * @param alpha valor alpha.
     * @return el valor heurístic més petit possible a partir del estat actual.
     */
    int minValor(MyGameStatus s, int depth, int alpha, int beta){
        ++cntNodes;

        if(s.checkGameOver()){                  //ha guanyat algu
            if(myType == s.GetWinner())             //  Guanyem nosaltres
                return Integer.MAX_VALUE-1;
            else                                    //  Guanya el contrincant
                return Integer.MIN_VALUE+1;
        }
        else if(s.isGameOver() || depth==0){    //no hi ha moviments possibles o profunditat es 0
            return heuristica(s);
        }
        
        int bestIndexStored = -1;
        long hash = getHash(s);
        InfoNode storedResult = tTransp[(int)(hash%N)];
        long currentNum1 = s.getBoard_occupied().toLongArray()[0];
        long currentNum2 = s.getBoard_color().toLongArray()[0];
        if(storedResult != null ){
            if(storedResult.num1 == currentNum1 && storedResult.num2 == currentNum2){
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
        }
        
        boolean exact = true;
        int bestIndexToStore = 0;
        int minEval = Integer.MAX_VALUE;
        ArrayList<Point> moves = s.getMoves();
        boolean bestVisited = false;
        for (int i = 0; i < moves.size(); i++) {
             if(timeOut){
                break;
            }
             if(i==bestIndexStored && bestVisited && i<moves.size()-1){
                i++;
            }
            if(bestIndexStored != -1 && !bestVisited && bestIndexStored < moves.size()){
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
                
                exact = false;
                break;
            }
        }
        tTransp[(int)(getHash(s)%N)] = new InfoNode((byte) bestIndexToStore, currentNum1, currentNum2, depth, minEval, exact);
        return minEval;
    }

    /**
     * Funció que utilitza l'algorisme minimax amb poda alpha-beta
     * per obtenir el valor amb màxima heurística, per a cada node
     * afegeix una nova entrada a la taula de transposició.
     * @param s l'estat de joc actual.
     * @param depth la màxima profunditat de la cerca minimax.
     * @param beta valor beta.
     * @param alpha valor alpha.
     * @return el valor heurístic més gran possible a partir del estat actual.
     */
    int maxValor(MyGameStatus s, int depth, int alpha, int beta){
        ++cntNodes;
        if(s.checkGameOver()){                  //ha guanyat algu
            if(myType == s.GetWinner())             //  Guanyem nosaltres
                return Integer.MAX_VALUE-1;
            else                                    //  Guanya el contrincant
                return Integer.MIN_VALUE+1;
        }
        else if(s.isGameOver() || depth==0){    //no hi ha moviments possibles o profunditat es 0
            return heuristica(s);
        }
        
        int bestIndexStored = -1;
        long hash = getHash(s);
        InfoNode storedResult = tTransp[(int)(hash%N)];
        long currentNum1 = s.getBoard_occupied().toLongArray()[0];
        long currentNum2 = s.getBoard_color().toLongArray()[0];
        if(storedResult != null){
            if(storedResult.num1 == currentNum1 && storedResult.num2 == currentNum2){
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
        }
        
        boolean exact = true;
        int bestIndexToStore = 0;
        int maxEval = Integer.MIN_VALUE;
        ArrayList<Point> moves = s.getMoves();
        boolean bestVisited = false;
        for (int i = 0; i < moves.size(); i++) {
            if(timeOut){
                break;
            }
            if(i==bestIndexStored && bestVisited && i<moves.size()-1){
                i++;
            }
            if(bestIndexStored != -1 && !bestVisited && bestIndexStored<moves.size()){
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
                
                exact = false;
                break;
            }
        }
        tTransp[(int)(getHash(s)%N)] = new InfoNode((byte) bestIndexToStore, currentNum1, currentNum2, depth, maxEval, exact);
        return maxEval;
    }
    
    /**
     * Funció que retorna la heuristica de un estat de joc determinat.
     * @param s l'estat de joc actual.
     * @return un valor numéric que representa la heuristica del estat de joc actual.
     */
    public int heuristica(MyGameStatus s) {
        int myTiles = 0, oppTiles = 0, myFrontTiles = 0, oppFrontTiles = 0, ret=0, cc=0;
        double p = 0, c = 0, m = 0, f = 0, d = 0, pi=0, xox=0;

        int[] X1 = {-1, -1, 0, 1, 1, 1, 0, -1};
        int[] Y1 = {0, 1, 1, 1, 0, -1, -1, -1};
        int[][] V = {
            {20, -15, 8, 8, 8, 8, -15, 20},
            {-15, -17, -4, 1, 1, -4, -17, -15},
            {8, -4, 2, 2, 2, 2, -4, 8},
            {8, 1, 2, -3, -3, 2, 1, 8},
            {8, 1, 2, -3, -3, 2, 1, 8},
            {8, -4, 2, 2, 2, 2, -4, 11},
            {-15, -17, -4, 1, 1, -4, -17, -15},
            {20, -15, 8, 8, 8, 11, -15, 20}
                
                
        };
        //  mirem si fem una piramide al corner 0,0 (cel·les ocupades que son immobils)
        int[] X2 = {0,1,0,2,0,1,2,1,3,0};
        int[] Y2 = {0,0,1,0,2,1,1,2,0,3};
        int x =0, y=0;
        myTiles=0;oppTiles=0;
        if(s.getPos(x,y)==CellType.PLAYER1 || s.getPos(x,y)==CellType.PLAYER2){
            CellType mom= s.getPos(x,y);
            for(int i=0; i<10; i++){
                int xm=x+X2[i];
                int ym=y+Y2[i];
                V[xm][ym]=0;
                if(s.getPos(xm,ym)==mom){
                    if(mom == myType){
                        myTiles+=1;
                    }
                    else{
                        oppTiles+=1;
                    }
                }
            }
        }
        //  mirem si fem una piramide al corner 0,7 (cel·les ocupades que son immobils)
        x =0; y=s.getSize()-1;
        if(s.getPos(x,y)==CellType.PLAYER1 || s.getPos(x,y)==CellType.PLAYER2){
            
            CellType mom= s.getPos(x,y);
            for(int i=0; i<10; i++){
                int xm=x+X2[i];
                int ym=y-Y2[i];
                V[xm][ym]=0;
                if(s.getPos(xm,ym)==mom){
                    if(mom == myType){
                        myTiles+=1;
                    }
                    else{
                        oppTiles+=1;
                    }
                }
            }
        }
        
        //  mirem si fem una piramide al corner 7,0 (cel·les ocupades que son immobils)
        x =s.getSize()-1; y=0;
        if(s.getPos(x,y)==CellType.PLAYER1 || s.getPos(x,y)==CellType.PLAYER2){
            CellType mom= s.getPos(x,y);
            for(int i=0; i<10; i++){
                int xm=x-X2[i];
                int ym=y+Y2[i];
                V[xm][ym]=0;
                if(s.getPos(xm,ym)==mom){
                    if(mom == myType){
                        myTiles+=1;
                    }
                    else{
                        oppTiles+=1;
                    }
                }
            }
        }
        //  mirem si fem una piramide al corner 7,7 (cel·les ocupades que son immobils)
        x =s.getSize()-1; y=s.getSize()-1;
        if(s.getPos(x,y)==CellType.PLAYER1 || s.getPos(x,y)==CellType.PLAYER2){
            CellType mom= s.getPos(x,y);
            for(int i=0; i<10; i++){
                int xm=x-X2[i];
                int ym=y-Y2[i];
                V[xm][ym]=0;
                if(s.getPos(xm,ym)==mom){
                    if(mom == myType){
                        myTiles+=1;
                    }
                    else{
                        oppTiles+=1;
                    }
                    
                }
            }
        }
        if (myTiles > oppTiles) {
            pi = (100.0 * myTiles) / (myTiles + oppTiles);
        } else if (myTiles < oppTiles) {
            pi = -(100.0 * oppTiles) / (myTiles + oppTiles);
        } else {
            pi = 0;
        }
        
        //  mirem cas X0X nostre en les files del cantó (de 0,0 a 7,0) i (0,7 a 7,7)
        myTiles=0;oppTiles=0;
        x=0;y=0;
        for(int i=0; i<s.getSize()-3; ++i){
            int xm=x+i;
            if ((s.getPos(xm,y) == opponentType)){
                if(s.getPos(xm+2,y) == opponentType && s.getPos(xm+1,y)==myType){
                    myTiles += 1;
                }
                y=s.getSize()-1;
                if(s.getPos(xm+2,y) == opponentType && s.getPos(xm+1,y)==myType){
                    myTiles += 1;
                }
            }
            if ((s.getPos(xm,y) == myType)){
                if(s.getPos(xm+2,y) == myType && s.getPos(xm+1,y)==opponentType){
                    oppTiles += 1;
                }
                y=s.getSize()-1;
                if(s.getPos(xm+2,y) == myType && s.getPos(xm+1,y)==opponentType){
                    oppTiles += 1;
                }
            }
        }
        //  mirem cas X0X nostre en les columnes del cantó (de 0,0 a 0,7) i (7,0 a 7,7)
        x=0;y=0;
        for(int i=0; i<s.getSize()-3; ++i){
            int ym=y+i;
            if ((s.getPos(x,ym) == opponentType)){
                if(s.getPos(x,ym+2) == opponentType && s.getPos(x,ym+1)==myType){
                    myTiles += 1;
                }
                x=s.getSize()-1;
                if(s.getPos(x,ym+2) == opponentType && s.getPos(x,ym+2)==myType){
                    myTiles += 1;
                }
            }
            if ((s.getPos(x,ym) == myType)){
                if(s.getPos(x,ym+2) == myType && s.getPos(x,ym+1)==opponentType){
                    oppTiles += 1;
                }
                x=s.getSize()-1;
                if(s.getPos(x,ym+2) == myType && s.getPos(x,ym+1)==opponentType){
                    oppTiles += 1;
                }
            }
        }
        if (myTiles > oppTiles) {
            xox = (100.0 * myTiles) / (myTiles + oppTiles);
        } else if (myTiles < oppTiles) {
            xox = -(100.0 * oppTiles) / (myTiles + oppTiles);
        } else {
            xox = 0;
        }
        //  mirem la taula de valors i les fitxer frontera (fitxes que tenen al
        //  costat fitxes buides) mirar documentació per saber que es una fitxa frontera.
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                if (s.getPos(i,j) == myType) {
                    d += V[i][j];

                } else if (s.getPos(i,j) == opponentType) {
                    d -= V[i][j];
                }
                if (s.getPos(i,j) == opponentType || s.getPos(i,j) == myType) {
                    for (int k = 0; k < 8; k++) {
                        x = i + X1[k];
                        y = j + Y1[k];
                        if (x >= 0 && x < 8 && y >= 0 && y < 8 && (s.getPos(x,y) == CellType.EMPTY)) {
                            if (s.getPos(i,j) == myType){
                                myFrontTiles++;
                            } else if(s.getPos(i,j) == opponentType){
                                oppFrontTiles++;
                            }
                        }
                    }
                }
            }
        }
        //  mirem el nombre de fitxes de cada jugador
        myTiles=s.getScore(myType);
        oppTiles=s.getScore(opponentType);
        if (myTiles > oppTiles) {
            p = (100.0 * myTiles) / (myTiles + oppTiles);
        } else if (myTiles < oppTiles) {
            p = -(100.0 * oppTiles) / (myTiles + oppTiles);
        } else {
            p = 0;
        }
        //  posem les fitxes frontera en tant per cent
        if (myFrontTiles > oppFrontTiles) {
            f = -(100.0 * myFrontTiles) / (myFrontTiles + oppFrontTiles);
        } else if (myFrontTiles < oppFrontTiles) {
            f = (100.0 * oppFrontTiles) / (myFrontTiles + oppFrontTiles);
        } else {
            f = 0;
        }

        // mirem l'ocupació dels corners
        myTiles = oppTiles = 0;
        if (s.getPos(0,0) == myType) {
            myTiles += 1;
        }
        else if (s.getPos(0,0) == (opponentType)) {
            oppTiles += 1;
        }
        if (s.getPos(0,s.getSize()-1) == myType) {
            myTiles += 1;
        }
        else if (s.getPos(0,s.getSize()-1) == (opponentType)) {
            oppTiles += 1;
        }
        if (s.getPos(s.getSize()-1,0) == myType) {
            myTiles += 1;
        }
        else if (s.getPos(s.getSize()-1,0) == (opponentType)) {
            oppTiles += 1;
        }
        if (s.getPos(s.getSize()-1, s.getSize()-1) == myType) {
            myTiles += 1;
        }
        else if (s.getPos(s.getSize()-1, s.getSize()-1) == (opponentType)) {
            oppTiles += 1;
        }

        c = 25 * (myTiles - oppTiles);

        // mirem la mobilitat de cada jugador
        myTiles = oppTiles = 0;
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                if (s.getPos(i,j) == myType) {
                    for (int k = 0; k < s.getSize(); k++) {
                        x = i + X1[k];
                        y = j + Y1[k];
                        if (x >= 0 && x < s.getSize() && y >= 0 && y < s.getSize() && s.getPos(x,y) == opponentType) {
                            x += X1[k];
                            y += Y1[k];
                            boolean inside = x >= 0 && x < s.getSize() && y >= 0 && y < s.getSize();
                            if(inside){
                                CellType auxCell = s.getPos(x, y);
                                while(auxCell != CellType.EMPTY && auxCell != myType && inside){
                                    x += X1[k];
                                    y += Y1[k];
                                    inside = x >= 0 && x < s.getSize() && y >= 0 && y < s.getSize();
                                    if(inside)
                                        auxCell = s.getPos(x, y);
                                }
                                if (auxCell == CellType.EMPTY  && inside) {
                                    myTiles++;
                                }
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < s.getSize(); i++) {
            for (int j = 0; j < s.getSize(); j++) {
                if (s.getPos(i,j) == opponentType) {
                    for (int k = 0; k < s.getSize(); k++) {
                        x = i + X1[k];
                        y = j + Y1[k];
                        if (x >= 0 && x < s.getSize() && y >= 0 && y < s.getSize() && s.getPos(x,y) == myType) {
                            x += X1[k];
                            y += Y1[k];
                            boolean inside = x >= 0 && x < s.getSize() && y >= 0 && y < s.getSize();
                            if(inside){
                                CellType auxCell = s.getPos(x, y);
                                while(auxCell != CellType.EMPTY && auxCell != opponentType  && inside){
                                    x += X1[k];
                                    y += Y1[k];
                                    inside = x >= 0 && x < s.getSize() && y >= 0 && y < s.getSize();
                                    if(inside)
                                        auxCell = s.getPos(x, y);
                                }
                                if (auxCell == CellType.EMPTY  && inside) {
                                    oppTiles++;
                                }
                            }
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
        // balanceig final
        ret = (int) (10*p + 800*c + 78*m + 74*f + 150*d + 160*xox + 200*pi);
        return ret ;
    }
}