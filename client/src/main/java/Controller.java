import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;

public class Controller {

    @FXML
    TextField setLoginField;

    @FXML
    TextField setNicknameField;

    @FXML
    TextField setPasswordField;

    @FXML
    ListView<String> clientList;

    @FXML
    TextArea textArea;

    @FXML
    TextField textField;

    @FXML
    HBox bottomPanel;

    @FXML
    HBox regPanel;

    @FXML
    HBox upperPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passwordField;


    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    FileInputStream fis;
    FileOutputStream fos;


    final String IP_ADRESS = "localhost";
    final int PORT = 8189;

    private boolean isAuthorised;

    public void  setAuthorised(boolean isAuthorised){
        this.isAuthorised = isAuthorised;
        if(!isAuthorised){
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            regPanel.setManaged(false);
            regPanel.setVisible(false);
        }else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            regPanel.setManaged(false);
            regPanel.setVisible(false);
        }
    }

    public void  setRegistration(boolean isAuthorised){
        this.isAuthorised = isAuthorised;
        if(!isAuthorised){
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
            regPanel.setManaged(false);
            regPanel.setVisible(false);
        }else {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
            regPanel.setManaged(false);
            regPanel.setVisible(false);
        }
    }

    public void connect() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());


            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String srt = in.readUTF();
                            if (srt.equals("/authok")) {
                                setAuthorised(true);
                                textArea.appendText("Авторизация прошла успешно" + "\n");

                                break;
                            }else if (srt.equals("/regok")) {
                                textArea.appendText("Регистрация прошла успешно" + "\n");
                                setRegistration(true);
                            }else {
                                textArea.appendText(srt + "\n");
                            }
                        }


                        while (true) {
                            String str = in.readUTF();
                            if (str.startsWith("/")) {
                                if (str.equals("/serverclosed")) break;
                                if (str.startsWith("/clientslist ")) {
                                    String[] tokens = str.split(" ");
                                    Platform.runLater(() -> {
                                        clientList.getItems().clear();
                                        for (int i = 1; i < tokens.length; i++) {
                                            clientList.getItems().add(tokens[i]);
                                        }
                                    });
                                }
                            } else {
                                textArea.appendText(str + "\n");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setAuthorised(false);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void Dispose() {
        System.out.println("Отправляем сообщение на сервер о завершении работы");
        try {
            if (out != null) {
                out.writeUTF("/end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent event) {
        if(socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectClient(MouseEvent mouseEvent) {
    }


    public void regpanel(ActionEvent event) {
        upperPanel.setVisible(false);
        upperPanel.setManaged(false);
        bottomPanel.setVisible(false);
        bottomPanel.setManaged(false);
        regPanel.setManaged(true);
        regPanel.setVisible(true);
    }

    public void reg(ActionEvent event) {
        if(socket == null || socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF("/reg " + setNicknameField.getText() + " " + setLoginField.getText() + " " +
                    setPasswordField.getText());
            setNicknameField.clear();
            setLoginField.clear();
            setPasswordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFile(ActionEvent actionEvent) {
        try {
            out.writeUTF("/sendFile");
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File("troll.avi");

        System.out.println("START TRANS");
        try(FileInputStream fis = new FileInputStream(file)){
        BufferedOutputStream bof = new BufferedOutputStream(out, 10240);
        int x;
        byte[] buffer = new byte[10240];
        while ((x = fis.read(buffer)) != -1){
            bof.write(buffer, 0, x);
            bof.flush();
        }
            System.out.println("/endOfTrans");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("client send");
    }
}
