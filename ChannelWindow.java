import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.jgroups.Address;

import com.formdev.flatlaf.FlatDarculaLaf;

/**
 * Generates a window to the channel and
 */
public class ChannelWindow extends WindowAdapter implements ActionListener {
    private final EventLogger log;

    JFrame frame;
    JTextPane text_area;
    JTextField tf;
    JMenuItem exitItem;
    JMenuItem aboutItem;
    JMenuItem saveItem;
    JMenuItem uploadItem;
    JMenuItem syncKeyItem;
    JMenuItem genKeyItem;
    JMenuItem listMembers;

    JButton sendButton;
    JButton clearButton;
    Channel channel;
    AttributeSet purpleAttributes;
    AttributeSet whiteAttributes;
    SecretKey aesKey;

    public ChannelWindow(Channel c, SecretKey key, EventLogger l) {
        channel = c;
        aesKey = key;
        log = l;
        FlatDarculaLaf.install();

        generateFontAttributes();
        makeWindow();
    }

    private void generateFontAttributes() {
        StyleContext sc = StyleContext.getDefaultStyleContext();

        purpleAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground,
                Color.decode("#bd93f9"));
        purpleAttributes = sc.addAttribute(purpleAttributes, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        purpleAttributes = sc.addAttribute(purpleAttributes, StyleConstants.Size, 15);
        whiteAttributes = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.decode("#f8f8f2"));
        whiteAttributes = sc.addAttribute(whiteAttributes, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        whiteAttributes = sc.addAttribute(whiteAttributes, StyleConstants.Size, 15);
    }

    private void makeWindow() {
        // Creating the Frame
        frame = new JFrame("Dialogue");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);

        // Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu toolsMenu = new JMenu("Tools");

        JMenu helpMenu = new JMenu("Help");

        uploadItem = new JMenuItem("Upload file");
        genKeyItem = new JMenuItem("Generate new key");
        syncKeyItem = new JMenuItem("Sync key to all channel members");
        listMembers = new JMenuItem("List all channel members");

        saveItem = new JMenuItem("Save as");
        aboutItem = new JMenuItem("About");
        exitItem = new JMenuItem("Exit");

