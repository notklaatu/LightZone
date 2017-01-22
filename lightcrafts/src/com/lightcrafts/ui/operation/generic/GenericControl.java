/* Copyright (C) 2005-2011 Fabio Riccardi */
/* Copyright (C) 2017-     Masahiro Kitagawa */

package com.lightcrafts.ui.operation.generic;

import com.lightcrafts.model.GenericOperation;
import com.lightcrafts.model.Operation;
import com.lightcrafts.model.OperationType;
import com.lightcrafts.ui.help.HelpConstants;
import com.lightcrafts.ui.layout.Box;
import com.lightcrafts.ui.operation.OpControl;
import com.lightcrafts.ui.operation.OpStack;
import com.lightcrafts.utils.xml.XMLException;
import com.lightcrafts.utils.xml.XmlNode;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.*;
import java.util.*;

import lombok.val;

public class GenericControl extends OpControl {

    // This resource bundle holds the user presentable forms of the
    // GenericOperation slider and checkbox keys.  (Choice keys are still
    // not localizable.)
    //
    // The format for the properties file is:
    //
    //     operationTypeName-keyString=userPresentableKeyString
    //
    // where "operationTypeName" is the String returned by
    // GenericOperation.getOperationType().getName().replaceAll(" ", "").

    private final static ResourceBundle Resources = ResourceBundle.getBundle(
        "com/lightcrafts/ui/operation/generic/GenericControl"
    );

    private GenericOperation op;

    // GenericOperation settings keys mapped to their control Components:

    private Map<String, GenericSlider> sliders = new HashMap<String, GenericSlider>();
    private Map<String, JCheckBox> checkboxes = new HashMap<String, JCheckBox>();
    private Map<String, JComboBox> choices = new HashMap<String, JComboBox>();

    public GenericControl(GenericOperation op, OpStack stack) {
        super(op, stack);
        operationChanged(op);
        readyForUndo();
    }

    protected void operationChanged(Operation operation) {
        super.operationChanged(operation);

        this.op = (GenericOperation) operation;

        val box = Box.createVerticalBox();

        box.add(Box.createVerticalStrut(6));

        // Add all the sliders:

        // A special layout that aligns the GenericSlider pieces in rows
        // and columns:
        val sliderContainer = new GenericSliderContainer();

        val sliderKeys = op.getSliderKeys();
        for (val key : sliderKeys) {
            val userKey = getUserPresentableKey(key);
            val config = op.getSliderConfig(key);
            val slider = new GenericSlider(userKey, config);
            slider.addChangeListener(
                    new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent event) {
                            val value = slider.getConfiguredValue();
                            op.setSliderValue(key, value);
                        }
                    }
            );
            val oldSlider = sliders.get(key);
            if (oldSlider != null) {
                slider.setConfiguredValue(oldSlider.getConfiguredValue());
            }
            slider.addSliderMouseListener(
                    new MouseAdapter() {
                        @Override
                        public void mousePressed(MouseEvent event) {
                            op.changeBatchStarted();
                        }

                        @Override
                        public void mouseReleased(MouseEvent event) {
                            op.changeBatchEnded();
                            undoSupport.postEdit(key + " Slider");
                        }
                    }
            );
            slider.setBackgroundRecurse(Background);
            slider.setFontRecurse(ControlFont);
            sliderContainer.addGenericSlider(slider);

