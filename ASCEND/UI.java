package ASCEND;

import java.awt.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class UI {
    private final JFrame frame = new JFrame("ASCEND Image Review");
    private final JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel progressLabel = new JLabel("", SwingConstants.CENTER);
    private final JPanel buttonsPanel = new JPanel(new FlowLayout());

    public UI(Runnable approveAction, Runnable disapproveAction) {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(12, 12));
        frame.add(progressLabel, BorderLayout.NORTH);
        imageLabel.setPreferredSize(new Dimension(760, 560));
        imageLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        frame.add(imageLabel, BorderLayout.CENTER);

        JButton disapproveButton = new JButton("Disapprove");
        disapproveButton.addActionListener(event -> disapproveAction.run());
        JButton approveButton = new JButton("Approve");
        approveButton.addActionListener(event -> approveAction.run());
        buttonsPanel.add(disapproveButton);
        buttonsPanel.add(approveButton);
        frame.add(buttonsPanel, BorderLayout.SOUTH);

        frame.pack();
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

    public boolean showImage(File file, int imageNumber, int totalImages) {
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
            return true;
        } catch (IOException exception) {
            showError(
                    "Could not read " + file.getName() + ": " + exception.getMessage(),
                    "Image error");
            return false;
        }
    }

    public void showResults(int approvedCount, int disapprovedCount, int skippedCount) {
        imageLabel.setIcon(null);
        imageLabel.setText("<html><div style='text-align:center'>Review complete!<br><br>"
                + "Approved: " + approvedCount + "<br>"
                + "Disapproved: " + disapprovedCount + "<br>"
                + "Skipped: " + skippedCount + "</div></html>");
        progressLabel.setText("All images reviewed");
        frame.remove(buttonsPanel);
        frame.revalidate();
        frame.repaint();
    }

    public static void main(String[] args) {
        Gameloop.game(args);
    }
}
