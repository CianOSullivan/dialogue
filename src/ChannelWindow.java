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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import org.jgroups.View;
import org.jgroups.Address;

import com.formdev.flatlaf.FlatDarculaLaf;

/**
 * Generates a window to the channel and handles incoming messages
 */
public class ChannelWindow extends WindowAdapter implements ActionListener {
    private final EventLogger log;

    private JFrame frame;
    private JTextPane text_area; // The main chat text area
    private JTextField textField; // Text field to enter new messages
    private JMenuItem exitItem;
    private JMenuItem aboutItem;
    private JMenuItem saveItem;
    private JMenuItem uploadItem;
    private JMenuItem syncKeyItem;
    private JMenuItem genKeyItem;
    private JMenuItem listMembers;

    private JButton sendButton;
    private JButton clearButton;
    private Channel channel;
    private AttributeSet purpleAttributes;
    private AttributeSet whiteAttributes;
    private SecretKey aesKey; // The symmetric key for encrypting/decrypting messages

    private boolean deviceListActive = false;
    private JPanel deviceList;

    /**
     * Make a new Channel Window
     * 
     * @param c   the JGroups channel
     * @param key the symmetric key
     * @param l   the logfile logger
     */
    public ChannelWindow(Channel c, SecretKey key, EventLogger l) {
        channel = c;
        aesKey = key;
        log = l;
        FlatDarculaLaf.install();

        generateFontAttributes();
        makeWindow();
    }

    /**
     * Generate different font colour attributes
     */
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

    /**
     * Make the GUI window with all relevant elements
     */
    private void makeWindow() {
        // Creating the Frame
        frame = new JFrame("Dialogue");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);

        // Creating the MenuBar and adding components
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu toolsMenu = new JMenu("Tools");
        JMenu viewMenu = new JMenu("View");
        JMenu helpMenu = new JMenu("Help");

