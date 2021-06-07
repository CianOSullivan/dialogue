import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.border.*;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jgroups.Message;
import com.formdev.flatlaf.FlatDarculaLaf;

public class ChannelWindow implements ActionListener {
    JTextPane text_area;
    JMenuItem exitItem;
    JButton sendButton;
    JButton clearButton;
    Channel channel;
    AttributeSet purpleAttributes;
    AttributeSet whiteAttributes;

    public ChannelWindow(Channel c) {
        channel = c;
        // FlatIntelliJLaf.install();
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        generateFontAttributes();
        makeWindow();

        c.send("SETUP COMPLETE");
    }

    private void generateFontAttributes() {
        Color c = Color.decode("#bd93f9");// Color.RED;

        StyleContext sc = StyleContext.getDefaultStyleContext();
        purpleAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        // purpleAttributes = sc.addAttribute(purpleAttributes,
        // StyleConstants.FontFamily, "Lucida Console");
        purpleAttributes = sc.addAttribute(purpleAttributes, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        purpleAttributes = sc.addAttribute(purpleAttributes, StyleConstants.Size, 14);

        c = Color.decode("#f8f8f2");
        sc = StyleContext.getDefaultStyleContext();
        whiteAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        // whiteAttributes = sc.addAttribute(whiteAttributes, StyleConstants.FontFamily,
        // "Lucida Console");
        whiteAttributes = sc.addAttribute(whiteAttributes, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        whiteAttributes = sc.addAttribute(whiteAttributes, StyleConstants.Size, 14);
    }

    private void makeWindow() {
        // Creating the Frame
        JFrame frame = new JFrame("Chat Frame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        // Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("FILE");
        JMenu m2 = new JMenu("Help");
        mb.add(m1);
        mb.add(m2);
        JMenuItem m11 = new JMenuItem("Open");
        JMenuItem m22 = new JMenuItem("Save as");
        exitItem = new JMenuItem("Exit");

        m1.add(m11);
        m1.add(m22);
        m1.add(exitItem);
        exitItem.addActionListener(this);

        // Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output

        JLabel label = new JLabel("Enter Text");
        JTextField tf = new JTextField(60); // accepts upto 10 characters

        JButton sendButton = new JButton("Send");
        text_area = new JTextPane();

        JScrollPane scroll = new JScrollPane(text_area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(this);
        panel.add(label); // Components Added using Flow Layout
        // panel.add(scroll);
        panel.add(tf);
        panel.add(sendButton);
        panel.add(clearButton);

        // Text Area at the Center

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, scroll);
        frame.setVisible(true);
    }

    public void addMessage(Message msg) {
        // Font f = new Font(Font.SANS_SERIF, Font.BOLD, 20);
        // Font f2 = new Font(Font.SANS_SERIF, Font.BOLD, 10);
        ChannelMessage contents = (ChannelMessage) msg.getObject();

        printUsername(contents.getAuthor());
        printMessage(contents.getMsg());

        /*
         * int len = text_area.getDocument().getLength();
         * text_area.setCaretPosition(len); text_area.setCharacterAttributes(aset,
         * false); text_area.replaceSelection(contents.getAuthor() + ": " +
         * contents.getMsg() + "\n");
         */

        // System.out.println(msg.getSrc() + ": " + msg.getObject());

        // text_area.replaceSelection(msg.getObject() + "\n");

        // text_area.setFont(f);
        // text_area.append(msg.getSrc() + "");
        // text_area.setFont(f);
        // text_area.append(": " + msg.getObject() + "\n");
    }

    private void printUsername(String author) {
        int len = text_area.getDocument().getLength();
        text_area.setCaretPosition(len);
        text_area.setCharacterAttributes(purpleAttributes, false);
        text_area.replaceSelection("[" + author + "]" + ": ");
    }

    private void printMessage(String msg) {
        int len = text_area.getDocument().getLength();
        text_area.setCaretPosition(len);
        text_area.setCharacterAttributes(whiteAttributes, false);
        text_area.replaceSelection(msg + "\n");
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearButton) {
            text_area.selectAll();
            text_area.replaceSelection("");
        } else if (e.getSource() == exitItem) {
            System.exit(0);
        }
    }
}
