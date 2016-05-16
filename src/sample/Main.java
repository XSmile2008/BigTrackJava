package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.command.Argument;
import sample.command.Command;
import sample.command.Commands;
import sample.command.Parser;
import sample.utils.PointPolar;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void parserTest() {
        Parser parser = new Parser();

        parser.parse("Boot complete, free memory: ".getBytes());
        parser.parse("Initialising chassis, free memory: \r\n".getBytes());

        Command command1 = new Command(Commands.MOVE).addArgument(new Argument(Commands.DIRACTION, Commands.FORWARD));
        byte[] serialized1 = command1.serialize();

        Command command2 = new Command(Commands.STOP);
        byte[] serialized2 = command2.serialize();

        byte[] data = new byte[serialized1.length + serialized2.length];
        System.arraycopy(serialized1, 0, data, 0, serialized1.length);
        System.arraycopy(serialized2, 0, data, serialized1.length, serialized2.length);

        parser.parse(data);
        //System.out.println(Arrays.toString(command.serialize()));
    }

}
