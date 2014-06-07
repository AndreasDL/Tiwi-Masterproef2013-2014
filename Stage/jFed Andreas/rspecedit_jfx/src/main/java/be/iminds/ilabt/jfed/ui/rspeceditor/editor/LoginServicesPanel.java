package be.iminds.ilabt.jfed.ui.rspeceditor.editor;

import be.iminds.ilabt.jfed.ui.rspeceditor.model.RspecNode;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * ExecuteServicesPanel
 */
public class LoginServicesPanel extends AbstractServicesPanel<RspecNode.LoginService> {

    @Override
    protected RspecNode.LoginService emptyToAdd() {
        throw new RuntimeException("Adding by user is not allowed for LoginService");
        //return new RspecNode.LoginService("error", "error", "error", "error");
    }

    @Override
    protected int getColumnCount() {
        return 4;
    }

    protected String columnName(int colIndex) {
        switch (colIndex) {
            case 0: return "Authentication";
            case 1: return "Username";
            case 2: return "Port";
            case 3: return "Hostname";
            default : return "Unknown collumn";
        }
    }

    @Override
    protected ObservableValue<String> getColumnValue(RspecNode.LoginService item, int colIndex) {
        switch (colIndex) {
            case 0: return item.authenticationProperty();
            case 1: return item.usernameProperty();
            case 2: return item.portProperty();
            case 3: return item.hostnameProperty();
            default : throw new RuntimeException("no such column "+colIndex);
        }
    }

    @Override
    protected void setColumnValue(RspecNode.LoginService item, int colIndex, String value) {
        //allow adding/changing anyway. problem is that even when not editing, we can be called
//        throw new RuntimeException("Adding by user is not allowed for LoginService");
        switch (colIndex) {
            case 0: { item.setAuthentication(value); break; }
            case 1: { item.setUsername(value); break; }
            case 2: { item.setPort(value); break; }
            case 3: { item.setHostname(value); break; }
            default : throw new RuntimeException("no such column "+colIndex);
        }
    }

    class LoginButtonCell extends TableCell<RspecNode.LoginService, String> {
        private Button button;

        public LoginButtonCell() {
            setEditable(false);
            button = new Button("Login...");
            button.setOnAction(new EventHandler<ActionEvent>(){
                @Override
                public void handle(ActionEvent actionEvent) {
                    final RspecNode.LoginService loginService = (RspecNode.LoginService) getTableRow().getItem();

                    //run process from new thread (which processes it's output)
                    Runnable processOutputMonitor = new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null,
                                    "This option is currently proof of concept only. It needs to be rewritten. Currently, it only works on windows, and only if the working dir of this application contains putty.exe and a putty key file name \"private_ssh_key.ppk\" containing your SSH private key.",
                                    "Info", JOptionPane.INFORMATION_MESSAGE);
                            Process p = null;
                            try {
//                                p = Runtime.getRuntime().exec("putty.exe -ssh "+loginService.getUsername()+"@"+loginService.getHostname());
                                p = Runtime.getRuntime().exec("putty.exe -ssh "+loginService.getUsername()+"@"+loginService.getHostname()+" -i private_ssh_key.ppk");
                                BufferedReader input = new BufferedReader (new InputStreamReader(p.getInputStream()));
                                String line = null;
                                while ((line = input.readLine()) != null) {
                                    System.out.println("LoginService putty output: "+line);
                                }
                                input.close();
                            } catch (IOException e) {
                                System.err.println("Exception while invoking subprocess for LoginService");
                                e.printStackTrace();
                            }
                        }
                    };
                    Thread t = new Thread(processOutputMonitor);
                    t.setDaemon(true); //=> this application may exit before putty stops
                    t.start();
                }
            });
            setText(null);
            if (getTableRow() == null || getTableRow().getItem() == null)
                setGraphic(null);
            else
                setGraphic(button);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (getTableRow() == null || getTableRow().getItem() == null)
                setGraphic(null);
            else
                setGraphic(button);
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);

        Callback<TableColumn, TableCell> loginButtonCellFactory =
                new Callback<TableColumn, TableCell>() {
                    public TableCell call(TableColumn p) {
                        return new LoginButtonCell();
                    }
                };

        TableColumn loginCol = new TableColumn("");
        loginCol.setCellFactory(loginButtonCellFactory);
        table.getColumns().addAll(loginCol);
    }
}
