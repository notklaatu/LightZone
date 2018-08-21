/* Copyright (C) 2018- Masahiro Kitagawa */

package com.lightcrafts.ui.metadata2;

import com.lightcrafts.image.ImageInfo;
import java.io.File;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import lombok.val;

import static javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED;

/**
 * The top-level container for the metadata interface, a scroll pane holding
 * a stack of MetadataSections.
 */
public class MetadataScrollFx extends ScrollPane {

    private MetadataStackFx stack;
    private ImageInfo info;

    public MetadataScrollFx(ImageInfo info) {
        this();
        setImage(info);
    }

    public MetadataScrollFx() {
        setBackground(Background.EMPTY);
        setVbarPolicy(AS_NEEDED);
        setHbarPolicy(AS_NEEDED);
        setBorder(Border.EMPTY);
    }

    public void setImage(ImageInfo info) {
        // If someone just wants to refresh this component, and we're in the
        // middle of editing, then ignore the refresh.
        if ((stack != null) && stack.isEditing()) {
            if ((this.info != null) && (info != null)) {
                File file = info.getFile();
                File thisFile = this.info.getFile();
                if (file.equals(thisFile)) {
                    return;
                }
            }
        }
        endEditing();

        this.info = info;

        if (info != null) {
            if (stack != null) {
                stack.setImage(info);
            }
            else {
                stack = new MetadataStackFx(info);
            }
            setContent(stack);
        }
        else {
            setContent(null);
        }
    }

    // The control refreshes itself whenever cell editing ends.
    // Called from MetadataTable.editingStopped().
    public void refresh() {
        if (info != null) {
            File file = info.getFile();
            if (file != null) {
                info = ImageInfo.getInstanceFor(file);
                setImage(info);
            }
        }
    }

    public void endEditing() {
        if ((stack != null) && stack.isEditing()) {
            stack.endEditing();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: MetadataScroll (file)");
            System.exit(1);
        }

        val file = new File(args[0]);
        val info = ImageInfo.getInstanceFor(file);

        SwingUtilities.invokeLater(() -> {
            val frame = new JFrame("MetadataScrollFx Test");
            val fxPanel = new JFXPanel();
            frame.setContentPane(fxPanel);
            frame.setVisible(true);

            javafx.application.Platform.runLater(() -> {
                val scroll = new MetadataScrollFx(info);
                scroll.setBackground(Background.EMPTY);

                val scene = new Scene(scroll);
                fxPanel.setScene(scene);
            });
        });
    }

    // Initializer for JavaFX
    @SuppressWarnings("unused")
    static private JFXPanel fxPanel = new JFXPanel();
}
