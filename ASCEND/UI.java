package ASCEND;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import javax.imageio.ImageIO;
import javax.swing.*;

public class UI {
    private static final String MENU_MUSIC_PLACEHOLDER = "Assets/Audio/main-menu.mp3";
    private static final String LEVEL_MUSIC_PLACEHOLDER = "Assets/Audio/levels.mp3";
    private static final String TUTORIAL_TEXT =
            "Welcome to ASCEND!\n\n"
            + "You will review the images in the folder and decide whether to approve or disapprove each one.\n\n"
            + "Choose Approve or Disapprove beneath each image to move on. Images are shown one at a time, "
            + "and the counter at the top tells you how far you are. The Hint panel starts hidden; click Show Hint "
            + "if you need help. You can hide or show it whenever you like.\n\n"
            + "When you finish, ASCEND will show your score and reveal any answers you missed. Complete every "
            + "image level to unlock a finale based on your answers. Press Enter to begin!";

    private final JFrame frame = new JFrame("ASCEND Image Review");
    private final JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel progressLabel = new JLabel("", SwingConstants.CENTER);
    private final JTextArea hintText = new JTextArea("Hints for the current image will appear here.");
    private final JPanel buttonsPanel = new JPanel(new FlowLayout());

    public static void showTutorial(Runnable startAction) {
        String[] pages = TUTORIAL_TEXT.split("\\n\\n");
        JFrame tutorialFrame = new JFrame("ASCEND Tutorial");
        tutorialFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Color background = new Color(18, 24, 32);
        Color accent = new Color(33, 190, 170);
        JTextArea dialogue = new JTextArea();
        dialogue.setFont(dialogue.getFont().deriveFont(22f));
        dialogue.setLineWrap(true);
        dialogue.setWrapStyleWord(true);
        dialogue.setEditable(false);
        dialogue.setFocusable(false);
        dialogue.setOpaque(false);
        dialogue.setForeground(Color.WHITE);
        dialogue.setBorder(BorderFactory.createEmptyBorder(32, 36, 24, 36));

        JLabel progressLabel = new JLabel("", SwingConstants.CENTER);
        progressLabel.setForeground(Color.LIGHT_GRAY);
        JButton continueButton = new JButton();
        continueButton.setFont(continueButton.getFont().deriveFont(Font.BOLD, 16f));
        continueButton.setBackground(accent);
        continueButton.setForeground(Color.BLACK);
        continueButton.setFocusPainted(false);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(background);
        content.add(progressLabel, BorderLayout.NORTH);
        content.add(dialogue, BorderLayout.CENTER);
        content.add(continueButton, BorderLayout.SOUTH);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        int[] currentPage = {0};
        Runnable advance = () -> {
            currentPage[0]++;
            if (currentPage[0] >= pages.length) {
                tutorialFrame.dispose();
                startAction.run();
                return;
            }

            dialogue.setText(pages[currentPage[0]]);
            progressLabel.setText(
                    "Tutorial " + (currentPage[0] + 1) + " of " + pages.length
                    + " | Music placeholder: " + LEVEL_MUSIC_PLACEHOLDER);
            continueButton.setText(
                    currentPage[0] == pages.length - 1 ? "Start game (Enter)" : "Continue (Enter)");
        };
        continueButton.addActionListener(event -> advance.run());
        tutorialFrame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ENTER"), "advanceTutorial");
        tutorialFrame.getRootPane().getActionMap().put("advanceTutorial",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent event) {
                        advance.run();
                    }
                });

        dialogue.setText(pages[0]);
        progressLabel.setText(
                "Tutorial 1 of " + pages.length + " | Music placeholder: "
                + LEVEL_MUSIC_PLACEHOLDER);
        continueButton.setText(pages.length == 1 ? "Start game (Enter)" : "Continue (Enter)");
        tutorialFrame.setContentPane(content);
        tutorialFrame.setSize(720, 360);
        tutorialFrame.setMinimumSize(new Dimension(560, 300));
        tutorialFrame.setLocationRelativeTo(null);
        tutorialFrame.setVisible(true);
    }

    public static void showMenu(
            Runnable startAction,
            BooleanSupplier resetAction,
            Runnable exitAction) {
        JFrame menuFrame = new JFrame("ASCEND");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Color background = new Color(18, 24, 32);
        Color accent = new Color(33, 190, 170);

        JLabel title = new JLabel("ASCEND", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 56f));
        JLabel musicPlaceholder = new JLabel(
                "Music placeholder: " + MENU_MUSIC_PLACEHOLDER,
                SwingConstants.CENTER);
        musicPlaceholder.setForeground(Color.LIGHT_GRAY);

        JButton startButton = new JButton("Start");
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD, 18f));
        startButton.setBackground(accent);
        startButton.setForeground(Color.BLACK);
        startButton.setFocusPainted(false);
        startButton.addActionListener(event -> {
            menuFrame.dispose();
            startAction.run();
        });

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(exitButton.getFont().deriveFont(Font.BOLD, 18f));
        exitButton.addActionListener(event -> {
            menuFrame.dispose();
            exitAction.run();
        });

        JButton resetButton = new JButton("Reset Progress");
        resetButton.setFont(resetButton.getFont().deriveFont(Font.BOLD, 18f));
        resetButton.addActionListener(event -> {
            int choice = JOptionPane.showConfirmDialog(
                    menuFrame,
                    "Reset progress for this image folder? This relocks all levels and clears finale results.",
                    "Reset Progress",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION && resetAction.getAsBoolean()) {
                showMessage("Progress reset. The tutorial and levels are locked again.", "Progress Reset");
            }
        });

        Dimension buttonSize = new Dimension(180, 48);
        startButton.setPreferredSize(buttonSize);
        exitButton.setPreferredSize(buttonSize);
        resetButton.setPreferredSize(buttonSize);

        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 14));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(startButton);
        buttonsPanel.add(resetButton);
        buttonsPanel.add(exitButton);

        JPanel menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(background);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(36, 48, 36, 48));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.insets = new Insets(0, 0, 30, 0);
        menuPanel.add(title, constraints);
        constraints.gridy = 1;
        constraints.insets = new Insets(0, 0, 12, 0);
        menuPanel.add(musicPlaceholder, constraints);
        constraints.gridy = 2;
        constraints.insets = new Insets(0, 0, 0, 0);
        menuPanel.add(buttonsPanel, constraints);

        menuFrame.setContentPane(menuPanel);
        menuFrame.setSize(520, 400);
        menuFrame.setMinimumSize(new Dimension(420, 360));
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setVisible(true);
    }

    public static void showLevelPicker(
            List<File> levels,
            boolean tutorialCompleted,
            Runnable tutorialAction,
            boolean[] unlockedFinales,
            BiConsumer<Integer, String> finaleAction,
            BiConsumer<File, Integer> selectAction,
            Runnable backAction) {
        JFrame levelFrame = new JFrame("ASCEND - Choose a Level");
        levelFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Color background = new Color(18, 24, 32);
        Color accent = new Color(33, 190, 170);
        JLabel title = new JLabel("CHOOSE A LEVEL", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 32f));
        JLabel instruction = new JLabel(
                "Complete a level to unlock the next one.", SwingConstants.CENTER);
        instruction.setForeground(Color.LIGHT_GRAY);

        JPanel levelList = new JPanel(new GridLayout(0, 1, 0, 10));
        levelList.setBackground(background);
        if (levels.isEmpty()) {
            JLabel noLevels = new JLabel("No levels are available.", SwingConstants.CENTER);
            noLevels.setForeground(Color.LIGHT_GRAY);
            levelList.add(noLevels);
        } else {
            JButton tutorialButton = new JButton(tutorialCompleted
                    ? "Tutorial  -  Completed (click to replay)"
                    : "Tutorial  -  Start here");
            tutorialButton.setFont(tutorialButton.getFont().deriveFont(Font.BOLD, 16f));
            tutorialButton.setBackground(accent);
            tutorialButton.setForeground(Color.BLACK);
            tutorialButton.setFocusPainted(false);
            tutorialButton.addActionListener(event -> {
                levelFrame.dispose();
                tutorialAction.run();
            });
            levelList.add(tutorialButton);

            int unlockedCount = Gameloop.getUnlockedLevelCount(levels);
            for (int i = 0; i < levels.size(); i++) {
                File image = levels.get(i);
                boolean unlocked = i < unlockedCount;
                String levelName = "Level " + (i + 1) + "  -  " + image.getName();
                JButton levelButton = new JButton(
                        unlocked ? levelName : levelName + "  [LOCKED]");
                levelButton.setFont(levelButton.getFont().deriveFont(Font.BOLD, 16f));
                levelButton.setBackground(accent);
                levelButton.setForeground(Color.BLACK);
                levelButton.setFocusPainted(false);
                levelButton.setEnabled(unlocked);
                levelButton.addActionListener(event -> {
                    levelFrame.dispose();
                    selectAction.accept(image, levels.indexOf(image));
                });
                levelList.add(levelButton);
            }

            String[] finaleNames = {
                "Perfect Finale  -  All answers correct",
                "All Incorrect Finale  -  No answers correct",
                "Imperfect Quiz Finale  -  Some answers correct"
            };
            for (int i = 0; i < finaleNames.length; i++) {
                final int finaleIndex = i;
                boolean unlocked = unlockedFinales[i];
                JButton finaleButton = new JButton(
                        unlocked ? finaleNames[i] : finaleNames[i] + "  [LOCKED]");
                finaleButton.setFont(finaleButton.getFont().deriveFont(Font.BOLD, 16f));
                finaleButton.setBackground(accent);
                finaleButton.setForeground(Color.BLACK);
                finaleButton.setFocusPainted(false);
                finaleButton.setEnabled(unlocked);
                finaleButton.addActionListener(event -> {
                    levelFrame.dispose();
                    finaleAction.accept(finaleIndex, finaleNames[finaleIndex]);
                });
                levelList.add(finaleButton);
            }
        }

        JScrollPane levelScroll = new JScrollPane(levelList);
        levelScroll.setBorder(BorderFactory.createEmptyBorder());
        levelScroll.getViewport().setBackground(background);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> {
            levelFrame.dispose();
            backAction.run();
        });

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setBackground(background);
        header.add(title, BorderLayout.NORTH);
        instruction.setText(levels.isEmpty()
                ? "Add images to your image folder to create levels."
                : "Complete the tutorial and image levels to unlock a finale that matches your answers. "
                + "Music placeholder: " + LEVEL_MUSIC_PLACEHOLDER);
        header.add(instruction, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(background);
        content.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        content.add(header, BorderLayout.NORTH);
        content.add(levelScroll, BorderLayout.CENTER);
        content.add(backButton, BorderLayout.SOUTH);

        levelFrame.setContentPane(content);
        levelFrame.setSize(560, 480);
        levelFrame.setMinimumSize(new Dimension(420, 320));
        levelFrame.setLocationRelativeTo(null);
        levelFrame.setVisible(true);
    }

    public UI(Runnable approveAction, Runnable disapproveAction) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(12, 12));
        JButton toggleHintButton = new JButton("Hide Hint");
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel musicPlaceholder = new JLabel(
                "Music placeholder: " + LEVEL_MUSIC_PLACEHOLDER);
        headerPanel.add(musicPlaceholder, BorderLayout.WEST);
        headerPanel.add(progressLabel, BorderLayout.CENTER);
        headerPanel.add(toggleHintButton, BorderLayout.EAST);
        frame.add(headerPanel, BorderLayout.NORTH);
        imageLabel.setPreferredSize(new Dimension(760, 560));
        imageLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        frame.add(imageLabel, BorderLayout.CENTER);

        hintText.setLineWrap(true);
        hintText.setWrapStyleWord(true);
        hintText.setEditable(false);
        hintText.setFocusable(false);
        hintText.setOpaque(false);
        hintText.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel hintPanel = new JPanel(new BorderLayout());
        hintPanel.setBorder(BorderFactory.createTitledBorder("Hint"));
        hintPanel.setPreferredSize(new Dimension(240, 560));
        hintPanel.add(hintText, BorderLayout.CENTER);
        hintPanel.setVisible(false);
        toggleHintButton.setText("Show Hint");
        frame.add(hintPanel, BorderLayout.EAST);
        toggleHintButton.addActionListener(event -> {
            boolean showHint = !hintPanel.isVisible();
            hintPanel.setVisible(showHint);
            toggleHintButton.setText(showHint ? "Hide Hint" : "Show Hint");
            frame.revalidate();
            frame.repaint();
        });

        JButton disapproveButton = new JButton("Disapprove");
        disapproveButton.addActionListener(event -> disapproveAction.run());
        JButton approveButton = new JButton("Approve");
        approveButton.addActionListener(event -> approveAction.run());
        buttonsPanel.add(disapproveButton);
        buttonsPanel.add(approveButton);
        frame.add(buttonsPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setMinimumSize(new Dimension(900, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void launch(Runnable action) {
        SwingUtilities.invokeLater(action);
    }

    public static void showError(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void showMessage(String message, String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showFinalLevel(String finaleName, Runnable backAction) {
        JFrame finalLevelFrame = new JFrame("ASCEND - " + finaleName);
        finalLevelFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(new Color(18, 24, 32));
        content.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel title = new JLabel(finaleName.toUpperCase(), SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 30f));

        JPanel videoPlaceholder = new JPanel(new GridBagLayout());
        videoPlaceholder.setBackground(Color.BLACK);
        JLabel placeholderLabel = new JLabel(
                "<html><div style='text-align:center'>MP4 VIDEO PLACEHOLDER<br>"
                + "Add the " + finaleName + " video here</div></html>",
                SwingConstants.CENTER);
        placeholderLabel.setForeground(Color.WHITE);
        placeholderLabel.setFont(placeholderLabel.getFont().deriveFont(Font.BOLD, 24f));
        videoPlaceholder.add(placeholderLabel);

        JButton backButton = new JButton("Back to Levels");
        backButton.addActionListener(event -> {
            finalLevelFrame.dispose();
            backAction.run();
        });

        JLabel musicPlaceholder = new JLabel(
                "Music placeholder: " + LEVEL_MUSIC_PLACEHOLDER,
                SwingConstants.CENTER);
        musicPlaceholder.setForeground(Color.LIGHT_GRAY);
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setBackground(new Color(18, 24, 32));
        footer.add(musicPlaceholder, BorderLayout.NORTH);
        footer.add(backButton, BorderLayout.SOUTH);

        content.add(title, BorderLayout.NORTH);
        content.add(videoPlaceholder, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);
        finalLevelFrame.setContentPane(content);
        finalLevelFrame.setSize(800, 520);
        finalLevelFrame.setMinimumSize(new Dimension(560, 360));
        finalLevelFrame.setLocationRelativeTo(null);
        finalLevelFrame.setVisible(true);
    }

    public boolean showImage(File file, int imageNumber, int totalImages, String hint) {
        try {
            Image image = ImageIO.read(file);
            if (image == null) {
                showError("Could not display " + file.getName() + ". It will be skipped.", "Image error");
                return false;
            }

            int width = image.getWidth(null);
            int height = image.getHeight(null);
            double scale = Math.min(740.0 / width, 540.0 / height);
            int displayWidth = Math.max(1, (int) (width * scale));
            int displayHeight = Math.max(1, (int) (height * scale));
            imageLabel.setIcon(new ImageIcon(
                    image.getScaledInstance(displayWidth, displayHeight, Image.SCALE_SMOOTH)));
            imageLabel.setText("");
            progressLabel.setText(imageNumber + " of " + totalImages + " — " + file.getName());
            hintText.setText(hint);
            return true;
        } catch (IOException exception) {
            showError(
                    "Could not read " + file.getName() + ": " + exception.getMessage(),
                    "Image error");
            return false;
        }
    }

    public void showResults(
            int correctCount,
            int scoredCount,
            int skippedCount,
            int unscoredCount,
            java.util.List<String> incorrectAnswers,
            Runnable returnToLevels) {
        java.util.List<String> resultPages = new java.util.ArrayList<>();
        resultPages.add("Review complete!\n\n"
                + "Correct answers: " + correctCount + " / " + scoredCount + "\n"
                + "Skipped images: " + skippedCount + "\n"
                + "Images without an answer key: " + unscoredCount);
        resultPages.addAll(incorrectAnswers);

        JLabel sequenceLabel = new JLabel("", SwingConstants.CENTER);
        sequenceLabel.setForeground(Color.LIGHT_GRAY);
        JTextArea resultText = new JTextArea();
        resultText.setFont(resultText.getFont().deriveFont(22f));
        resultText.setLineWrap(true);
        resultText.setWrapStyleWord(true);
        resultText.setEditable(false);
        resultText.setFocusable(false);
        resultText.setOpaque(false);
        resultText.setForeground(Color.WHITE);
        resultText.setBorder(BorderFactory.createEmptyBorder(32, 36, 24, 36));

        JPanel pagePanel = new JPanel(new BorderLayout());
        pagePanel.setBackground(new Color(18, 24, 32));
        JButton continueButton = new JButton();
        continueButton.setFont(continueButton.getFont().deriveFont(Font.BOLD, 16f));
        continueButton.setBackground(new Color(33, 190, 170));
        continueButton.setForeground(Color.BLACK);
        continueButton.setFocusPainted(false);

        JPanel sequencePanel = new JPanel(new BorderLayout(0, 12));
        sequencePanel.setBackground(new Color(18, 24, 32));
        sequencePanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        sequencePanel.add(sequenceLabel, BorderLayout.NORTH);
        sequencePanel.add(pagePanel, BorderLayout.CENTER);
        sequencePanel.add(continueButton, BorderLayout.SOUTH);

        int totalSteps = resultPages.size();
        int[] currentStep = {0};
        Runnable showStep = () -> {
            pagePanel.removeAll();
            resultText.setText(resultPages.get(currentStep[0]));
            pagePanel.add(resultText, BorderLayout.CENTER);
            continueButton.setText(
                    currentStep[0] == totalSteps - 1 ? "Back to Levels" : "Continue (Enter)");

            sequenceLabel.setText("End sequence " + (currentStep[0] + 1) + " of " + totalSteps);
            pagePanel.revalidate();
            pagePanel.repaint();
        };
        continueButton.addActionListener(event -> {
            if (currentStep[0] == totalSteps - 1) {
                frame.dispose();
                returnToLevels.run();
            } else {
                currentStep[0]++;
                showStep.run();
            }
        });
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("ENTER"), "advanceEndSequence");
        frame.getRootPane().getActionMap().put("advanceEndSequence",
                new AbstractAction() {
                    @Override
                    public void actionPerformed(java.awt.event.ActionEvent event) {
                        continueButton.doClick();
                    }
                });

        showStep.run();
        frame.setTitle("ASCEND - Results");
        frame.setContentPane(sequencePanel);
        frame.setSize(720, 420);
        frame.setMinimumSize(new Dimension(560, 340));
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }
}
