/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tam.workspace;

import javafx.scene.control.TextField;
import jtps.jTPS_Transaction;
import properties_manager.PropertiesManager;
import tam.TAManagerApp;
import tam.TAManagerProp;

/**
 *
 * @author noam
 */
public class editTA_Transaction implements jTPS_Transaction{
    String name;
    String email;
    String oldName;
    String oldEmail;
    TAManagerApp app;
    
    
    editTA_Transaction(String Name, String Email, String OldName, String OldEmail, TAManagerApp initApp){
        name = Name;
        email = Email;
        oldName = OldName;
        oldEmail = OldEmail;
        app = initApp;
    }

    @Override
    public void doTransaction() {
        TAController controller;
        controller = new TAController(app);
        controller.tempName = oldName;
        controller.tempEmail = oldEmail;
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        workspace.nameTextField.setText(name);
        workspace.emailTextField.setText(email);
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String updateButtonText = props.getProperty(TAManagerProp.UPDATE_BUTTON_TEXT);
        workspace.addButton.setText(updateButtonText);
        controller.HandleAddTA();
    }

    @Override
    public void undoTransaction() {
        TAController controller;
        controller = new TAController(app);
        controller.tempName = name;
        controller.tempEmail = email;
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        workspace.nameTextField.setText(oldName);
        workspace.emailTextField.setText(oldEmail);
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        String updateButtonText = props.getProperty(TAManagerProp.UPDATE_BUTTON_TEXT);
        workspace.addButton.setText(updateButtonText);
        controller.HandleAddTA();    }
    
}
