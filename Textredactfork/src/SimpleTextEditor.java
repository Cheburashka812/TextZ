import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;
import java.net.URI;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

public class SimpleTextEditor extends JFrame {
    private JTextPane textPane;
    private JComboBox<String> fontSizes;
    private JComboBox<String> fontStyles;
    private JButton searchButton;
    private JButton replaceButton;
    private JTextField searchField;
    private JTextField replaceField;
    private JComboBox<String> fontTypes;


    public SimpleTextEditor() {
        super("Простой текстовый редактор");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textPane = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        searchField = new JTextField(10);
        replaceField = new JTextField(10);
        searchButton = new JButton("Найти");
        replaceButton = new JButton("Заменить");
        bottomPanel.add(new JLabel("Искать:"));
        bottomPanel.add(searchField);
        bottomPanel.add(new JLabel("Заменить на:"));
        bottomPanel.add(replaceField);
        bottomPanel.add(searchButton);
        bottomPanel.add(replaceButton);

        JPanel topPanel = new JPanel();
        fontSizes = new JComboBox<>(new String[]{"12", "14", "16", "18", "20", "22", "24", "26", "28", "30"});
        fontStyles = new JComboBox<>(new String[]{"Обычный", "Полужирный", "Курсив"});
        topPanel.add(new JLabel("Размер шрифта:"));
        topPanel.add(fontSizes);
        topPanel.add(new JLabel("Стиль шрифта:"));
        topPanel.add(fontStyles);
        JButton colorButton = new JButton("Цвет текста");
        topPanel.add(colorButton);
        String[] fontNames = {"Times New Roman", "Arial", "Courier New", "Georgia", "Verdana"};
        fontTypes = new JComboBox<>(fontNames);
        topPanel.add(new JLabel("Шрифт:"));
        topPanel.add(fontTypes);

        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

        fontSizes.addActionListener(e -> updateFont());
        fontStyles.addActionListener(e -> updateFont());
        searchButton.addActionListener(e -> search());
        replaceButton.addActionListener(e -> replace());
        colorButton.addActionListener(e -> changeTextColor());
        fontTypes.addActionListener(e -> updateFontType());

        highlightURLs();
    }

    private void updateFont() {
        int size = Integer.parseInt((String) fontSizes.getSelectedItem());
        String styleString = (String) fontStyles.getSelectedItem();
        int style;
        switch (styleString) {
            case "Полужирный":
                style = Font.BOLD;
                break;
            case "Курсив":
                style = Font.ITALIC;
                break;
            default:
                style = Font.PLAIN;
        }
        Font font = new Font("SansSerif", style, size);
        textPane.setFont(font);
    }
    private void updateFontType() {
        String selectedFontName = (String) fontTypes.getSelectedItem();
        int style = textPane.getFont().getStyle();
        int size = textPane.getFont().getSize();
        Font font = new Font(selectedFontName, style, size);
        textPane.setFont(font);
    }

    private void changeTextColor() {
        Color color = JColorChooser.showDialog(null, "Выберите цвет", Color.BLACK);
        if (color != null) {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, color);
            textPane.setCharacterAttributes(attr, false);
        }
    }

    private void search() {
        String searchText = searchField.getText();
        String textContent = textPane.getText();
        int index = textContent.indexOf(searchText);
        if (index >= 0) {
            try {
                textPane.getHighlighter().addHighlight(index, index + searchText.length(), new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void replace() {
        String searchText = searchField.getText();
        String replaceText = replaceField.getText();
        textPane.setText(textPane.getText().replaceFirst(Pattern.quote(searchText), Matcher.quoteReplacement(replaceText)));
    }

    private void highlightURLs() {
        textPane.getHighlighter().removeAllHighlights();

        String textContent = textPane.getText();
        Pattern pattern = Pattern.compile("\\b(https?://[\\w\\-\\.]+(:\\d+)?(/[\\w\\-\\./]*)?)\\b");
        Matcher matcher = pattern.matcher(textContent);

        while (matcher.find()) {
            try {
                textPane.getHighlighter().addHighlight(matcher.start(), matcher.end(),
                        new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupHyperlinkListener() {
        textPane.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                Element elem = textPane.getStyledDocument().getCharacterElement(textPane.viewToModel(e.getPoint()));
                AttributeSet as = elem.getAttributes();
                if (StyleConstants.isUnderline(as)) {
                    if (textPane.getCursor() != Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)) {
                        textPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                } else {
                    if (textPane.getCursor() != Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)) {
                        textPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        });

        textPane.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Element elem = textPane.getStyledDocument().getCharacterElement(textPane.viewToModel(e.getPoint()));
                AttributeSet as = elem.getAttributes();
                if (StyleConstants.isUnderline(as)) {
                    try {
                        Desktop.getDesktop().browse(new URI(StyleConstants.getForeground(as).toString()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateDocumentListener() {
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                highlightURLs();
            }

            public void removeUpdate(DocumentEvent e) {
                highlightURLs();
            }

            public void changedUpdate(DocumentEvent e) {
                highlightURLs();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleTextEditor().setVisible(true));
    }
}

