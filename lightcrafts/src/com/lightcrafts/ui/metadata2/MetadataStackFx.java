/* Copyright (C) 2018- Masahiro Kitagawa */

package com.lightcrafts.ui.metadata2;

import com.lightcrafts.image.BadImageFileException;
import com.lightcrafts.image.ImageInfo;
import com.lightcrafts.image.UnknownImageTypeException;
import com.lightcrafts.image.metadata.ImageMetadata;
import com.lightcrafts.image.metadata.ImageMetadataDirectory;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Separator;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javax.swing.CellEditor;
import javax.swing.JTable;
import lombok.val;

/**
 * A vertical box holding MetadataSectionTables, or specialized error
 * components if there is trouble with metadata.
 */
class MetadataStackFx extends Pane {

    private List<MetadataTable> tables;

    MetadataStackFx(ImageInfo info) {
        tables = new LinkedList<>();
        setImage(info);
        setBackground(Background.EMPTY);
    }

    void setImage(ImageInfo info) {
        // removeAll();
        // setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        tables.clear();

        // add(new PaneTitle(LOCALE.get("MetadataTitle")));

        ImageMetadata meta = null;
        try {
            meta = info.getMetadata();
        }
        catch (BadImageFileException | IOException | UnknownImageTypeException e) {
            e.printStackTrace();
        }
        if (meta == null) {
            /*
            String no = LOCALE.get("NoLabel");
            JLabel label = new JLabel(no);
            label.setAlignmentX(.5f);
            add(Box.createVerticalGlue());
            add(label);
            add(Box.createVerticalGlue());
            */
            return;
        }
        Collection<ImageMetadataDirectory> directories = meta.getDirectories();
        if (directories.isEmpty()) {
            /*
            String empty = LOCALE.get("EmptyLabel");
            JLabel label = new JLabel(empty);
            label.setAlignmentX(.5f);
            add(Box.createVerticalGlue());
            add(label);
            add(Box.createVerticalGlue());
            */
            return;
        }

        MetadataPresentation present = new MetadataPresentation();

        List<MetadataSection> sections = present.getSections();

        for (MetadataSection section : sections) {
            val model = new MetadataTableModel(info, meta, section);
            val table = new MetadataTable(model);
            tables.add(table);

            val swingNode1 = new SwingNode();
            swingNode1.setContent(table);

            val swingNode2 = new SwingNode();
            val buttons = new DefaultButtons(table, meta);
            swingNode2.setContent(buttons);

            val control = new HBox();
            control.getChildren().addAll(swingNode1, swingNode2);
            getChildren().add(control);
            // add(Box.createVerticalStrut(4));
            getChildren().add(new Separator());
            // add(Box.createVerticalStrut(4));
        }
        // add(Box.createVerticalGlue());
    }

    boolean isEditing() {
        return tables.stream().anyMatch(JTable::isEditing);
    }

    void endEditing() {
        tables.stream().filter(JTable::isEditing)
                .map(JTable::getCellEditor)
                .forEach(CellEditor::stopCellEditing);
    }

    /*
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(
        Rectangle visibleRect, int orientation, int direction
    ) {
        // If we have any JTables, then defer to one of them:
        Component[] comps = getComponents();
        for (Component comp : comps) {
            if (comp instanceof JTable) {
                JTable table = (JTable) comp;
                return table.getScrollableBlockIncrement(
                    visibleRect, orientation, direction
                );
            }
        }
        return 1;
    }

    public int getScrollableUnitIncrement(
        Rectangle visibleRect, int orientation, int direction
    ) {
        // If we have any JTables, then defer to one of them:
        Component[] comps = getComponents();
        for (Component comp : comps) {
            if (comp instanceof JTable) {
                JTable table = (JTable) comp;
                return table.getScrollableUnitIncrement(
                    visibleRect, orientation, direction
                );
            }
        }
        return 1;
    }
    */
}
