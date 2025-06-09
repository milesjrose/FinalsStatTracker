package uk.ac.ed.inf;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.inf.checks.EndGameCheck;
import uk.ac.ed.inf.database.PlayerStatsDAO;
import uk.ac.ed.inf.model.Command;
import uk.ac.ed.inf.model.Config;
import uk.ac.ed.inf.model.PlayerStats;
import uk.ac.ed.inf.service.CollectionService;
import uk.ac.ed.inf.service.DBService;
import uk.ac.ed.inf.service.Service;
import uk.ac.ed.inf.utility.FileUtil;
import uk.ac.ed.inf.utility.Screenshot;

public class Commands {
    private static List<String> commands = new ArrayList<>();

    private static PlayerStatsDAO playerStatsDAO = new PlayerStatsDAO();

    public static void call(String inputString){
        commands = new ArrayList<>();
        commands.add("exit");
        commands.add("help");
        commands.add("clear");
        commands.add("screenshot");
        commands.add("isEnd");
        commands.add("autoParse");
        commands.add("parse");
        commands.add("regions");
        commands.add("save");
        commands.add("load");
        commands.add("init");
        commands.add("del");

        Command command = new Command(inputString);

        if (commands.contains(command.getAction())){
            switch (command.getAction()){
                case "exit" -> exit();
                case "help" -> help();
                case "clear" -> clear();
                case "screenshot" -> saveScreenshot();
                case "isEnd" -> checkEndGame(command.getArgs());
                case "procAuto" -> Service.startAutoProcessing();
                case "proc" -> saveStats(command.getArgs());
                case "parse" -> parse(command.getArgs());
                case "regions" -> addRegions(command.getArgs());
                case "load" -> loadStats(command.getArgs());
                case "init" -> initDB();
                case "del" -> deleteStats(command.getArgs());
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

    public static boolean checkEndGame(String[] args) {
        BufferedImage screenshot = args.length>0 ? Screenshot.loadImage(args[0]) : CollectionService.screenshot();
        if (args.length>0 && args[0].equals("/h")){
            System.out.println("USAGE: checkEndGame [screenshot_path]");
            return false;
        }
        System.out.println("Checking if end game...");
        boolean isEndGame = EndGameCheck.check(screenshot);
        System.out.println("End game: " + isEndGame);
        return isEndGame;
    }

    public static String parse(String[] args){
        if (args.length != 2){
            if (args.length == 1 && args[0].equals("/h")){
                System.out.println("USAGE: outPutStats [screenshot_path] [player]");
            } else {
                System.out.println("Invalid number of arguments");
            }
            return "";
        }
        return parse(Screenshot.loadImage(args[0]), args[1]);
    }

    public static String parse(BufferedImage screenshot, String player){
        if (!("123all".contains(player))){
            System.out.println("Invalid player");
            return "";
        }
        String stats = "";
        switch (player){
            case "1" -> stats = CollectionService.parse(screenshot, 1).toString();
            case "2" -> stats = CollectionService.parse(screenshot, 2).toString();
            case "3" -> stats = CollectionService.parse(screenshot, 3).toString();
            case "all" -> {
                stats = CollectionService.parse(screenshot, 1).toString();
                stats += CollectionService.parse(screenshot, 2).toString();
                stats += CollectionService.parse(screenshot, 3).toString();
            }
        }
        System.out.println("--------------------------------- player " + player + " stats ---------------------------------");
        System.out.println(stats);
        return stats;
    }

    public static void addRegions(String[] args){
        if (args.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        BufferedImage image = Screenshot.loadImage(args[0]);
        List<Rectangle> regions = Config.allRegions();
        for (Rectangle region : regions){
            image = Screenshot.addRectangleToImage(image, region);
        }
        FileUtil.writeImageToFile(image, "debug");
    }

    public static void saveStats(String path, int player){
        PlayerStats stats = CollectionService.parse(Screenshot.loadImage(path), player);
        DBService.save(stats);
        System.out.println("Stats saved: " + stats.toString());
    }

    public static void saveStats(String[] args){
        if (args.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        saveStats(args[0], 1);
        saveStats(args[0], 2);
        saveStats(args[0], 3);
    }

    public static void loadStats(String[] args){
        if (args.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        if (args[0].equals("all")){
            System.out.println("Loading all stats...");
            DBService.loadAll();
        }
        else{
            PlayerStats stats = DBService.load(args[0]);
            System.out.println("Stats loaded: " + stats.toString());
        }
    }

    public static void initDB(){
        System.out.println("Initializing database...");
        PlayerStatsDAO.initialise();
        System.out.println("Database initialized");
    }

    public static void deleteStats(String[] args){
        if (args.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        if (args[0].equals("all")){
            DBService.delAll();
        }
        else{
            DBService.del(args[0]);
        }
    }

}