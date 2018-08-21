package com.lightcrafts.ui.metadata2;

import com.lightcrafts.image.ImageInfo;
import java.io.File;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.stage.Stage;
import lombok.val;

public class MatadataScrollFxLaunch extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        val file = new File("/tmp/a.raf");
        val info = ImageInfo.getInstanceFor(file);

        stage.setTitle("MetadataScrollFx Test");

        val scroll = new MetadataScrollFx(info);
        scroll.setBackground(Background.EMPTY);

        val scene = new Scene(scroll);
        stage.setScene(scene);
        stage.show();
    }
}
