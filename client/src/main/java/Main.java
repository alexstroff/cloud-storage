import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class Main extends Application {
        Controller c;

        @Override
        public void start(Stage primaryStage) throws Exception{
            String fxmlFile = "/FXML/sample.fxml";
            FXMLLoader loader = new FXMLLoader();

            Parent root = loader.load(getClass().getResourceAsStream(fxmlFile));
            primaryStage.setTitle("Chat");
            c = loader.getController();
            primaryStage.setScene(new Scene(root, 440, 375));
            primaryStage.getIcons().add(new Image("icon.png"));
            primaryStage.show();

            primaryStage.setOnCloseRequest((WindowEvent event) -> {
                c.Dispose();
                Platform.exit();
                System.exit(0);
            });
       }


        public static void main(String[] args) {
            launch(args);
        }
    }


