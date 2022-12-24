package edu.upc.epsevg.prop.othello.players.hellouda;


import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList; 

/**
 * Jugador MiniMax
 * @author Arnau Roca y Henok Argudo
 */
public class PlayerMiniMax implements IPlayer, IAuto {
    private int ContaNodes;
    private GameStatus s;
    private CellType myType;
    private CellType opponentType;

    public PlayerMiniMax() {
    }

    @Override
    public String getName() {
        return "Hellowda";
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
    }
    
    /**
     * Decideix el moviment del jugador donat un tauler i un color de peça que
     * ha de posar.
     *
     * @param s estat actual de joc.
     * @return el moviment que fa el jugador.
     */
    @Override
    public Move move(GameStatus s) {
        ContaNodes = 0;
        myType = s.getCurrentPlayer();
        opponentType = CellType.opposite(myType);
        Point mov = triaPosició(s, 8);
        return new Move( mov, this.ContaNodes, 0, SearchType.MINIMAX);
    }
    
    /**
     * Funció que retorna el millor moviment per a un estat de joc
     * i una profunditat determinada.
     * @param s l'estat de joc actual
     * @param depth la màxima profunditat de la cerca minimax
     * @return un Point que representa la posició del moviment
     */
    Point triaPosició(GameStatus s, int depth){
        int maxEval = Integer.MIN_VALUE;
        Point bestMove = new Point();
        ArrayList<Point> moves = s.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            int eval = minValor(fill, depth-1, Integer.MAX_VALUE, Integer.MIN_VALUE);
            if(maxEval < eval){
                maxEval = eval;
                bestMove = moves.get(i);
            }
        }
        return bestMove;
    }

    /**
     * Funció que utilitza l'algorisme minimax amb poda alpha-beta
     * per obtenir el valor amb mínima heurística.
     * @param s l'estat de joc actual
     * @param depth la màxima profunditat de la cerca minimax
     * @param beta valor beta
     * @param alpha valor alpha
     * @return el valor heurístic més petit possible a partir del estat actual
     */
    int minValor(GameStatus s, int depth, int beta, int alpha){
        //  ha guanyat algu
        if(s.checkGameOver()){
            //  Guanyem nosaltres
            if(myType == s.GetWinner())
                return 1000000;
            //  Guanya el contrincant
            else
                return -1000000;
        }
        //  no hi ha moviments possibles o profunditat es 0
        else if(s.isGameOver() || depth==0){
            return heuristica(s);
        }
        
        int minEval = Integer.MAX_VALUE;
        ArrayList<Point> moves = s.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
             
            minEval = Math.min(minEval, maxValor(fill, depth-1, beta, alpha));
            beta = Math.min(beta, minEval);
            if(alpha>=beta){
                break;
            }
            
        }
        return minEval;
    }

    /**
     * Funció que utilitza l'algorisme minimax amb poda alpha-beta
     * per obtenir el valor amb màxima heurística.
     * @param s l'estat de joc actual
     * @param depth la màxima profunditat de la cerca minimax
     * @param beta valor beta
     * @param alpha valor alpha
     * @return el valor heurístic més gran possible a partir del estat actual
     */
    int maxValor(GameStatus s, int depth, int beta, int alpha){
        //  ha guanyat algu
        if(s.checkGameOver()){
            //  Guanyem nosaltres
            if(myType == s.GetWinner())
                return 1000000;
            //  Guanya el contrincant
            else
                return -1000000;
        }
        //  no hi ha moviments possibles o profunditat es 0
        else if(s.isGameOver() || depth==0){
            return heuristica(s);
        }
        
        int maxEval = Integer.MIN_VALUE+1;
        ArrayList<Point> moves = s.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            maxEval = Math.max(maxEval, minValor(fill, depth-1, beta, alpha));
            alpha = Math.max(alpha, maxEval);
            if(alpha>=beta){
                break;
            }
        }
        return maxEval;
    }
    
    /**
     * Funció que retorna la heuristica de un estat de joc determinat.
     * @param s l'estat de joc actual.
     * @return un valor numéric que representa la heuristica del estat de joc actual.
     */
    public int heuristica(GameStatus s) {
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