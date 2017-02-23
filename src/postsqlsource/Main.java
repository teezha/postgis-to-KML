package postsqlsource;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application  implements EventHandler<WindowEvent>{

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
        primaryStage.setTitle("PostgreSQL MAAAAAAGIC");
        primaryStage.setScene(new Scene(root, 600, 800));
        primaryStage.show();
        primaryStage.setOnCloseRequest(this);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void handle(WindowEvent event) {
        System.exit(0);
    }
}
