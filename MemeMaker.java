package AppsWithGUIs;

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
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.ColorAdjust;

import javax.imageio.ImageIO;
import java.io.*;

/**
 * Some problems I'm having right now are being able to change the color and radius
 * of the brush, and undoing nodes.
 *
 * You can't change a local (method?) variable via a lambda expression, and making the brush
 * color and radius global variables would make any changes to them affect other windows
 * of the same program. I need to be able to change its color and radius and have that
 * action be independent of anything that goes on in other windows.
 * -- solved this buy using two global arrayLists: brushColors and brushRadii, a counter variable
 * -- that keeps track of all the windows made, and a method variable for each window
 * -- that represents the number of that window.
 *
 * -- ^^ this comment is obsolete now! Now, MemeMaker is a class, and MemeMakerMain makes an object of
 * -- type MemeMaker. The way to get around the pesky lambda expressions was to use instance variables.
 * -- Now, brushRadius and brushColor are instance variable. i.e. no longer static.
 *
 * Also, I currently have the undo button designed to remove any circle from the pane.
 * I did this so that the undo would remove a whole stroke instead of a singular circle.
 * This raises a problem: If i have two different strokes, the undo action will remove
 * booth strokes instead of the most recent one. It is intended for the undo button to
 * remove the most recent addition to the pane.
 */
public class MemeMaker {

    private static int windowsOpened;

    private Color brushColor;
    private double brushRadius;
    private int windowNumber;

    private Image image = null;
    private ImageView imageView = null;
    private TextArea textArea;
    private Pane pane;

    //make the ColorAdjust object for the saturation, hue, brightness, and contrast
    private ColorAdjust colorAdjust = new ColorAdjust();

    //make a Glow object
    private Glow glow = new Glow(0.0);

    //make a MotionBlur object
    private MotionBlur motionBlur = new MotionBlur();

    private VBox glowVBox;
    private VBox saturationVBox;
    private VBox hueVBox;
    private VBox brightnessVBox;
    private VBox contrastVBox;
    private VBox motionBlurAngleVBox;
    private VBox motionBlurRadiusVBox;

    private MenuBar menuBar;

    public MemeMaker() {
        windowsOpened++;
        brushColor = Color.BLACK;
        brushRadius = 25.0;
        windowNumber = windowsOpened - 1;
    }

    public void makeStage(Stage stage)
    {
        // build the primaryStage
        makePane(stage);

        // build the menu bar
        makeMenuBar(stage);

        //make a BorderPane for everything to go in
        BorderPane borderPane = new BorderPane();

        // build the controls for the effects
        makeEffectsControls();

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

        // add stuff to the border pane
        borderPane.setTop(menuBar);
        borderPane.setCenter(hbox);

        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.setTitle("Meme-it!");
        stage.show();
    }

