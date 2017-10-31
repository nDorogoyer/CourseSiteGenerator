/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tam.workspace;

import jtps.jTPS_Transaction;
import tam.TAManagerApp;
import tam.data.TAData;

/**
 *
 * @author noam
 */
public class addTA_Transaction implements jTPS_Transaction{
    TAData ta;
    String name;
    String email;
    TAManagerApp app;
    
    addTA_Transaction(TAData TA, String Name, String Email, TAManagerApp App){
        ta = TA;
        name = Name;
        email = Email;
        app = App;
    }

    @Override
    public void doTransaction() {
        TAController controller;
        controller = new TAController(app);
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        workspace.nameTextField.setText(name);
        workspace.emailTextField.setText(email);
        controller.HandleAddTA();
        //ta.addTA(name, email);
    }

    @Override
    public void undoTransaction() {
        ta.removeTA(name);
    }
}