        uploadItem = new JMenuItem("Upload file");
        genKeyItem = new JMenuItem("Generate new key");
        syncKeyItem = new JMenuItem("Sync key to all channel members");
        listMembers = new JMenuItem("List all channel members");
        saveItem = new JMenuItem("Save as");
        aboutItem = new JMenuItem("About");
        exitItem = new JMenuItem("Exit");

        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        fileMenu.add(uploadItem);
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);
        toolsMenu.add(genKeyItem);
        toolsMenu.add(syncKeyItem);
        viewMenu.add(listMembers);
        helpMenu.add(aboutItem);

        // Making bottom bar with text field and buttons
        JPanel bottomBar = new JPanel(new BorderLayout(10, 10)); // the panel is not visible in output
        JPanel bottomBarButtons = new JPanel(new BorderLayout(10, 10)); // the panel is not visible in output
        bottomBarButtons.setBorder(new EmptyBorder(0, 10, 0, 10));

        textField = new JTextField(50);
        sendButton = new JButton("Send");
        text_area = new JTextPane();
        text_area.setEditable(false);

        JScrollPane scroll = new JScrollPane(text_area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        clearButton = new JButton("Clear");

        // Add action listeners to all buttons
        uploadItem.addActionListener(this);
        saveItem.addActionListener(this);
        genKeyItem.addActionListener(this);
        syncKeyItem.addActionListener(this);
        listMembers.addActionListener(this);
        aboutItem.addActionListener(this);
        exitItem.addActionListener(this);
        sendButton.addActionListener(this);
        clearButton.addActionListener(this);
        textField.addActionListener(this);

        JLabel enter = new JLabel("Enter Message: ");
        enter.setBorder(new EmptyBorder(0, 10, 0, 0));

        // Add elements to bottom bar
        bottomBar.add(enter, BorderLayout.WEST);
        bottomBar.add(textField, BorderLayout.CENTER);
        bottomBarButtons.add(sendButton, BorderLayout.WEST);
        bottomBarButtons.add(clearButton, BorderLayout.EAST);
        bottomBar.add(bottomBarButtons, BorderLayout.EAST);

        // Adding Components to the frame.
        frame.getContentPane().add(BorderLayout.SOUTH, bottomBar);
        frame.getContentPane().add(BorderLayout.NORTH, menuBar);
        frame.getContentPane().add(BorderLayout.CENTER, scroll);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(this);
    }

    /**
     * Accepts messages, files and symmetric keys and handles them appropriately
     * 
     * @param msg The sealed channel message
     */
    public void processMessage(Message msg) {
        try {
            Address source = msg.getSrc();
            SealedObject sealedContents = (SealedObject) msg.getObject();
            ChannelMessage contents = (ChannelMessage) sealedContents.getObject(aesKey);

            if (contents.isFile()) {
                // Save the file if this client didn't send it
                if (source != channel.getLocalAddress())
                    saveFile(contents.getFileMeta(), contents.getFile());
            } else {
                // Add the message to the text area
                printUsername(contents.getAuthor());
                printMessage(contents.getMsg());
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | ClassNotFoundException | IOException e) {
            log.error("Couldn't unseal message" + e);
        } catch (ClassCastException ce) {
            // Then it must be an unsealed message as it is a key
            ChannelMessage contents = (ChannelMessage) msg.getObject();
            if (contents.isKey()) {
                acceptKey(contents.getKey());
            }
        }
    }

    /**
     * Save a file to the downloads folder
     * 
     * @param fileMeta the file metadata
     * @param file     the byte array of the file
     */
    private void saveFile(File fileMeta, byte[] file) {
        String file_loc = System.getProperty("user.home") + "/Downloads/" + fileMeta.getName();
        log.info("Saving file" + file_loc);

        // Also check if src is this machine
        if (new File(file_loc).isFile()) {
            log.info("File already exists");
            JOptionPane.showMessageDialog(frame, "File already exists", "File already exists",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            // Ask to accept file
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

    /**
     * Print the username in purple
     * 
     * @param author the username string
     */
    private void printUsername(String author) {
        StyledDocument doc = text_area.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), "[" + author + "]" + ": ", purpleAttributes);
        } catch (BadLocationException e) {
            log.error("Couldn't insert string");
        }
    }

    /**
     * Print the message in white
     * 
     * @param msg the message string
     */
    private void printMessage(String msg) {
        StyledDocument doc = text_area.getStyledDocument();

        try {
            doc.insertString(doc.getLength(), msg + "\n", whiteAttributes);
        } catch (BadLocationException e) {
            log.error("Couldn't insert string");
        }
    }

    /**
     * Display the about dialogue
     */
    private void displayAbout() {
        String helpString = "Dialogue - Local network chat application.\n\n"
                + "Allows for easy communication between multiple computers on a local network. "
                + "Utilises JGroups for relible group messaging. "
                + "Any messages sent to one client will appear on other clients that are also online.";
        JOptionPane.showMessageDialog(frame, helpString);
    }

    /**
     * Wipe all text from the text area
     */
    private void clearTextArea() {
        StyledDocument doc = text_area.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            log.error("Couldn't insert string");
        }
    }

    private void addDeviceList(String members) {
        deviceList = new JPanel(new BorderLayout()); // the panel is not visible in output
        deviceList.add(new JTextArea(members));
        frame.getContentPane().add(BorderLayout.EAST, deviceList);
        frame.invalidate();
        frame.validate();
        frame.repaint();
        deviceListActive = true;

    }

    private void removeDeviceList() {
        frame.getContentPane().remove(deviceList);
        frame.invalidate();
        frame.validate();
        frame.repaint();
        deviceListActive = false;
    }

    /**
     * List the members currently connected to the channel
     */
    private void listMembers() {
        if (deviceListActive) {
            removeDeviceList();
        } else {
            String members = "Members connected to cluster: \n\n";
            if (!channel.isConnected()) {
                JOptionPane.showMessageDialog(frame, "The channel has not been connected yet, slow down!",
                        "Channel not connected", JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (Address a : channel.getView().getMembers()) {
                members += a.toString() + "\n";
            }

            addDeviceList(members);
        }

    }

    /**
     * Save the current text transcript to the downloads folder
     */
    private void saveTranscript() {
        try {
            StyledDocument doc = text_area.getStyledDocument();
            String text = doc.getText(0, doc.getLength());

            // Need to change file_loc for windows

            String file_loc = System.getProperty("user.home") + "/Downloads/chat_transcript_"
                    + new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss").format(new Date()) + ".txt";
            BufferedWriter out = new BufferedWriter(new FileWriter(file_loc));
            out.write(text); // Replace with the string
            out.close();

        } catch (BadLocationException | IOException e) {
            log.error("Couldn't save transcript: " + e);
        }

    }

    /**
     * Generate a new symmetric key
     */
    private void makeKey() {
        // Generate a new cipher and add key to server
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            SecretKey newKey = kgen.generateKey();

            if (new File("key.txt").isFile()) {
                log.warning("Key file already exists");

                // Ask if the key file should be overwritten
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

    /**
     * Accept a new symmetric key
     * 
     * @param key the new key
     */
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

    /**
     * Send this clients key to the channel
     */
    private void syncKey() {
        channel.send(aesKey);
    }

    /**
     * Upload files to the channel
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
                    channel.send("FILE INCOMING: " + file.getName());
                    channel.send(file, fileContent);
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
        } else if (src == sendButton || src == textField) {
            if (!textField.getText().isEmpty()) {
                channel.send(textField.getText());
                textField.setText("");
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
