package com.project.sharedfolderclient;

import com.project.sharedfolderclient.v1.gui.MainFrame;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class SharedFolderClientApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = createApplicationContext(args);
        displayMainFrame(context);
    }

    private static ConfigurableApplicationContext createApplicationContext(String[] args) {
        return new SpringApplicationBuilder(SharedFolderClientApplication.class)
                .headless(false)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    private static void displayMainFrame(ConfigurableApplicationContext context) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrameController = context.getBean(MainFrame.class);
            mainFrameController.setVisible(true);
        });
    }
}
