import javax.swing.*;
import java.awt.*;

import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.border.*;

import org.jgroups.Message;

public class ChannelWindow {
    JTextPane text_area;
    JButton sendButton;
    JButton clearButton;

    public ChannelWindow() {
        makeWindow();

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
        m1.add(m11);
        m1.add(m22);

        // Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output

        JLabel label = new JLabel("Enter Text");
        JTextField tf = new JTextField(60); // accepts upto 10 characters

        JButton sendButton = new JButton("Send");
        clearButton = new JButton("Clear");
        panel.add(label); // Components Added using Flow Layout
        panel.add(tf);
        panel.add(sendButton);
        panel.add(clearButton);

        // Text Area at the Center
        text_area = new JTextPane();

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mb);
        frame.getContentPane().add(BorderLayout.CENTER, text_area);
        frame.setVisible(true);
    }

    public void addMessage(Message msg) {
        // Font f = new Font(Font.SANS_SERIF, Font.BOLD, 20);
        // Font f2 = new Font(Font.SANS_SERIF, Font.BOLD, 10);
        ChannelMessage contents = (ChannelMessage) msg.getObject();
        Color c = Color.RED;
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        // System.out.println(msg.getSrc() + ": " + msg.getObject());

        int len = text_area.getDocument().getLength();
        text_area.setCaretPosition(len);
        text_area.setCharacterAttributes(aset, false);
        // text_area.replaceSelection(msg.getObject() + "\n");
        text_area.replaceSelection(contents.getAuthor() + ": " + contents.getMsg() + "\n");

        // text_area.setFont(f);
        // text_area.append(msg.getSrc() + "");
        // text_area.setFont(f);
        // text_area.append(": " + msg.getObject() + "\n");
    }
}
