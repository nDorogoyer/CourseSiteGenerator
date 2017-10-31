package tam.workspace;

import djf.controller.AppFileController;
import djf.ui.AppGUI;
import static tam.TAManagerProp.*;
import djf.ui.AppMessageDialogSingleton;
import java.util.HashMap;
import java.util.Optional;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import properties_manager.PropertiesManager;
import tam.TAManagerApp;
import tam.data.TAData;
import tam.data.TeachingAssistant;
import tam.style.TAStyle;
import static tam.style.TAStyle.CLASS_HIGHLIGHTED_GRID_CELL;
import static tam.style.TAStyle.CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN;
import static tam.style.TAStyle.CLASS_OFFICE_HOURS_GRID_TA_CELL_PANE;
import tam.workspace.TAWorkspace;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import jtps.jTPS;
import jtps.jTPS_Transaction;
import tam.TAManagerProp;


/**
 * This class provides responses to all workspace interactions, meaning
 * interactions with the application controls not including the file
 * toolbar.
 * 
 * @author Richard McKenna
 * @version 1.0
 */
public class TAController {
    // THE APP PROVIDES ACCESS TO OTHER COMPONENTS AS NEEDED
    TAManagerApp app;
    
    public String tempName;
    public String tempEmail;
    private static final String EMAIL_PATTERN =
		"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    private Matcher matcher;
    
    PropertiesManager props = PropertiesManager.getPropertiesManager();
    String updateButtonText = props.getProperty(TAManagerProp.UPDATE_BUTTON_TEXT);
    String addButtonText = props.getProperty(TAManagerProp.ADD_BUTTON_TEXT);
    
    static jTPS jTPS = new jTPS();
    
