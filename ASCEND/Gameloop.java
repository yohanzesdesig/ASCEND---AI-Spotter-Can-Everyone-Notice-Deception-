package ASCEND;

import java.io.File;
import java.util.*;

public class Gameloop {
    private final List<File> images;
    private final boolean[] decisions;
    private final boolean[] reviewed;
    private final UI ui;
    private int currentImage;

    private Gameloop(List<File> images) {
        this.images = images;
        this.decisions = new boolean[images.size()];
        this.reviewed = new boolean[images.size()];
        this.ui = new UI(() -> recordDecision(true), () -> recordDecision(false));
        showCurrentImage();
    }

    public static void game(String[] args) {
        UI.launch(() -> {
            File directory = args.length > 0
                    ? new File(args[0])
                    : defaultImageDirectory();

            List<File> images = findImages(directory);
            if (images.isEmpty()) {
                return;
            }

            new Gameloop(images);
        });
    }

    private static File defaultImageDirectory() {
        File assetsPicsFolder = new File("Assets", "Pics");
        return assetsPicsFolder.isDirectory()
                ? assetsPicsFolder
                : new File("ASCEND", "Assets" + File.separator + "Pics");
    }

    public static void main(String[] args) {
        game(args);
    }

    private static List<File> findImages(File directory) {
        if (!directory.isDirectory()) {
            UI.showError("The provided path is not a folder.", "Invalid folder");
            return new ArrayList<>();
        }

        File[] files = directory.listFiles(file -> {
            String name = file.getName().toLowerCase();
            return file.isFile()
                    && (name.endsWith(".png")
                            || name.endsWith(".jpg")
                            || name.endsWith(".jpeg")
                            || name.endsWith(".gif")
                            || name.endsWith(".bmp"));
        });
        if (files == null) {
            UI.showError("Could not read the selected folder.", "Folder error");
            return new ArrayList<>();
        }
        if (files.length == 0) {
            UI.showMessage("No supported images were found in that folder.", "No images found");
            return new ArrayList<>();
        }

        Arrays.sort(files, (first, second) -> first.getName().compareToIgnoreCase(second.getName()));
        return Arrays.asList(files);
    }

    private void showCurrentImage() {
        if (currentImage >= images.size()) {
            showResults();
            return;
        }

        if (!ui.showImage(images.get(currentImage), currentImage + 1, images.size())) {
            currentImage++;
            showCurrentImage();
        }
    }

    private void recordDecision(boolean approved) {
        decisions[currentImage] = approved;
        reviewed[currentImage] = true;
        currentImage++;
        showCurrentImage();
    }

    private void showResults() {
        int approvedCount = 0;
        int disapprovedCount = 0;
        int skippedCount = 0;
        for (int i = 0; i < decisions.length; i++) {
            if (!reviewed[i]) {
                skippedCount++;
            } else if (decisions[i]) {
                approvedCount++;
            } else {
                disapprovedCount++;
            }
        }

        ui.showResults(approvedCount, disapprovedCount, skippedCount);
    }
}