        mb.add(fileMenu);
        mb.add(toolsMenu);
        mb.add(helpMenu);
        fileMenu.add(uploadItem);
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);
        toolsMenu.add(genKeyItem);
        toolsMenu.add(syncKeyItem);
        toolsMenu.add(listMembers);
        helpMenu.add(aboutItem);

        uploadItem.addActionListener(this);
        saveItem.addActionListener(this);
        genKeyItem.addActionListener(this);
        syncKeyItem.addActionListener(this);
        listMembers.addActionListener(this);

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

    public void processMessage(Message msg) {
        try {
            Address source = msg.getSrc();
            SealedObject sealedContents = (SealedObject) msg.getObject();
            ChannelMessage contents = (ChannelMessage) sealedContents.getObject(aesKey);
            if (contents.isFile()) {
                if (source != channel.getLocalAddress())
                    saveFile(contents.getFileMeta(), contents.getFile());
            } else {
                printUsername(contents.getAuthor());
                printMessage(contents.getMsg());
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | ClassNotFoundException | IOException e) {
            log.error("Couldn't unseal message" + e);
        } catch (ClassCastException ce) {
            ChannelMessage contents = (ChannelMessage) msg.getObject();
            if (contents.isKey()) {
                acceptKey(contents.getKey());
            }
        }
    }

    private void saveFile(File fileMeta, byte[] file) {
        String file_loc = System.getProperty("user.home") + "/Downloads/" + fileMeta.getName();
        log.info("Saving file" + file_loc);

        // Also check if src is this machine
        if (new File(file_loc).isFile()) {
            log.info("File already exists");
            JOptionPane.showMessageDialog(frame, "File already exists", "File already exists",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            // if accept file == yes
            int dialogResult = JOptionPane.showConfirmDialog(null, "Accept file?: " + fileMeta.getName(), "Warning",
                    JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                try (FileOutputStream fos = new FileOutputStream(file_loc)) {
                    fos.write(file);
                } catch (IOException e) {
                    log.error("Couldn't write file: " + e);
                }
            }
        }
    }

    private void printUsername(String author) {
        StyledDocument doc = text_area.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), "[" + author + "]" + ": ", purpleAttributes);
        } catch (BadLocationException e) {
            log.error("Couldn't insert string");
        }
    }

    private void printMessage(String msg) {
        StyledDocument doc = text_area.getStyledDocument();

        try {
            doc.insertString(doc.getLength(), msg + "\n", whiteAttributes);
        } catch (BadLocationException e) {
            log.error("Couldn't insert string");
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
            log.error("Couldn't insert string");
        }
    }

    private void listMembers() {
        String members = "Members connected to cluster: \n";

        for (Address a : channel.getView().getMembers()) {
            members += a.toString() + "\n";
        }

        JOptionPane.showMessageDialog(frame, members);
    }

    private void saveTranscript() {
        try {
            StyledDocument doc = text_area.getStyledDocument();
            String text = doc.getText(0, doc.getLength());
            String home = System.getProperty("user.home");
            String file_loc = home + "/Downloads/chat_transcript_"
                    + new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(new Date()) + ".txt";
            BufferedWriter out = new BufferedWriter(new FileWriter(file_loc));
            out.write(text); // Replace with the string
            out.close();

        } catch (BadLocationException | IOException e) {
            log.error("Couldn't save transcript: " + e);
        }

    }

    private void makeKey() {
        // Generate a new cipher and add key to server
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            SecretKey newKey = kgen.generateKey();

            if (new File("key.txt").isFile()) {
                log.warning("Key file already exists");

                int dialogResult = JOptionPane.showConfirmDialog(null, "Key already exists. Overwrite?", "Warning",
                        JOptionPane.YES_NO_OPTION);
                if (dialogResult == JOptionPane.YES_OPTION) {
                    try (FileOutputStream key_file = new FileOutputStream("key.txt")) {
                        key_file.write(newKey.getEncoded());
                    }
                    aesKey = newKey;
                    channel.updateKey(aesKey);
                }

            } else {
                try (FileOutputStream key_file = new FileOutputStream("key.txt")) {
                    key_file.write(newKey.getEncoded());
                }
                aesKey = newKey;
                channel.updateKey(aesKey);

            }

        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("An error occurred generating key: " + e);
        }
    }

    private void acceptKey(SecretKey key) {
        int dialogResult = JOptionPane.showConfirmDialog(null, "Accept new key?", "Warning", JOptionPane.YES_NO_OPTION);
        if (dialogResult == JOptionPane.YES_OPTION) {
            try (FileOutputStream key_file = new FileOutputStream("key.txt")) {
                key_file.write(key.getEncoded());
            } catch (IOException e) {
                log.error("Couldn't accept key: " + e);
            }
            aesKey = key;
            channel.updateKey(aesKey);
        }
    }

    private void syncKey() {
        channel.send(aesKey);
    }

    /**
     * Upload files to the group
     */
    private void uploadFile() {
        log.info("Attempting file upload");
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);

        // Upload files if selected
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            for (File file : chooser.getSelectedFiles()) {
                try {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    channel.send(file, fileContent);
                    channel.send("FILE INCOMING: " + file.getName());
                } catch (IOException e) {
                    log.error("Couldn't send file: " + e);
                }
            }
        }
    }

    /**
     * Run appropriate method when buttons are pressed
     */
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
        } else if (src == uploadItem) {
            uploadFile();
        } else if (src == listMembers) {
            listMembers();
        } else if (src == genKeyItem) {
            makeKey();
        } else if (src == saveItem) {
            saveTranscript();
        } else if (src == syncKeyItem) {
            syncKey();
        } else if (src == exitItem) {
            channel.close();
            System.exit(0);
        }
    }

    /**
     * Close the channel properly before closing the window
     */
    public void windowClosing(WindowEvent e) {
        log.info("WindowListener method called: windowClosing.");
        channel.close();
        log.info("Channel closed");
    }
}
