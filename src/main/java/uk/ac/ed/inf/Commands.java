package uk.ac.ed.inf;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.inf.checks.EndGameCheck;
import uk.ac.ed.inf.model.Command;
import uk.ac.ed.inf.utility.Screenshot;

public class Commands {
    private static List<String> commands = new ArrayList<>();

    public static void call(String inputString){
        commands = new ArrayList<>();
        commands.add("exit");
        commands.add("help");
        commands.add("clear");
        commands.add("screenshot");
        commands.add("checkEndGame");
        commands.add("process");

        Command command = new Command(inputString);

        if (commands.contains(command.getAction())){
            switch (command.getAction()){
                case "exit", "e" -> exit();
                case "help", "h" -> help();
                case "clear", "c" -> clear();
                case "screenshot", "s" -> saveScreenshot();
                case "checkEndGame", "ceg" -> checkEndGame(command.getArgs());
                case "process", "p" -> process(command.getArgs());
            }
        }
        else{
            System.out.println("Invalid command");
        }
    }

    public static void exit() {
        System.exit(0);
    }

    public static void help() {
        System.out.println("Available commands:");
        for (String cmd : commands){
            System.out.println(cmd);
        }
    }

    public static void clear() {
        System.out.println("Clearing screenshots...");
        Screenshot.clearFiles();
    }

    public static void saveScreenshot() {
        System.out.println("Saving screenshot...");
        String screenshotPath = Screenshot.saveScreenshot();
        System.out.println("Screenshot saved at: " + screenshotPath);
    }

    public static BufferedImage screenshot() {
        System.out.println("Capturing screenshot...");
        BufferedImage screenshot = Screenshot.getScreenshot();
        return screenshot;
    }

    public static boolean checkEndGame(String[] args) {
        BufferedImage screenshot = args.length>0 ? Screenshot.loadImage(args[0]) : screenshot();
        System.out.println("Checking if end game...");
        boolean isEndGame = EndGameCheck.check(screenshot);
        System.out.println("End game: " + isEndGame);
        return isEndGame;
    }

    public static void process(String[] args){
        System.out.println("Processing screenshot...");
    }
}