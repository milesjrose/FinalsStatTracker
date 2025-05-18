package uk.ac.ed.inf;
// Main.java
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;


public class Main implements NativeKeyListener {

    public static void main(String[] args) {
        // Set Tesseract data path
        
        // Disable excessive logging from JNativeHook
        Logger logger = Logger.getLogger(Main.class.getName());
        logger.setLevel(Level.OFF);

        try {
            GlobalScreen.registerNativeHook();
        } catch (UnsatisfiedLinkError | NativeHookException e) {
            System.out.println("Error registering native hook: " + e.getMessage());
            return;
        }
        
        GlobalScreen.addNativeKeyListener(new Main());
        System.out.println("Press F9 to capture screenshot and extract stats.");

        System.out.println("------------- Enter Command -------------");
        while (true){
            System.out.print("> ");
            String command = System.console().readLine();
            Commands.call(command);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F9) {
            Commands.call("process");
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) { }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) { }
}
