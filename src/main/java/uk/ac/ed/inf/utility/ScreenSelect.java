package uk.ac.ed.inf.utility;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenSelect {
    private static final Logger logger = LoggerFactory.getLogger(ScreenSelect.class);
    /**
     * Returns a list of all connected monitors
     * @return List of monitor information including ID, resolution, and refresh rate
     */
    public static List<String> getConnectedMonitors() {
        List<String> monitors = new ArrayList<>();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();
        logger.info("Found {} monitors", devices.length);

        for (int i = 0; i < devices.length; i++) {
            GraphicsDevice device = devices[i];
            String monitorInfo = String.format("Monitor %d: %s - %dx%d @ %dHz",
                i + 1,
                device.getIDstring(),
                device.getDisplayMode().getWidth(),
                device.getDisplayMode().getHeight(),
                device.getDisplayMode().getRefreshRate());
            monitors.add(monitorInfo);
        }
        
        return monitors;
    }
}