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

import java.util.Arrays;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    static Parser parser = new Parser();
    public static void main(String[] args) {
        launch(args);
//        parser.parse("Boot complete, free memory: \r\n".getBytes());
//        parser.parse("Intialising chassis, free memomory: \r\n".getBytes());
//        Command command = new Command(Commands.MOVE).addArgument(new Argument(Commands.DIRACTION, Commands.FORWARD));
//        byte[] serialized = command.serialize();
//        command = parser.parse(serialized);
//        System.out.println(Arrays.toString(command.serialize()));
    }

}
