package uk.ac.ed.inf;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import uk.ac.ed.inf.checks.EndGameCheck;
import uk.ac.ed.inf.model.Command;
import uk.ac.ed.inf.model.Config;
import uk.ac.ed.inf.model.PlayerStats;
import uk.ac.ed.inf.textProc.CharacterComparer;
import uk.ac.ed.inf.textProc.ImageProc;
import uk.ac.ed.inf.textProc.Preproc3;
import uk.ac.ed.inf.utility.FileUtil;
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
        commands.add("autoCheck");
        commands.add("outPutStats");
        commands.add("debug");
        commands.add("sepchars");
        commands.add("getText");
        commands.add("getInt");
        commands.add("compareChars");

        Command command = new Command(inputString);

        if (commands.contains(command.getAction())){
            switch (command.getAction()){
                case "exit", "e" -> exit();
                case "help", "h" -> help();
                case "clear", "c" -> clear();
                case "screenshot", "s" -> saveScreenshot();
                case "checkEndGame", "ceg" -> checkEndGame(command.getArgs());
                case "process", "p" -> process(command.getArgs());
                case "autoCheck", "ac" -> autoCheck();
                case "outPutStats", "os" -> outPutStats(command.getArgs());
                case "debug", "d" -> addRegions(command.getArgs());
                case "sepchars", "sc" -> separateChars(command.getArgs());
                case "getText", "gt" -> getText(command.getArgs());
                case "getInt", "gi" -> getInt(command.getArgs());
                case "compareChars", "cc" -> compareChars(command.getArgs());
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
        if (args.length>0 && args[0].equals("/h")){
            System.out.println("USAGE: checkEndGame [screenshot_path]");
            return false;
        }
        System.out.println("Checking if end game...");
        boolean isEndGame = EndGameCheck.check(screenshot);
        System.out.println("End game: " + isEndGame);
        return isEndGame;
    }

    public static void autoCheck() {
        System.out.println("Auto checking...");
        while (true) { 
            boolean isEndGame = EndGameCheck.check(Screenshot.getScreenshot());
            if (isEndGame) {
                System.out.println("--------------------------------- End game detected ---------------------------------");
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void autoParse(){
        System.out.println("Auto parsing...");
        while (true){
            BufferedImage screenshot = Screenshot.getScreenshot();
            if (EndGameCheck.check(screenshot)){
                // Parse the screenshot
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    public static PlayerStats parseStats(BufferedImage screenshot, int player){
        System.out.println("Parsing stats...");
        // Get regions
        Rectangle nameBox = Config.nameBox(player);
        Rectangle[] statBoxes = new Rectangle[Config.NUM_STATS];
        for (int i = 0; i < Config.NUM_STATS; i++){
            statBoxes[i] = Config.statBox(player, i);
        }
        // Get name
        String name = ImageProc.getText(ImageProc.crop(screenshot, nameBox));
        //get stats
        int[] stats = new int[Config.NUM_STATS];
        for (int i = 0; i < Config.NUM_STATS; i++){
            stats[i] = ImageProc.getInt(ImageProc.crop(screenshot, statBoxes[i]));
        }
        return new PlayerStats(name, stats);
    }

    public static String outPutStats(String[] args){
        if (args.length != 2){
            if (args.length == 1 && args[0].equals("/h")){
                System.out.println("USAGE: outPutStats [screenshot_path] [player]");
            } else {
                System.out.println("Invalid number of arguments");
            }
            return "";
        }
        return outPutStats(Screenshot.loadImage(args[0]), args[1]);
    }

    public static String outPutStats(BufferedImage screenshot, String player){
        if (!("123all".contains(player))){
            System.out.println("Invalid player");
            return "";
        }
        String stats = "";
        switch (player){
            case "1" -> stats = parseStats(screenshot, 1).toString();
            case "2" -> stats = parseStats(screenshot, 2).toString();
            case "3" -> stats = parseStats(screenshot, 3).toString();
            case "all" -> {
                stats = parseStats(screenshot, 1).toString();
                stats += parseStats(screenshot, 2).toString();
                stats += parseStats(screenshot, 3).toString();
            }
        }
        System.out.println("--------------------------------- player " + player + " stats ---------------------------------");
        System.out.println(stats);
        return stats;
    }

    public static void loadConfig(){
        System.out.println("Loading config...");
    }

    public static void process(String[] args){
        System.out.println("Processing screenshot...");
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

    public static void separateChars(String[] args){
        if (args.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        BufferedImage image = Screenshot.loadImage(args[0]);
        Preproc3 segmenter = new Preproc3();
        List<BufferedImage> characters = segmenter.segmentCharacters(image);

        System.out.println("Found " + characters.size() + " characters.");

        for (int i = 0; i < characters.size(); i++) {
            FileUtil.writeImageToFile(characters.get(i), "debug3_" + i);
        }

    }

    public static void getText(String[] args){
        if (args.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        BufferedImage image = Screenshot.loadImage(args[0]);
        String text = ImageProc.getText(image);
        System.out.println(text);
    }

    public static void getInt(String[] args){
        if (args.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        BufferedImage image = Screenshot.loadImage(args[0]);
        int number = ImageProc.getInt(image);
        System.out.println(number);
    }

    public static void compareChars(String[] args){
        if (args.length != 1){
            System.out.println("Invalid number of arguments");
            return;
        }
        Map<String, BufferedImage> templates = new java.util.HashMap<>();

        // Load templates from resources/templates. Each template is a file with a single char, named after the char.png
        File templatesDir = new File("resources/templates");
        for (File template : templatesDir.listFiles()){
            BufferedImage templateImage = Screenshot.fullLoadImage(template.getAbsolutePath());
            templates.put(template.getName().split("\\.")[0], templateImage);
        }

        // Load unknown character
        BufferedImage unknownChar = Screenshot.loadImage(args[0]);

        // Compare unknown character to templates
        CharacterComparer comparer = new CharacterComparer();
        String bestMatch = comparer.findBestMatch(unknownChar, templates);
        System.out.println("Best match: " + bestMatch);
    }
}