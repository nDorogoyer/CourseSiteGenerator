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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import jtps.jTPS_Transaction;
import tam.TAManagerApp;
import tam.data.TAData;
import tam.data.TeachingAssistant;
import tam.workspace.TAWorkspace;

/**
 *
 * @author noam
 */
public class removeTA_Transaction implements jTPS_Transaction{
    TAData ta;
    String name;
    String email;
    TAManagerApp app;
    HashMap<String, Label> Hmap;
    TAData tData;
    TeachingAssistant selectedTA;
    TAWorkspace workspace;
    
    removeTA_Transaction(TAData TA, String Name, String Email, TAManagerApp initApp, HashMap<String, Label> map, TAData data, TeachingAssistant selectedta, TAWorkspace ws){
        ta = TA;
        name = Name;
        email = Email;
        app = initApp;
        Hmap = map;
        tData = data;
        selectedTA = selectedta;
        workspace = ws;
    }

    @Override
    public void doTransaction() {
        Hmap = workspace.getDeepMap(workspace.getOfficeHoursGridTACellLabels(), tData);
        TAController controller;
        controller = new TAController(app);
        controller.handleDelete(selectedTA);
    }

    @Override
    public void undoTransaction() {
        //TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        workspace.resetWorkspace();
        workspace.setOHGD(Hmap);
        workspace.setPrev(true);
        int row = 1;
        for (int i = tData.getStartHour(); i < tData.getEndHour(); i++) {
            for (int col = 2; col < 7; col++){
                StringProperty tmp1 = new SimpleStringProperty((Hmap.get(tData.getCellKey(col, row))).getText());
                tData.setCellProperty(col, row, tmp1);
                StringProperty tmp2 = new SimpleStringProperty((Hmap.get(tData.getCellKey(col, row+1))).getText());
                tData.setCellProperty(col, row+1, tmp2);
            }
            row +=2;
        }
        workspace.reloadOfficeHoursGrid((TAData)app.getDataComponent());
        workspace.setPrev(false);
        ta.addTA(name, email);
    }
}
