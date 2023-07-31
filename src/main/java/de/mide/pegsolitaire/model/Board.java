package de.mide.pegsolitaire.model;

import android.content.Context;

import java.util.List;

import de.mide.pegsolitaire.model.PlaceStatusEnum;

public class Board {
    String name;
    private int rows;
    private int columns;

    private List<List<PlaceStatusEnum>> board = null;



    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }



    public PlaceStatusEnum get(int column,int row){
        return board.get(column).get(row);
    }

    public void setRC(){
        this.columns = board.size();
        this.rows = board.get(0).size();
    }

    public PlaceStatusEnum[][] getBoard() {
        PlaceStatusEnum[][] array = new PlaceStatusEnum[columns][rows];
        for(int c = 0;c<columns;c++){
            for(int r = 0;r<rows;r++){
                array[c][r] = get(c,r);
            }
        }
        return array;
    }

    public String getName() {
        return name;
    }
}
