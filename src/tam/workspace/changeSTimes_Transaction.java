/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tam.workspace;

import java.util.HashMap;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import jtps.jTPS_Transaction;
import tam.TAManagerApp;
import tam.data.TAData;

/**
 *
 * @author noam
 */
public class changeSTimes_Transaction implements jTPS_Transaction{
    int prevStart;
    int prevEnd;
    String start;
    TAManagerApp app;
    TAData data;
    HashMap<String, Label> deepMap;
    
    changeSTimes_Transaction(int PrevStart, int PrevEnd, String Start, TAManagerApp App, TAData Data){
        prevStart = PrevStart;
        prevEnd = PrevEnd;
        start = Start;
        app = App;
        data = Data;
    }

    @Override
    public void doTransaction() {
        TAController controller;
        controller = new TAController(app); 
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        deepMap = workspace.getDeepMap(workspace.officeHoursGridTACellLabels, data);
        workspace.resetWorkspace();
        workspace.officeHoursGridTACellLabels = deepMap;
        try {
            data.setStartHour(controller.timeToInt(start));
            controller.ChangeR(prevStart, prevEnd);
            workspace.prev = true;
            workspace.reloadWorkspace(app.getDataComponent());
            workspace.prev = false;
            for (int i = (2*(prevStart-data.getStartHour())); i >= 1; i--){
                for (int j = 2 ; j < 7; j++){
                    StringProperty temp = data.getCellTextProperty(j, i);
                    temp.setValue("");
                    data.setCellProperty(j, i, temp);
                }
            }
        }catch (NullPointerException E){
            System.out.println("start error caught");
        }
    }

    @Override
    public void undoTransaction() {
        TAController controller;
        controller = new TAController(app); 
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        //deepMap = workspace.getDeepMap(workspace.officeHoursGridTACellLabels, data);
        workspace.resetWorkspace();
        workspace.setOHGD(deepMap);
        int Istart = controller.timeToInt(start);
        try {
            data.setStartHour(prevStart);
            int row = 1;
            for (int i = data.getStartHour(); i < data.getEndHour(); i++) {
                for (int col = 2; col < 7; col++){
                    StringProperty tmp1 = new SimpleStringProperty((deepMap.get(data.getCellKey(col, row))).getText());
                    data.setCellProperty(col, row, tmp1);
                    StringProperty tmp2 = new SimpleStringProperty((deepMap.get(data.getCellKey(col, row+1))).getText());
                    data.setCellProperty(col, row+1, tmp2);
                }
                row +=2;
            }
            controller.ChangeR(Istart, prevEnd);
            workspace.prev = true;
            workspace.reloadWorkspace(app.getDataComponent());
            workspace.prev = false;
            for (int i = (2*(Istart-data.getStartHour())); i >= 1; i--){
                for (int j = 2 ; j < 7; j++){
                    StringProperty temp = data.getCellTextProperty(j, i);
                    temp.setValue("");
                    data.setCellProperty(j, i, temp);
                }
            }
        }catch (NullPointerException E){
            System.out.println("start error caught");
        }    }
    
}