            sliders.put(key, slider);
        }
        sliderContainer.setBackground(Background);
        box.add(sliderContainer);

        // Add all the checkboxes:

        val checkboxKeys = op.getCheckboxKeys();
        for (val key : checkboxKeys) {
            val userKey = getUserPresentableKey(key);
            val checkbox = new JCheckBox(userKey);
            checkbox.addItemListener(
                    new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent event) {
                            val value = checkbox.isSelected();
                            op.setCheckboxValue(key, value);
                            undoSupport.postEdit(key + " Checkbox");
                        }
                    }
            );
            val oldCheckbox = checkboxes.get(key);
            if (oldCheckbox != null) {
                checkbox.setSelected(oldCheckbox.isSelected());
            }
            checkbox.setBackground(Background);
            checkbox.setFont(ControlFont);
            box.add(checkbox);

            checkboxes.put(key, checkbox);
        }

        // Add all the choices:

        val choiceKeys = op.getChoiceKeys();
        for (val key : choiceKeys) {
            val values = new Vector<String>(op.getChoiceValues(key));
            val choice = new JComboBox(values);
            choice.addActionListener(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            val value = (String) choice.getSelectedItem();
                            op.setChoiceValue(key, value);
                            undoSupport.postEdit(key + " Choice");
                        }
                    }
            );
            choice.addMouseWheelListener(
                    new MouseWheelListener() {
                        @Override
                        public void mouseWheelMoved(MouseWheelEvent e) {
                            val source = (JComboBox) e.getComponent();
                            if (!source.hasFocus()) {
                                return;
                            }
                            val ni = source.getSelectedIndex() + e.getWheelRotation();
                            if (ni >= 0 && ni < source.getItemCount()) {
                                source.setSelectedIndex(ni);
                            }
                        }
                    }
            );
            val oldChoice = choices.get(key);
            if (oldChoice != null) {
                choice.setSelectedItem(oldChoice.getSelectedItem());
            }
            choice.setBackground(Background);
            choice.setFont(ControlFont);
            box.add(choice);

            choices.put(key, choice);
        }
        box.add(Box.createVerticalStrut(6));

        setContent(box);

        undoSupport.initialize();
    }

    protected void slewSlider(String key, double value) {
        val slider = sliders.get(key);
        if (slider != null) {
            slider.setConfiguredValue(value);
        }
    }

    // Find the user presentable version of the given slider or
    // checkbox key in the properties.  If none is configured, just
    // return the given String.
    private String getUserPresentableKey(String key) {
        val type = op.getType();
        val name = type.getName().replaceAll(" ", "").replaceAll("V[0-9]+\\Z", "");
        try {
            val propKey = name + "-" + key;
            return Resources.getString(propKey);
        }
        catch (MissingResourceException e) {
            return key;
        }
    }

    private final static String SliderTag = "Slider";
    private final static String CheckBoxTag = "Checkbox";
    private final static String ChoiceTag = "Choice";

    @Override
    public void save(XmlNode node) {
        super.save(node);
        val sliderNode = node.addChild(SliderTag);
        val sliderKeys = sliders.keySet();
        for (val key : sliderKeys) {
            val slider = sliders.get(key);
            val value = slider.getConfiguredValue();
            sliderNode.setAttribute(key, Double.toString(value));
        }
        val checkboxNode = node.addChild(CheckBoxTag);
        val checkboxKeys = checkboxes.keySet();
        for (val key : checkboxKeys) {
            val checkbox = checkboxes.get(key);
            val value = checkbox.isSelected();
            checkboxNode.setAttribute(key, value ? "True" : "False");
        }
        val choiceNode = node.addChild(ChoiceTag);
        val choiceKeys = choices.keySet();
        for (val key : choiceKeys) {
            val choice = choices.get(key);
            val value = (String) choice.getSelectedItem();
            choiceNode.setAttribute(key, value);
        }
    }

    @Override
    public void restore(XmlNode node) throws XMLException {
        super.restore(node);
        undoSupport.restoreStart();
        op.changeBatchStarted();
        if (node.hasChild(SliderTag)) {
            val sliderNode = node.getChild(SliderTag);
            val keys = sliders.keySet();
            for (val key : keys) {
                val slider = sliders.get(key);
                try {
                    val version = sliderNode.getVersion();
                    if ((version >= 3) || (version < 0)) {
                        val value = Double.parseDouble(sliderNode.getAttribute(key));
                        slider.setConfiguredValue(value);
                    } else {
                        val value = Integer.parseInt(sliderNode.getAttribute(key));
                        slider.setSliderPosition(value);
                    }
                } catch (NumberFormatException e) {
                    throw new XMLException(
                            "Value at attribute \"" + key + "\" is not a number", e
                    );
                }
            }
        }
        if (node.hasChild(CheckBoxTag)) {
            val checkboxNode = node.getChild(CheckBoxTag);
            val keys = checkboxes.keySet();
            for (val key : keys) {
                val checkbox = checkboxes.get(key);
                val value = checkboxNode.getAttribute(key);
                checkbox.setSelected(value.equals("True"));
            }
        }
        if (node.hasChild(ChoiceTag)) {
            val choiceNode = node.getChild(ChoiceTag);
            val keys = choices.keySet();
            for (val key : keys) {
                val choice = choices.get(key);
                val value = choiceNode.getAttribute(key);
                choice.setSelectedItem(value);
            }
        }
        op.changeBatchEnded();
        undoSupport.restoreEnd();
    }

    // This is a crude mapping from GenericOperation OperationType names
    // (as found, for instance, in opActions.properties) into help topics
    // (as defined in HelpConstants).
    //
    // This mapping needs maintenance, as tools come and go.
    @Override
    protected String getHelpTopic() {
        OperationType type = op.getType();
        String name = type.getName();
        if (name.startsWith("ZoneMapper")) {
            return HelpConstants.HELP_TOOL_ZONEMAPPER;
        }
        if (name.startsWith("UnSharp Mask")) {
            return HelpConstants.HELP_TOOL_SHARPEN;
        }
        if (name.startsWith("Gaussian Blur")) {
            return HelpConstants.HELP_TOOL_BLUR;
        }
        if (name.startsWith("Hue/Saturation")) {
            return HelpConstants.HELP_TOOL_HUE_SATURATION;
        }
        if (name.startsWith("Color Balance")) {
            return HelpConstants.HELP_TOOL_COLOR_BALANCE;
        }
        if (name.startsWith("White Point")) {
            return HelpConstants.HELP_TOOL_WHITE_BALANCE;
        }
        if (name.startsWith("Channel Mixer")) {
            return HelpConstants.HELP_TOOL_BLACK_AND_WHITE;
        }
        if (name.startsWith("Advanced Noise Reduction")) {
            return HelpConstants.HELP_TOOL_NOISE_REDUCTION;
        }
        if (name.startsWith("Clone")) {
            return HelpConstants.HELP_TOOL_CLONE;
        }
        if (name.startsWith("Spot")) {
            return HelpConstants.HELP_TOOL_SPOT;
        }
        if (name.startsWith("RAW Adjustments")) {
            return HelpConstants.HELP_TOOL_RAW_ADJUSTMENTS;
        }
        if (name.startsWith("Relight") || name.startsWith("Tone")) {
            return HelpConstants.HELP_TOOL_RELIGHT;
        }
        if (name.startsWith("Red Eyes")) {
            return HelpConstants.HELP_TOOL_RED_EYE;
        }
        // This null leads to the help home page.
        return null;
    }
}
