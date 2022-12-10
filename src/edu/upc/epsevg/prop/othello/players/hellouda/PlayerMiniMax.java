package edu.upc.epsevg.prop.othello.players.hellouda;


import edu.upc.epsevg.prop.othello.CellType;
import edu.upc.epsevg.prop.othello.GameStatus;
import edu.upc.epsevg.prop.othello.IAuto;
import edu.upc.epsevg.prop.othello.IPlayer;
import edu.upc.epsevg.prop.othello.Move;
import edu.upc.epsevg.prop.othello.SearchType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Random;
import java.util.ArrayList; 

/**
 * Jugador aleatori
 * @author bernat
 */
public class PlayerMiniMax implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private CellType myType;
    private CellType hisType;
    private int[][] stabilityTable = {
        {4,  -3,  2,  2,  2,  2, -3,  4,},
        {-3, -4, -1, -1, -1, -1, -4, -3,},
        {2,  -1,  1,  0,  0,  1, -1,  2,},
        {2,  -1,  0,  1,  1,  0, -1,  2,},
        {2,  -1,  0,  1,  1,  0, -1,  2,},
        {2,  -1,  1,  0,  0,  1, -1,  2,},
        {-3, -4, -1, -1, -1, -1, -4, -3,},
        {4,  -3,  2,  2,  2,  2, -3,  4}
    };

    public PlayerMiniMax(String name) {
        this.name = name;
    }

    /**
     * Ens avisa que hem de parar la cerca en curs perquè s'ha exhaurit el temps
     * de joc.
     */
    @Override
    public String getName() {
        return "Hellowda(" + name + ")";
    }

    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
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
        
        myType = s.getCurrentPlayer();
        hisType = CellType.opposite(myType);
        Point mov = triaPosició(s, 6);
        return new Move( mov, 0L, 0, SearchType.RANDOM);
        //return move (posicio, 0, 0, MINIMAX)
    }
    
    Point triaPosició(GameStatus s, int depth){

        int maxEval = Integer.MIN_VALUE;
        Point bestMove = new Point();
        ArrayList<Point> moves = s.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            int eval = minValor(fill, depth-1);
            if(maxEval < eval){
                maxEval = eval;
                bestMove = moves.get(i);
            }
        }
        return bestMove;
    }

    int minValor(GameStatus s, int depth){
        if(s.checkGameOver()){                  //ha guanyat algu
            if(myType == s.GetWinner())             //  Guanyem nosaltres
                return 1000000;
            else                                    //  Guanya el contrincant
                return -1000000;
        }
        else if(s.isGameOver() || depth==0){    //no hi ha moviments possibles o profunditat es 0
            return heuristica(s, s.getCurrentPlayer());
        }
        int minEval = Integer.MAX_VALUE;
        ArrayList<Point> moves = s.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            minEval = Math.min(minEval, maxValor(fill, depth-1));
        }
        return minEval;
    }

    int maxValor(GameStatus s, int depth){
        if(s.checkGameOver()){                  //ha guanyat algu
            if(myType == s.GetWinner())             //  Guanyem nosaltres
                return 1000000;
            else                                    //  Guanya el contrincant
                return -1000000;
        }
        else if(s.isGameOver() || depth==0){    //no hi ha moviments possibles o profunditat es 0
            return heuristica(s, s.getCurrentPlayer());
        }
        int maxEval = Integer.MAX_VALUE;
        ArrayList<Point> moves = s.getMoves();
        for (int i = 0; i < moves.size(); i++) {
            GameStatus fill = new GameStatus(s);
            fill.movePiece(moves.get(i));
            maxEval = Math.max(maxEval, minValor(fill, depth-1));
        }
        return maxEval;
    }

    public int heuristica(GameStatus s, CellType player){
        //  mirar corners 
        //  mirar quantes fitxer te cadascu
        //  mirar posibles moviments

        //Coin Parity Heuristic Value =
        //100 * (Max Player Coins - Min Player Coins ) / (Max Player Coins + Min Player Coins)
        int heur = 0;
        
        int fitxes = s.getScore(player);
        heur += fitxes;
        
        int moviments = s.getMoves().size();
        heur += moviments;
        
        for (int i = 0; i < stabilityTable.length; i++) {
            for (int j = 0; j < stabilityTable.length; j++) {
                heur += stabilityTable[i][j];
            }
        }
        
        return heur;
    }
}
