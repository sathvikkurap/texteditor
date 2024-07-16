import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.undo.UndoManager;

public class AdvancedTextEditor extends JFrame implements ActionListener {
    private JTextArea textArea;
    private JMenuItem newItem, openItem, saveItem, saveAsItem, exitItem;
    private JMenuItem cutItem, copyItem, pasteItem, findItem, replaceItem, fontItem, undoItem, redoItem;
    private JLabel statusLabel;
    private JFileChooser fileChooser;
    private File currentFile = null;
    private UndoManager undoManager;

    private static final Map<String, String> FILE_EXTENSIONS = new HashMap<>();

    static {
        FILE_EXTENSIONS.put("Text Documents (*.txt)", "txt");
        FILE_EXTENSIONS.put("Java Source Files (*.java)", "java");
        FILE_EXTENSIONS.put("HTML Files (*.html)", "html");
        FILE_EXTENSIONS.put("XML Files (*.xml)", "xml");
    }

    public AdvancedTextEditor() {
        // Set up the frame
        setTitle("Advanced Text Editor");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create a text area
        textArea = new JTextArea();
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Create a menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create menus
        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu formatMenu = new JMenu("Format");

        // Create menu items for File menu
        newItem = new JMenuItem("New");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        saveAsItem = new JMenuItem("Save As");
        exitItem = new JMenuItem("Exit");

        // Create menu items for Edit menu
        cutItem = new JMenuItem("Cut");
        copyItem = new JMenuItem("Copy");
        pasteItem = new JMenuItem("Paste");
        findItem = new JMenuItem("Find");
        replaceItem = new JMenuItem("Replace");
        undoItem = new JMenuItem("Undo");
        redoItem = new JMenuItem("Redo");

        // Create menu items for Format menu
        fontItem = new JMenuItem("Font");

        // Add action listeners
        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        saveAsItem.addActionListener(this);
        exitItem.addActionListener(this);

        cutItem.addActionListener(this);
        copyItem.addActionListener(this);
        pasteItem.addActionListener(this);
        findItem.addActionListener(this);
        replaceItem.addActionListener(this);
        undoItem.addActionListener(this);
        redoItem.addActionListener(this);
        fontItem.addActionListener(this);

        // Add menu items to File menu
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Add menu items to Edit menu
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(findItem);
        editMenu.add(replaceItem);
        editMenu.addSeparator();
        editMenu.add(undoItem);
        editMenu.add(redoItem);

        // Add menu items to Format menu
        formatMenu.add(fontItem);

        // Add menus to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);

        // Set the menu bar for the frame
        setJMenuBar(menuBar);

        // Create a status bar
        statusLabel = new JLabel(" ");
        add(statusLabel, BorderLayout.SOUTH);

        // Document listener to update status bar
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStatus();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStatus();
            }

            private void updateStatus() {
                statusLabel.setText("Length: " + textArea.getDocument().getLength());
            }
        });

        // Initialize file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Documents (*.txt)", "txt"));
        for (String description : FILE_EXTENSIONS.keySet()) {
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(description, FILE_EXTENSIONS.get(description)));
        }

        // Initialize undo manager
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newItem) {
            textArea.setText("");
            currentFile = null;
        } else if (e.getSource() == openItem) {
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                    textArea.read(reader, null);
                } catch (IOException ioException) {
                    JOptionPane.showMessageDialog(this, "Error opening file", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (e.getSource() == saveItem) {
            saveFile(currentFile);
        } else if (e.getSource() == saveAsItem) {
            saveFile(null);
        } else if (e.getSource() == exitItem) {
            System.exit(0);
        } else if (e.getSource() == cutItem) {
            textArea.cut();
        } else if (e.getSource() == copyItem) {
            textArea.copy();
        } else if (e.getSource() == pasteItem) {
            textArea.paste();
        } else if (e.getSource() == findItem) {
            findText();
        } else if (e.getSource() == replaceItem) {
            replaceText();
        } else if (e.getSource() == fontItem) {
            setFont();
        } else if (e.getSource() == undoItem) {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } else if (e.getSource() == redoItem) {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        }
    }

    private void saveFile(File file) {
        if (file == null) {
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                String extension = ((FileNameExtensionFilter) fileChooser.getFileFilter()).getExtensions()[0];
                if (!file.getName().endsWith("." + extension)) {
                    file = new File(file.getAbsolutePath() + "." + extension);
                }
            }
        }

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                textArea.write(writer);
                currentFile = file;
            } catch (IOException ioException) {
                JOptionPane.showMessageDialog(this, "Error saving file", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void findText() {
        String find = JOptionPane.showInputDialog(this, "Find:");
        if (find != null) {
            String text = textArea.getText();
            int index = text.indexOf(find);
            if (index >= 0) {
                try {
                    textArea.getHighlighter().addHighlight(index, index + find.length(), new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Text not found");
            }
        }
    }

    private void replaceText() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Find:"));
        JTextField findField = new JTextField(10);
        panel.add(findField);
        panel.add(new JLabel("Replace:"));
        JTextField replaceField = new JTextField(10);
        panel.add(replaceField);

        int option = JOptionPane.showConfirmDialog(this, panel, "Replace", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String find = findField.getText();
            String replace = replaceField.getText();
            String text = textArea.getText();
            textArea.setText(text.replace(find, replace));
        }
    }

    private void setFont() {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String font = (String) JOptionPane.showInputDialog(this, "Select Font:", "Font", JOptionPane.PLAIN_MESSAGE, null, fonts, textArea.getFont().getFamily());
        if (font != null) {
            textArea.setFont(new Font(font, Font.PLAIN, textArea.getFont().getSize()));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdvancedTextEditor editor = new AdvancedTextEditor();
            editor.setVisible(true);
        });
    }
}
