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
import com.sun.tools.javac.util.Pair;
import java.util.ArrayList; 

/**
 * Jugador aleatori
 * @author bernat
 */
public class PlayerMiniMax implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private CellType myType;

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
        Point mov = MiniMax(s, 4, true).snd;
        return new Move( mov, 0L, 0, SearchType.RANDOM);
        //return move (posicio, 0, 0, MINIMAX)
    }
    
    Pair<Integer, Point> MiniMax(GameStatus s, int depth, boolean maximizingPlayer){
        //es terminal if isGameOver()
            //hi ha un guanyador (s.checkGameOver() i GetWinner()
            //no hi ha moviments possibles else
        //la profunditat es 0
        if(s.checkGameOver()){ //ha guanyat algu
            if(myType == s.GetWinner())               //  Guanyem nosaltres
                return new Pair<>(1000000, null);
            else                                    //  Guanya el contrincant
                return new Pair<>(-1000000, null);
        }
        else if(s.isGameOver() || depth==0){ //no hi ha moviments possibles o profunditat es 0
            return heuristica(s);
        }
        Point pos = new Point();
        ArrayList<Point> moves = s.getMoves();
        if(maximizingPlayer){
            int maxEval = Integer.MIN_VALUE;
            for (int i = 0; i < moves.size(); i++) {
                GameStatus fill = new GameStatus(s);
                fill.movePiece(moves.get(i));
                int eval = MiniMax(fill, depth-1, !maximizingPlayer);
                if(maxEval < eval){
                    maxEval = eval;
                    pos = moves.get(i);
                }
                return new Pair<>(maxEval, pos);
            }
        }
        else{
            int minEval = Integer.MAX_VALUE;
            for (int i = 0; i < moves.size(); i++) {
                GameStatus fill = new GameStatus(s);
                fill.movePiece(moves.get(i));
                int eval = MiniMax(fill, depth-1, !maximizingPlayer);
                if(minEval > eval){
                    minEval = eval;
                    pos = moves.get(i);
                }
                return new Pair<>(minEval, pos);
            }
        }
    }

    /**
     * 
     * @return
     */
    public int heuristica(GameStatus s){
        //mirar corners 
        
        //mirar quantes fitxer te cadascu
        //

        //Coin Parity Heuristic Value =
        //100 * (Max Player Coins - Min Player Coins ) / (Max Player Coins + Min Player Coins)

            return 1;
        }

    public int calcula(CellType player, GameStatus s){
        /*int heur = 0;

        int fitxes = s.getScore(player);

        int moviments = (s.getMoves(player)).size;/*
        
        */
        return 1;
    }
}
