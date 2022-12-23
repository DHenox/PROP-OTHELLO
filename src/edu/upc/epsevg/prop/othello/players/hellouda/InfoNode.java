/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upc.epsevg.prop.othello.players.hellouda;

/**
 *
 * @author HENOK
 */
public class InfoNode {
    byte indexMillorFill;
    //  num1(0=buit, 1=plena), num2(0=negra, 1=blanca)
    long num1, num2;
    //  numero de nivells per sota del node
    int nivellsPerSota;
    //  heuristica del node
    int heur;
    //  true=Exacte, false=Poda
    boolean isExact;

    public InfoNode(byte indexMillorFill, int nivellsPerSota, int heur, boolean isExact, MyGameStatus myGameStatus) {
        this.indexMillorFill = indexMillorFill;
        this.num1 = myGameStatus.getBoard_occupied().toLongArray()[0];
        this.num2 = myGameStatus.getBoard_color().toLongArray()[0];
        this.nivellsPerSota = nivellsPerSota;
        this.heur = heur;
        this.isExact = isExact;
    }
}