    /**
     * This method initializes the TextArea, lets the user choose the image for the ImageView, initializes the pane,
     * and adds event handlers for when the user clicks the pane and when the user drags the mouse
     * @param stage - The window for everything to be built on
     */
    void makePane(Stage stage) {
        //Image image = null;
        //ImageView imageView = null;

        //make a text field for the user to type what they want on the photo
        textArea = new TextArea("Type text here that you want to put on the image. Then click where you want" +
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

        pane = new Pane(imageView);
        //we set the prefSize to center the image and prevent the text from pushing the image
        pane.setPrefSize(imageView.getFitWidth(), imageView.getFitWidth() * image.getHeight() / image.getWidth());

        //add event handler to draw when the mouse is pressed
        pane.setOnMouseDragged(event ->
        {
            //get mouse coordinates
            double mouseX = event.getSceneX() - pane.getLayoutX();
            double mouseY = event.getSceneY() - pane.getLayoutY();

            //double brushRadius = brushRadii.get(windowNumber);
            //Color brushColor = brushColors.get(windowNumber);

            //make a circle object
            Circle circle = new Circle(mouseX, mouseY, brushRadius);
            circle.setFill(brushColor);

            //add the object to the pane
            pane.getChildren().add(circle);
        });

        //add an event handler for the pane to add the text from the TextArea when clicked
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
    }

    /**
     * This method makes a menu bar; a file menu with new, reset, save, and undo options, and;
     * a brush menu with options for the brush's color and size
     * @param stage - The window for everything to be built in
     */
    public void makeMenuBar(Stage stage) {
        //make a MenuBar
        menuBar = new MenuBar();

        //make the File menu
        Menu fileMenu = new Menu("File");

        //make file menu items
        MenuItem newItem = new MenuItem("New");
        MenuItem resetItem = new MenuItem("Reset");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem undoItem = new MenuItem("Undo");

        //add an event handler for the newItem
        newItem.setOnAction(event ->
        {
            new MemeMaker().makeStage(new Stage());
            //makeStage(new Stage());
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

        //add an event handler for the undo item
        undoItem.setOnAction(event ->
        {
            removeDrawing(pane.getChildren().size(), pane);
        });

        //add the MenuItem's to the fileMenu
        fileMenu.getItems().addAll(newItem, resetItem, saveItem, undoItem);

        //make a menu for the brush
        Menu brushMenu = new Menu("Brush");

        //make a submenu for the color of the brush
        Menu colorMenu = new Menu("Color");

        // make a menuitem for the colorMenu for each color
        MenuItem redItem = new MenuItem("Red");
        MenuItem blueItem = new MenuItem("Blue");
        MenuItem yellowItem = new MenuItem("Yellow");
        MenuItem blackItem = new MenuItem("Black");
        MenuItem whiteItem = new MenuItem("White");

        redItem.setOnAction(event ->
        {
            //brushColor = Color.RED;
            //setRedBrush(brushColor);
            //brushColors.set(windowNumber, Color.RED);
            brushColor = Color.RED;
        });

        blueItem.setOnAction(event ->
        {
            //brushColors.set(windowNumber, Color.BLUE);
            brushColor = Color.BLUE;
        });

        yellowItem.setOnAction(event ->
        {
            //brushColors.set(windowNumber, Color.YELLOW);
            brushColor = Color.YELLOW;
        });

        blackItem.setOnAction(event ->
        {
            //brushColors.set(windowNumber, Color.BLACK);
            brushColor = Color.BLACK;
        });

        whiteItem.setOnAction(event ->
        {
            //brushColors.set(windowNumber, Color.WHITE);
            brushColor = Color.WHITE;
        });

        // add the color items to the color sub menu
        colorMenu.getItems().add(redItem);
        colorMenu.getItems().add(blueItem);
        colorMenu.getItems().add(yellowItem);
        colorMenu.getItems().add(blackItem);
        colorMenu.getItems().add(whiteItem);

        // make a submenu for the size of the brish
        Menu sizeMenu = new Menu("Size");

        MenuItem smallItem = new MenuItem("Small");
        MenuItem mediumItem = new MenuItem("Medium");
        MenuItem largeItem = new MenuItem("Large");

        smallItem.setOnAction(event ->
        {
            //brushRadii.set(windowNumber, 5.0);
            brushRadius = 5.0;
        });

        mediumItem.setOnAction(event ->
        {
            //brushRadii.set(windowNumber, 15.0);
            brushRadius = 15.0;
        });

        largeItem.setOnAction(event ->
        {
            //brushRadii.set(windowNumber, 25.0);
            brushRadius = 25.0;
        });

        sizeMenu.getItems().addAll(smallItem, mediumItem, largeItem);

        // add the color submenu to the brush menu
        brushMenu.getItems().addAll(colorMenu, sizeMenu);

        //add the fileMenu to the menuBar
        menuBar.getMenus().add(fileMenu);

        //add the brushMenu to the menuBar
        menuBar.getMenus().add(brushMenu);
    }

    /**
     * This method creates the controls for the glow, saturation, hue, brightness, contrast,
     * motionBlue angle and motionBlue radius.
     */
    public void makeEffectsControls() {
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
        glowVBox = new VBox(5, glowLabel, glowSlider);
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
        saturationVBox = new VBox(5, saturationLabel, saturationSlider);
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
        hueVBox = new VBox(5, hueLabel, hueSlider);
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
        brightnessVBox = new VBox(5, brightnessLabel, brightnessSlider);
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
        contrastVBox = new VBox(5, contrastLabel, contrastSlider);
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
        motionBlurAngleVBox = new VBox(5, motionBlurAngleLabel, motionBlurAngleSlider);
        motionBlurAngleVBox.setAlignment(Pos.CENTER);

        motionBlurRadiusVBox = new VBox(5, motionBlurRadiusLabel, motionBlurRadiusSlider);
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
    }

    //make a recursive method to remove "drawn" circles with the brush

    /**
     * This method recursively goes through the elements of the Pane to remove the last element added
     * @param index - The size of the array holding the Pane's children
     * @param pane - The pane whose children the method will remove
     */
    public void removeDrawing(int index, Pane pane)
    {
        if(pane.getChildren().size() > 1) // if the Pane holds something other than the ImageView
        {
            if(pane.getChildren().get(index - 1) instanceof Circle) // check if you're removing a circle
            {
                if(pane.getChildren().get(index - 2) instanceof Circle) // check if the next item in the list is a circle
                {
                    removeDrawing(index - 1, pane); // call the method
                }
            }
            pane.getChildren().remove(pane.getChildren().size() - 1); // remove the first item in the list
        }
    }
}
