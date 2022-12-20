/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.othello.players.hellouda;

import edu.upc.epsevg.prop.othello.GameStatus;
import java.util.BitSet;

/**
 *
 * @author HENOK
 */
public class MyGameStatus extends GameStatus{

    public MyGameStatus(GameStatus gs) {
        super(gs);
    }

    public BitSet getBoard_occupied() {
        return board_occupied;
    }

    public BitSet getBoard_color() {
        return board_color;
    }
    
}
