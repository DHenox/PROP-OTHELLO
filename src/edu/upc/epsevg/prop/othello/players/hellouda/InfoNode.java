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
    //144 bits, 18 bytes -> 18 bytes * 2147483647
    byte indexMillorFill;
    //  num1(0=buit, 1=plena), num2(0=negra, 1=blanca)
    long num1, num2;
    //  numero de nivells per sota del node
    int nivellsPerSota;
    //  heuristica del node
    int heur;
    //  true=Exacte, false=Poda
    boolean isExact;

    public InfoNode(byte indexMillorFill, long num1, long num2, int nivellsPerSota, int heur, boolean isExact) {
        this.indexMillorFill = indexMillorFill;
        this.num1 = num1;
        this.num2 = num2;
        this.nivellsPerSota = nivellsPerSota;
        this.heur = heur;
        this.isExact = isExact;
    }
}