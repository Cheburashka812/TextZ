import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Scanner;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;

public class textred extends JFrame implements ActionListener {

    JTextPane textPane;
    JScrollPane scrollPane;
    JLabel fontLabel;
    JSpinner fontSizeSpinner;
    JButton fontColorButton;
    JComboBox<String> fontBox;

    JMenuBar menuBar;
    JMenu fileMenu;
    JMenuItem openItem;
    JMenuItem saveItem;
    JMenuItem exitItem;

    textred() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Bro text Editor");
        this.setSize(500, 500);
        this.setLayout(new FlowLayout());
        this.setLocationRelativeTo(null);

        textPane = new JTextPane();
        textPane.setFont(new Font("Arial", Font.PLAIN, 20));
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                highlightURLsAsync();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                highlightURLsAsync();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                highlightURLsAsync();
            }
        });

        scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(450, 450));
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        fontLabel = new JLabel("Font: ");

        fontSizeSpinner = new JSpinner();
        fontSizeSpinner.setPreferredSize(new Dimension(50, 25));
        fontSizeSpinner.setValue(20);
        fontSizeSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                textPane.setFont(new Font(textPane.getFont().getFamily(), Font.PLAIN, (int) fontSizeSpinner.getValue()));
            }
        });

        fontColorButton = new JButton("Color");
        fontColorButton.addActionListener(this);

        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        fontBox = new JComboBox<>(fonts);
        fontBox.addActionListener(this);
        fontBox.setSelectedItem("Arial");

        // ----- menubar -----

        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        exitItem = new JMenuItem("Exit");

        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        exitItem.addActionListener(this);

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // ----- /menubar -----

        this.setJMenuBar(menuBar);
        this.add(fontLabel);
        this.add(fontSizeSpinner);
        this.add(fontColorButton);
        this.add(fontBox);
        this.add(scrollPane);
        this.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == fontColorButton) {
            JColorChooser colorChooser = new JColorChooser();
            Color color = colorChooser.showDialog(null, "Choose a color", Color.black);
            textPane.setForeground(color);
        }

        if (e.getSource() == fontBox) {
            textPane.setFont(new Font((String) fontBox.getSelectedItem(), Font.PLAIN, textPane.getFont().getSize()));
        }

        if (e.getSource() == openItem) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
            fileChooser.setFileFilter(filter);

            int response = fileChooser.showOpenDialog(null);

            if (response == JFileChooser.APPROVE_OPTION) {
                File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                Scanner fileIn = null;

                try {
                    fileIn = new Scanner(file);
                    if (file.isFile()) {
                        textPane.setText("");  // Очистите текстовое поле перед загрузкой нового файла
                        while (fileIn.hasNextLine()) {
                            String line = fileIn.nextLine() + "\n";
                            appendText(line);
                        }
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } finally {
                    if (fileIn != null) {
                        fileIn.close();
                    }
                }
            }
        }
        if (e.getSource() == saveItem) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));

            int response = fileChooser.showSaveDialog(null);

            if (response == JFileChooser.APPROVE_OPTION) {
                File file;
                PrintWriter fileOut = null;

                file = new File(fileChooser.getSelectedFile().getAbsolutePath());
                try {
                    fileOut = new PrintWriter(file);
                    fileOut.println(textPane.getText());
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } finally {
                    if (fileOut != null) {
                        fileOut.close();
                    }
                }
            }
        }
        if (e.getSource() == exitItem) {
            System.exit(0);
        }
    }

    private void appendText(String text) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            Style style = textPane.addStyle("URL_Style", null);
            StyleConstants.setForeground(style, Color.BLUE);
            StyleConstants.setUnderline(style, true);

            Matcher matcher = Pattern.compile("(https?://\\S+)").matcher(text);
            int lastEnd = 0;
            while (matcher.find()) {
                doc.insertString(doc.getLength(), text.substring(lastEnd, matcher.start()), null);
                doc.insertString(doc.getLength(), matcher.group(), style);
                lastEnd = matcher.end();
            }
            doc.insertString(doc.getLength(), text.substring(lastEnd), null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void highlightURLsAsync() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                StyledDocument doc = textPane.getStyledDocument();
                String text = doc.getText(0, doc.getLength());

                doc.setCharacterAttributes(0, text.length(), textPane.getStyle(StyleContext.DEFAULT_STYLE), true);

                Style style = textPane.addStyle("URL_Style", null);
                StyleConstants.setForeground(style, Color.BLUE);
                StyleConstants.setUnderline(style, true);

                Matcher matcher = Pattern.compile("(https?://\\S+)").matcher(text);
                while (matcher.find()) {
                    doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), style, false);
                }
                return null;
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        new textred();
    }
}
