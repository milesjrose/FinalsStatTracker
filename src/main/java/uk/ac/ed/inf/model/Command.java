package uk.ac.ed.inf.model;
import java.util.Arrays;

public class Command {
    private final String action;
    private final String[] arguments;

    public Command(String input){
        if (input.contains(" ")){
            String[] parts = input.split(" ");
            this.action = parts[0];
            this.arguments = Arrays.copyOfRange(parts, 1, parts.length);
        }
        else{
            this.action = input;
            this.arguments = new String[0];
        }
    }

    public Command(String action, String[] arguments){
        this.action = action;
        this.arguments = arguments;
    }

    public String getAction(){
        return action;
    }

    public String[] getArgs(){
        return arguments;
    }
}