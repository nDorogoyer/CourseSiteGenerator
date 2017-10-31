/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tam.workspace;

import jtps.jTPS_Transaction;
import tam.data.TAData;

/**
 *
 * @author noam
 */
public class toggleTA_Transaction implements jTPS_Transaction{
    String name;
    String CellKey;
    TAData tData;
    
    toggleTA_Transaction(TAData data, String cellKey, String Name){
        name = Name;
        CellKey = cellKey;
        tData = data;
    }

    @Override
    public void doTransaction() {
        tData.toggleTAOfficeHours(CellKey, name);
        System.out.println("hi");
    }

    @Override
    public void undoTransaction() {
        tData.toggleTAOfficeHours(CellKey, name);
                System.out.println("undo");

    }
}