    public boolean validateEmail(String email){
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Constructor, note that the app must already be constructed.
     */
    public TAController(TAManagerApp initApp) {
        // KEEP THIS FOR LATER
        app = initApp;
    }
    
    /**
     * This helper method should be called every time an edit happens.
     */    
    private void markWorkAsEdited() {
        // MARK WORK AS EDITED
        AppGUI gui = app.getGUI();
        gui.getFileController().markAsEdited(gui);
    }
    
    public void handleAddTA() {
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        if (workspace.addButton.getText().equals(updateButtonText)){
            jTPS_Transaction transaction = new editTA_Transaction(workspace.nameTextField.getText(), workspace.emailTextField.getText(), tempName, tempEmail, app);
            jTPS.addTransaction(transaction);
        }
        else
        {
            TAData data = (TAData)app.getDataComponent();
            jTPS_Transaction transaction = new addTA_Transaction(data, workspace.nameTextField.getText(), workspace.emailTextField.getText(), app);
            jTPS.addTransaction(transaction);
            //HandleAddTA();
        }
    }
    
    /**
     * This method responds to when the user requests to add
     * a new TA via the UI. Note that it must first do some
     * validation to make sure a unique name and email address
     * has been provided.
     */
    public void HandleAddTA() {
        // WE'LL NEED THE WORKSPACE TO RETRIEVE THE USER INPUT VALUES
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        TextField nameTextField = workspace.getNameTextField();
        TextField emailTextField = workspace.getEmailTextField();
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        boolean edit = true;
        
        // WE'LL NEED TO ASK THE DATA SOME QUESTIONS TOO
        TAData data = (TAData)app.getDataComponent();
        
        // WE'LL NEED THIS IN CASE WE NEED TO DISPLAY ANY ERROR MESSAGES
        PropertiesManager props = PropertiesManager.getPropertiesManager();
        
        // DID THE USER NEGLECT TO PROVIDE A TA NAME?
        if (name.isEmpty()) {
	    AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
	    dialog.show(props.getProperty(MISSING_TA_NAME_TITLE), props.getProperty(MISSING_TA_NAME_MESSAGE));            
        }
        // DID THE USER NEGLECT TO PROVIDE A TA EMAIL?
        else if (email.isEmpty() | !validateEmail(email)) {
	    AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
	    dialog.show(props.getProperty(MISSING_TA_EMAIL_TITLE), props.getProperty(MISSING_TA_EMAIL_MESSAGE));                        
        }
        // DOES A TA ALREADY HAVE THE SAME NAME OR EMAIL?
        else if (!workspace.addButton.getText().equals("Update TA") && data.containsTA(name, email)) {
	    AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
	    dialog.show(props.getProperty(TA_NAME_AND_EMAIL_NOT_UNIQUE_TITLE), props.getProperty(TA_NAME_AND_EMAIL_NOT_UNIQUE_MESSAGE));                                    
        }
        // EVERYTHING IS FINE, ADD A NEW TA
        else {
            // ADD THE NEW TA TO THE DATA
            if (workspace.addButton.getText().equals(updateButtonText)) {
                workspace.addButton.setText(addButtonText);
                if (!name.equals(tempName) || !email.equals(tempEmail)){
                    data.removeTA(tempName);
                    HashMap<String, Label> labels = workspace.getOfficeHoursGridTACellLabels();
                    for (Label label : labels.values()) {
                        if (label.getText().equals(tempName)
                        || (label.getText().contains(tempName + "\n"))
                        || (label.getText().contains("\n" + tempName))) {
                            data.editTAInCell(label.textProperty(), tempName, name);
                        }
                    }
                }
                else
                {
                    edit = false;
                }
                
            }
            data.addTA(name, email);
            
            // CLEAR THE TEXT FIELDS
            nameTextField.setText("");
            emailTextField.setText("");
            
            // AND SEND THE CARET BACK TO THE NAME TEXT FIELD FOR EASY DATA ENTRY
            nameTextField.requestFocus();
            
            // WE'VE CHANGED STUFF
            if (edit){
            markWorkAsEdited();
            }
        }
    }

    public void handleControlDown(KeyCode code) {
        if (code == KeyCode.Z) {
            jTPS.undoTransaction();
        }
        else if (code == KeyCode.Y){
            jTPS.doTransaction();
        }
    }
    
    /**
     * This function provides a response for when the user presses a
     * keyboard key. Note that we're only responding to Delete, to remove
     * a TA.
     * 
     * @param code The keyboard code pressed.
     */
    public void handleKeyPress(KeyCode code) {
        // DID THE USER PRESS THE DELETE KEY?
        if (code == KeyCode.DELETE) {
            TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
            TableView taTable = workspace.getTATable();
            
            // IS A TA SELECTED IN THE TABLE?
            Object selectedItem = taTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                // GET THE TA AND REMOVE IT
                TeachingAssistant ta = (TeachingAssistant)selectedItem;
                String taName = ta.getName();
                TAData data = (TAData)app.getDataComponent();
                jTPS_Transaction transaction = new removeTA_Transaction(data, taName, ta.getEmail(), app, workspace.getDeepMap(workspace.officeHoursGridTACellLabels, data), data, ta, workspace);
                jTPS.addTransaction(transaction);
                //handleDelete();
            }
        }
    }
    
    public void handleDelete(TeachingAssistant ta){
        // GET THE TABLE
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        String taName = ta.getName();
        TAData data = (TAData)app.getDataComponent();
        data.removeTA(taName);

        // AND BE SURE TO REMOVE ALL THE TA'S OFFICE HOURS
        HashMap<String, Label> labels = workspace.getOfficeHoursGridTACellLabels();
        for (Label label : labels.values()) {
            if (label.getText().equals(taName)
            || (label.getText().contains(taName + "\n"))
            || (label.getText().contains("\n" + taName))) {
                data.removeTAFromCell(label.textProperty(), taName);
            }
        }
        // WE'VE CHANGED STUFF
        markWorkAsEdited();
    }

