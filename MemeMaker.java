package AppsWithGUIs;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;

import javax.imageio.ImageIO;
import java.io.*;

public class MemeMaker extends Application {

    private int counter = 1; //to keep track of the memes made

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {
        makeStage(primaryStage);
    }

    void makeStage(Stage stage)
    {
        Image image = null;
        ImageView imageView = null;

        //make a text field for the user to type what they want on the photo
        TextArea textArea = new TextArea("Type text here that you want to put on the image. Then click where you want" +
                "the text.");
        textArea.setWrapText(true);

        //make a fileChooser object
        FileChooser fileChooser = new FileChooser();

        //make a File object for the chosen file
        File selectedFile = fileChooser.showOpenDialog(stage);
        if(selectedFile != null)
        {
            //String fileName = selectedFile.getPath();
            //image = new Image("file:" + fileName);
            String fileName = selectedFile.toURI().toString();
            //print the name of the file to console
            System.out.println(fileName);
            image = new Image(fileName);
            imageView = new ImageView(image);
            imageView.setFitWidth(500);
            //only calculates height when the pane is laid out. Must declare the imageView height.
            imageView.setPreserveRatio(true);
        }

        Pane pane = new Pane(imageView);
        //we set the prefSize to center the image and prevent the text from pushing the image
        pane.setPrefSize(imageView.getFitWidth(), imageView.getFitWidth() * image.getHeight() / image.getWidth());

        //add an event handler for the imageView that prompts the user to
        imageView.setOnMouseClicked(event ->
        {
            System.out.println("Clicked imageView");
            System.out.println((event.getSceneX() - pane.getLayoutX()) + ", " + (event.getSceneY() - pane.getLayoutY()));

            //make a text field
            String textInput = textArea.getText();
            Text text = new Text(event.getSceneX() - pane.getLayoutX(), event.getSceneY() - pane.getLayoutY(), textInput);
            text.setFont(new Font("SansSerif", 36));
            text.setFill(Color.WHITE);
            pane.getChildren().add(text);
        });

        //make the ColorAdjust object for the saturation, hue, brightness, and contrast
        ColorAdjust colorAdjust = new ColorAdjust();

        //make a Glow object
        Glow glow = new Glow(0.0);

        //make a MotionBlur object
        MotionBlur motionBlur = new MotionBlur();

        //make a MenuBar
        MenuBar menuBar = new MenuBar();

        //make the File menu
        Menu fileMenu = new Menu("File");

        //make file menu items
        MenuItem newItem = new MenuItem("New");
        MenuItem resetItem = new MenuItem("Reset");
        MenuItem saveItem = new MenuItem("Save");

        //add an event handler for the newItem
        newItem.setOnAction(event ->
        {
            makeStage(new Stage());
        });

        //add an event handler for the resetItem
        resetItem.setOnAction(event ->
        {
            glow.setLevel(0);

            colorAdjust.setContrast(0);
            colorAdjust.setBrightness(0);
            colorAdjust.setHue(0);
            colorAdjust.setSaturation(0);

            motionBlur.setRadius(0);
            motionBlur.setAngle(0);

            pane.getChildren().remove(1, pane.getChildren().size());
        });

        //add an event handler for the saveItem
        saveItem.setOnAction(event ->
        {
            FileChooser directoryChooser = new FileChooser();
            File selectedDirectory = directoryChooser.showSaveDialog(stage);
            if(directoryChooser != null)
            {
                String fileName = selectedDirectory.toURI().toString();
                fileName = fileName.substring(5); // this gets rid of "file:" from the front of the string's name
                //print the name of the file to console
                System.out.println(fileName);

                File file = new File(fileName);
                WritableImage writableImage = pane.snapshot(new SnapshotParameters(), null);
                try {
                    System.out.println("Clicked save");
                    //make a buffered image???
                    ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                } catch (IOException e) {
                    System.out.println("Couldn't write to disk.");
                }
            }

            /*File file = new File("meme" + counter + ".png");
            counter++;
            WritableImage writableImage = pane.snapshot(new SnapshotParameters(), null);
            //WritableImage writableImage = imageView.snapshot(new SnapshotParameters(), null);
            try {
                System.out.println("Clicked save");
                //make a buffered image???
                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
            } catch (IOException e) {
                System.out.println("Couldn't write to disk.");
            }*/
        });

        //add the MenuItem's to the fileMenu
        fileMenu.getItems().addAll(newItem, resetItem, saveItem);

        //add the fileMenu to the menuBar
        menuBar.getMenus().add(fileMenu);

        //make a BorderPane for everything to go in
        BorderPane borderPane = new BorderPane();

        //create the glow controls
        Label glowLabel = new Label("Glow: ");
        glowLabel.setPrefWidth(75);

        Slider glowSlider = new Slider(0.0, 1.0, 0.3);
        glowSlider.setShowTickMarks(true);
        glowSlider.setMajorTickUnit(0.1);
        glowSlider.setShowTickLabels(true);
        glowSlider.setSnapToTicks(true);
        glowSlider.setOrientation(Orientation.VERTICAL);

        //create VBox for the glow controls
        VBox glowVBox = new VBox(5, glowLabel, glowSlider);
        glowVBox.setAlignment(Pos.CENTER);

        //create the saturation controls
        Label saturationLabel = new Label("Saturation: ");
        saturationLabel.setPrefWidth(100);

        Slider saturationSlider = new Slider(0.0, 1.0, 0.0);
        saturationSlider.setShowTickMarks(true);
        saturationSlider.setMajorTickUnit(0.1);
        saturationSlider.setShowTickLabels(true);
        saturationSlider.setSnapToTicks(true);
        saturationSlider.setOrientation(Orientation.VERTICAL);

        //create VBox for the saturation controls
        VBox saturationVBox = new VBox(5, saturationLabel, saturationSlider);
        saturationVBox.setAlignment(Pos.CENTER);

        //create the hue controls
        Label hueLabel = new Label("Hue: ");
        hueLabel.setPrefWidth(75);

        Slider hueSlider = new Slider(0.0, 1.0, 0.0);
        hueSlider.setShowTickMarks(true);
        hueSlider.setMajorTickUnit(0.1);
        hueSlider.setShowTickLabels(true);
        hueSlider.setSnapToTicks(true);
        hueSlider.setOrientation(Orientation.VERTICAL);

        //create VBox for the Hue controls
        VBox hueVBox = new VBox(5, hueLabel, hueSlider);
        hueVBox.setAlignment(Pos.CENTER);

        //create the brightness controls
        Label brightnessLabel = new Label("Brightness: ");
        brightnessLabel.setPrefWidth(120);

        Slider brightnessSlider = new Slider(-1.0, 1.0, 0.0);
        brightnessSlider.setShowTickMarks(true);
        brightnessSlider.setMajorTickUnit(0.1);
        brightnessSlider.setShowTickLabels(true);
        brightnessSlider.setSnapToTicks(true);
        brightnessSlider.setOrientation(Orientation.VERTICAL);

        //create VBox for the brightness conrtols
        VBox brightnessVBox = new VBox(5, brightnessLabel, brightnessSlider);
        brightnessVBox.setAlignment(Pos.CENTER);

        //create the contrast controls
        Label contrastLabel = new Label("Contrast: ");
        contrastLabel.setPrefWidth(100);

        Slider contrastSlider = new Slider(0.0, 1.0, 0.0);
        contrastSlider.setShowTickMarks(true);
        contrastSlider.setMajorTickUnit(0.1);
        contrastSlider.setShowTickLabels(true);
        contrastSlider.setSnapToTicks(true);
        contrastSlider.setOrientation(Orientation.VERTICAL);

        //create a VBox for the contrast controls
        VBox contrastVBox = new VBox(5, contrastLabel, contrastSlider);
        contrastVBox.setAlignment(Pos.CENTER);

        //create the MotionBlur controls
        Label motionBlurAngleLabel = new Label("Motion blur angle: ");
        motionBlurAngleLabel.setPrefWidth(200);

        Label motionBlurRadiusLabel = new Label("Motion blur radius: ");
        motionBlurRadiusLabel.setPrefWidth(200);

        Slider motionBlurAngleSlider = new Slider(0.0, 360.0, 0.0);
        motionBlurAngleSlider.setShowTickMarks(true);
        motionBlurAngleSlider.setMajorTickUnit(5);
        motionBlurAngleSlider.setMinorTickCount(1);
        motionBlurAngleSlider.setShowTickLabels(true);
        motionBlurAngleSlider.setSnapToTicks(true);
        motionBlurAngleSlider.setOrientation(Orientation.VERTICAL);

        Slider motionBlurRadiusSlider = new Slider(0.0, 20.0, 0.0);
        motionBlurRadiusSlider.setShowTickMarks(true);
        motionBlurRadiusSlider.setMajorTickUnit(5);
        motionBlurRadiusSlider.setMinorTickCount(1);
        motionBlurRadiusSlider.setShowTickLabels(true);
        motionBlurRadiusSlider.setSnapToTicks(true);
        motionBlurRadiusSlider.setOrientation(Orientation.VERTICAL);

        //put the motion blur sliders in their own vbox's
        VBox motionBlurAngleVBox = new VBox(5, motionBlurAngleLabel, motionBlurAngleSlider);
        motionBlurAngleVBox.setAlignment(Pos.CENTER);

        VBox motionBlurRadiusVBox = new VBox(5, motionBlurRadiusLabel, motionBlurRadiusSlider);
        motionBlurRadiusVBox.setAlignment(Pos.CENTER);

        //register event handler for the glowSlider
        glowSlider.valueProperty().addListener(((observable, oldValue, newValue) ->
        {
            //get the value from the slider
            double value = glowSlider.getValue();

            //display the value and apply it to the image
            glowLabel.setText(String.format("Glow: %.2f", value));
            glow.setLevel(value);
        }));

        //register event handler for the saturation slider
        saturationSlider.valueProperty().addListener(((observable, oldValue, newValue) ->
        {
            saturationLabel.setText(String.format("Saturation: %.2f", saturationSlider.getValue()));
            colorAdjust.setSaturation(saturationSlider.getValue());
        }));

        //register an event handler for the hue slider
        hueSlider.valueProperty().addListener(((observable, oldValue, newValue) ->
        {
            double value = hueSlider.getValue();

            hueLabel.setText(String.format("Hue: %.2f", value));
            colorAdjust.setHue(value);
        }));

        //register an event handler for the brightness slider
        brightnessSlider.valueProperty().addListener(((observable, oldValue, newValue) ->
        {
            double value = brightnessSlider.getValue();

            brightnessLabel.setText(String.format("Brightness: %.2f", value));
            colorAdjust.setBrightness(value);
        }));

        //register an event handler for the contrast slider
        contrastSlider.valueProperty().addListener(((observable, oldValue, newValue) ->
        {
            double value = contrastSlider.getValue();

            contrastLabel.setText(String.format("Contrast: %.2f", value));
            colorAdjust.setContrast(value);
        }));

        motionBlurAngleSlider.valueProperty().addListener(((observable, oldValue, newValue) ->
        {
            double value = motionBlurAngleSlider.getValue();

            motionBlurAngleLabel.setText(String.format("Motion blur angle: %.2f", value));
            motionBlur.setAngle(value);
        }));

        motionBlurRadiusSlider.valueProperty().addListener(((observable, oldValue, newValue) ->
        {
            double value = motionBlurRadiusSlider.getValue();

            motionBlurRadiusLabel.setText(String.format("Motion blur radius: %.2f", value));
            motionBlur.setRadius(value);
        }));

        //add the effects to the image
        glow.setInput(motionBlur);
        colorAdjust.setInput(glow);
        imageView.setEffect(colorAdjust);

        //create an HBox for all the sliders
        HBox sliderHBox = new HBox(glowVBox, saturationVBox, hueVBox, brightnessVBox, contrastVBox, motionBlurAngleVBox, motionBlurRadiusVBox);

        //create a VBox for the sliderHBox and TextArea
        VBox vbox = new VBox(5, textArea, sliderHBox);

        //create an HBox for all the vbox and pane
        HBox hbox = new HBox(10, vbox, pane);
        hbox.setPadding(new Insets(10));
        hbox.setFillHeight(false); //we do this so that the pane doesn't fit to the height of the pane. It stays at the height we set it at.
        hbox.setAlignment(Pos.CENTER);

        borderPane.setTop(menuBar);
        borderPane.setCenter(hbox);

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setTitle("Meme-it!");
        stage.show();
    }
}
