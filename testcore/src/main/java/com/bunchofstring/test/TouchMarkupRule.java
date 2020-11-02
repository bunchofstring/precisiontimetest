package com.bunchofstring.test;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TouchMarkupRule extends LifecycleTestRule {

    private static final Logger LOGGER = Logger.getLogger(TouchMarkupRule.class.getSimpleName());

    @Override
    public void before(){
        showVisualIndicators();
    }

    @Override
    public void after(){
        hideVisualIndicators();
    }

    public static boolean showVisualIndicators(){
        try {
            CoreUtils.executeShellCommand("content insert --uri content://settings/system --bind name:s:pointer_location --bind value:i:1");
            CoreUtils.executeShellCommand("content insert --uri content://settings/system --bind name:s:show_touches --bind value:i:1");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not show visual indicators", e);
            return false;
        }
    }

    public static boolean hideVisualIndicators(){
        try {
            CoreUtils.executeShellCommand("content insert --uri content://settings/system --bind name:s:pointer_location --bind value:i:0");
            CoreUtils.executeShellCommand("content insert --uri content://settings/system --bind name:s:show_touches --bind value:i:0");
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not hide visual indicators", e);
            return false;
        }
    }
}