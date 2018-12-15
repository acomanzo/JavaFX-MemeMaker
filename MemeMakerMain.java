package AppsWithGUIs;

import javafx.application.Application;
import javafx.stage.Stage;

public class MemeMakerMain extends Application {

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        MemeMaker memeMaker = new MemeMaker();
        memeMaker.makeStage(primaryStage);
    }
}
