import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowAdapter;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import javax.swing.border.*;
import javax.swing.UIManager.LookAndFeelInfo;

import org.jgroups.Message;
import com.formdev.flatlaf.FlatDarculaLaf;

public class ChannelWindow extends WindowAdapter implements ActionListener {
    JFrame frame;
    JTextPane text_area;
    JTextField tf;
    JMenuItem exitItem;
    JMenuItem aboutItem;
    JMenuItem saveItem;
    JButton sendButton;
    JButton clearButton;
    Channel channel;
    AttributeSet purpleAttributes;
    AttributeSet whiteAttributes;

    public ChannelWindow(Channel c) {
        channel = c;
        FlatDarculaLaf.install();

        generateFontAttributes();
        makeWindow();
    }

    private void generateFontAttributes() {
        Color c = Color.decode("#bd93f9");// Color.RED;

        StyleContext sc = StyleContext.getDefaultStyleContext();
        purpleAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        // purpleAttributes = sc.addAttribute(purpleAttributes,
        // StyleConstants.FontFamily, "Lucida Console");
        purpleAttributes = sc.addAttribute(purpleAttributes, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        purpleAttributes = sc.addAttribute(purpleAttributes, StyleConstants.Size, 15);

        c = Color.decode("#f8f8f2");
        sc = StyleContext.getDefaultStyleContext();
        whiteAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
        // whiteAttributes = sc.addAttribute(whiteAttributes, StyleConstants.FontFamily,
        // "Lucida Console");
        whiteAttributes = sc.addAttribute(whiteAttributes, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        whiteAttributes = sc.addAttribute(whiteAttributes, StyleConstants.Size, 15);
    }

    private void makeWindow() {
        // Creating the Frame
        frame = new JFrame("Dialogue");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        // Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("Help");

        saveItem = new JMenuItem("Save as");
        aboutItem = new JMenuItem("About");
        exitItem = new JMenuItem("Exit");

        mb.add(fileMenu);
        mb.add(helpMenu);
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);
        helpMenu.add(aboutItem);

        aboutItem.addActionListener(this);
        exitItem.addActionListener(this);

        // Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output

        JLabel label = new JLabel("Enter Text");
        tf = new JTextField(50); // accepts upto 10 characters

        sendButton = new JButton("Send");
        text_area = new JTextPane();
        text_area.setEditable(false);

        JScrollPane scroll = new JScrollPane(text_area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        clearButton = new JButton("Clear");
        sendButton.addActionListener(this);
        clearButton.addActionListener(this);
        tf.addActionListener(this);

        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(sendButton);
        panel.add(clearButton);

        // Text Area at the Center

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, scroll);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(this);
    }

    public void addMessage(Message msg) {
        ChannelMessage contents = (ChannelMessage) msg.getObject();

        printUsername(contents.getAuthor());
        printMessage(contents.getMsg());
    }

    private void printUsername(String author) {
        StyledDocument doc = text_area.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), "[" + author + "]" + ": ", purpleAttributes);
        } catch (BadLocationException e) {
            System.out.println("Couldn't insert string");
        }
    }

    private void printMessage(String msg) {
        // int len = text_area.getDocument().getLength();
        // text_area.setCaretPosition(len);
        // text_area.setCharacterAttributes(whiteAttributes, false);
        // text_area.replaceSelection(msg + "\n");
        // StyledDocument doc = text_area.getStyledDocument();
        // doc.insertString(author, doc.getLength(), purpleAttributes);
        StyledDocument doc = text_area.getStyledDocument();

        try {
            doc.insertString(doc.getLength(), msg + "\n", whiteAttributes);
        } catch (BadLocationException e) {
            System.out.println("Couldn't insert string");
        }
    }

    private void displayAbout() {
        String helpString = "Dialogue - Local network chat application.\n\n"
                + "Allows for easy communication between multiple computers on a local network. "
                + "Utilises JGroups for relible group messaging. "
                + "Any messages sent to one client will appear on other clients that are also online.";
        JOptionPane.showMessageDialog(frame, helpString);
    }

    private void clearTextArea() {
        StyledDocument doc = text_area.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            System.out.println("Couldn't insert string");
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == clearButton) {
            clearTextArea();
        } else if (src == sendButton || src == tf) {
            if (!tf.getText().isEmpty()) {
                channel.send(tf.getText());
                tf.setText("");
            }
        } else if (src == aboutItem) {
            displayAbout();
        } else if (src == exitItem) {
            channel.close();
            System.exit(0);
        }
    }

    public void windowClosing(WindowEvent e) {
        System.out.println("WindowListener method called: windowClosing.");
        channel.close();
        System.out.println("Channel closed");
    }
}