    /**
     * This function provides a response for when the user clicks
     * on the office hours grid to add or remove a TA to a time slot.
     * 
     * @param pane The pane that was toggled.
     */
    public void handleCellToggle(Pane pane) {
        // GET THE TABLE
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        TableView taTable = workspace.getTATable();
        
        // IS A TA SELECTED IN THE TABLE?
        Object selectedItem = taTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            // GET THE TA
            TeachingAssistant ta = (TeachingAssistant)selectedItem;
            String taName = ta.getName();
            TAData data = (TAData)app.getDataComponent();
            String cellKey = pane.getId();
            
            // AND TOGGLE THE OFFICE HOURS IN THE CLICKED CELL
            //data.toggleTAOfficeHours(cellKey, taName);
            
            jTPS_Transaction transaction = new toggleTA_Transaction(data, cellKey, taName);
            jTPS.addTransaction(transaction);
            
            // WE'VE CHANGED STUFF
            markWorkAsEdited();
        }
    }
    
    void handleGridCellMouseExited(Pane pane) {
        String cellKey = pane.getId();
        TAData data = (TAData)app.getDataComponent();
        int column = Integer.parseInt(cellKey.substring(0, cellKey.indexOf("_")));
        int row = Integer.parseInt(cellKey.substring(cellKey.indexOf("_") + 1));
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();

        Pane mousedOverPane = workspace.getTACellPane(data.getCellKey(column, row));
        mousedOverPane.getStyleClass().clear();
        mousedOverPane.getStyleClass().add(CLASS_OFFICE_HOURS_GRID_TA_CELL_PANE);

        // THE MOUSED OVER COLUMN HEADER
        Pane headerPane = workspace.getOfficeHoursGridDayHeaderPanes().get(data.getCellKey(column, 0));
        headerPane.getStyleClass().remove(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);

        // THE MOUSED OVER ROW HEADERS
        headerPane = workspace.getOfficeHoursGridTimeCellPanes().get(data.getCellKey(0, row));
        headerPane.getStyleClass().remove(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
        headerPane = workspace.getOfficeHoursGridTimeCellPanes().get(data.getCellKey(1, row));
        headerPane.getStyleClass().remove(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
        
        // AND NOW UPDATE ALL THE CELLS IN THE SAME ROW TO THE LEFT
        for (int i = 2; i < column; i++) {
            cellKey = data.getCellKey(i, row);
            Pane cell = workspace.getTACellPane(cellKey);
            cell.getStyleClass().remove(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
            cell.getStyleClass().add(CLASS_OFFICE_HOURS_GRID_TA_CELL_PANE);
        }

        // AND THE CELLS IN THE SAME COLUMN ABOVE
        for (int i = 1; i < row; i++) {
            cellKey = data.getCellKey(column, i);
            Pane cell = workspace.getTACellPane(cellKey);
            cell.getStyleClass().remove(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
            cell.getStyleClass().add(CLASS_OFFICE_HOURS_GRID_TA_CELL_PANE);
        }
    }

    void handleGridCellMouseEntered(Pane pane) {
        String cellKey = pane.getId();
        TAData data = (TAData)app.getDataComponent();
        int column = Integer.parseInt(cellKey.substring(0, cellKey.indexOf("_")));
        int row = Integer.parseInt(cellKey.substring(cellKey.indexOf("_") + 1));
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        
        // THE MOUSED OVER PANE
        Pane mousedOverPane = workspace.getTACellPane(data.getCellKey(column, row));
        mousedOverPane.getStyleClass().clear();
        mousedOverPane.getStyleClass().add(CLASS_HIGHLIGHTED_GRID_CELL);
        
        // THE MOUSED OVER COLUMN HEADER
        Pane headerPane = workspace.getOfficeHoursGridDayHeaderPanes().get(data.getCellKey(column, 0));
        headerPane.getStyleClass().add(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
        
        // THE MOUSED OVER ROW HEADERS
        headerPane = workspace.getOfficeHoursGridTimeCellPanes().get(data.getCellKey(0, row));
        headerPane.getStyleClass().add(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
        headerPane = workspace.getOfficeHoursGridTimeCellPanes().get(data.getCellKey(1, row));
        headerPane.getStyleClass().add(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
        
        // AND NOW UPDATE ALL THE CELLS IN THE SAME ROW TO THE LEFT
        for (int i = 2; i < column; i++) {
            cellKey = data.getCellKey(i, row);
            Pane cell = workspace.getTACellPane(cellKey);
            cell.getStyleClass().add(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
        }

        // AND THE CELLS IN THE SAME COLUMN ABOVE
        for (int i = 1; i < row; i++) {
            cellKey = data.getCellKey(column, i);
            Pane cell = workspace.getTACellPane(cellKey);
            cell.getStyleClass().add(CLASS_HIGHLIGHTED_GRID_ROW_OR_COLUMN);
        }
    }
    
    void updateSelectedTA(){
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        TableView taTable = workspace.getTATable();
        Object selectedItem = taTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
                TeachingAssistant ta = (TeachingAssistant)selectedItem;
                updateTA(ta);
                
        }
    }
    
    void updateTA(TeachingAssistant ta){
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        TextField nameTextField = workspace.getNameTextField();
        TextField emailTextField = workspace.getEmailTextField();
        nameTextField.setText(ta.getName());
        emailTextField.setText(ta.getEmail());
        tempName = ta.getName();
        tempEmail = ta.getEmail();
        workspace.addButton.setText(updateButtonText);
        markWorkAsEdited();
    }
    
    void clearTextFields(){
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        TableView taTable = workspace.getTATable();
        TextField nameTextField = workspace.getNameTextField();
                TextField emailTextField = workspace.getEmailTextField();
                nameTextField.setText("");
                emailTextField.setText("");
                nameTextField.requestFocus();
                workspace.addButton.setText(addButtonText);
    }
    
    int timeToInt(String time){
        String [] parts = time.split(":");
        int t =Integer.parseInt(parts[0]);
        if (time.contains("PM")){
            if (t != 12){
                t += 12;
            }
        }
        if (time.contains("AM")){
            if (t == 12){
                t = 0;
            }
        }
        return t;
    }
    void changeR (int prevStart, int prevEnd, String start){
        jTPS_Transaction transaction = new changeSTimes_Transaction(prevStart, prevEnd, start, app, (TAData)app.getDataComponent());
        jTPS.addTransaction(transaction);
    }
    
    
    void ChangeR (int prevStart, int prevEnd){
        TAData data = (TAData)app.getDataComponent();
        TAWorkspace workspace = (TAWorkspace)app.getWorkspaceComponent();
        boolean out = false;
        if (prevStart < data.getStartHour()){
            for (int i = 1; i <= 2*(data.getStartHour()-prevStart); i++){
                for (int j = 2 ; j < 7; j++){
                    String text = data.getCellTextProperty(j, i).getValue();
                    if (!text.equals("")){
                        out = true;
                        break;
                    }
                }
            }
        }
        if (prevEnd > data.getEndHour() && out == false){
            for (int i = 2*(data.getEndHour()-prevStart) + 1;i<=2*(prevEnd-prevStart);i++){
                for (int j = 2 ; j < 7; j++){
                    String text = data.getCellTextProperty(j, i).getValue();
                    if (!text.equals("")){
                        out = true;
                        break;
                    }
                }
            }
        }
        if (out == true){
//            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
//	    dialog.show("Office Hours Range", "Some office hours are not in the time range you specified.\nAre you sure you want to delete them?");
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Office Hours Conflict");
            alert.setHeaderText("Some office hours are outside the time bounds you specified.\nAre you sure you want to proceed?");
            //alert.setContentText("Are you ok with this?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                System.out.println("OKAAAAAY");
            } else {
                System.out.println("NAHHHHH");
                return;
            }
            
        }
        
        if (prevStart < data.getStartHour()){
            for (int i = 1; i <= 2*(prevEnd-prevStart); i++){
                for (int j = 2 ; j < 7; j++){
                    if (i < 1+2*(data.getStartHour()-prevStart)){
                        StringProperty temp = data.getCellTextProperty(j, i);
                        temp.setValue("");
                        data.setCellProperty(j, i-2*(data.getStartHour()-prevStart), temp);
                    }
                    else
                    {
                        data.setCellProperty(j, i-2*(data.getStartHour()-prevStart), data.getCellTextProperty(j, i));
                    }
                }
            }
        }
        if (prevEnd > data.getEndHour()){
            for (int i = 1; i <= 2*(prevEnd-prevStart); i++){
                for (int j = 2 ; j < 7; j++){
                    if (i > 2*(data.getEndHour()-prevStart)){
                        StringProperty temp = data.getCellTextProperty(j, i);
                        temp.setValue("");
                    }
                }
            }
        }
        
        if (prevStart > data.getStartHour()){
            for (int i = 2*(prevEnd-prevStart); i >= 1; i--){
                for (int j = 2 ; j < 7; j++){
                    data.setCellProperty(j, i+2*(prevStart-data.getStartHour()), data.getCellTextProperty(j, i));
                }
            }
        }
        markWorkAsEdited();
    }
}