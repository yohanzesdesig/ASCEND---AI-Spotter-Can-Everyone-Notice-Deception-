package ASCEND;

import java.io.File;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Gameloop {
    private static final Map<String, Boolean> CORRECT_ANSWERS = createAnswerKey();
    private static final Preferences PROGRESS = Preferences.userNodeForPackage(Gameloop.class);

    private final List<File> images;
    private final int levelIndex;
    private final Runnable returnToLevels;
    private final boolean[] decisions;
    private final boolean[] reviewed;
    private final boolean[] correctAnswers;
    private final boolean[] answerKnown;
    private final UI ui;
    private int currentImage;
    private int correctCount;

    private Gameloop(List<File> images, int levelIndex, Runnable returnToLevels) {
        this.images = images;
        this.levelIndex = levelIndex;
        this.returnToLevels = returnToLevels;
        this.decisions = new boolean[images.size()];
        this.reviewed = new boolean[images.size()];
        this.correctAnswers = new boolean[images.size()];
        this.answerKnown = new boolean[images.size()];
        for (int i = 0; i < images.size(); i++) {
            Boolean answer = CORRECT_ANSWERS.get(images.get(i).getName().toLowerCase(Locale.ROOT));
            if (answer != null) {
                answerKnown[i] = true;
                correctAnswers[i] = answer;
            }
        }
        this.ui = new UI(() -> recordDecision(true), () -> recordDecision(false));
        showCurrentImage();
    }

    private static Map<String, Boolean> createAnswerKey() {
        Map<String, Boolean> answers = new HashMap<>();
        answers.put("bim3yxjv.jpg", true);
        answers.put("fzokte95.jpg", false);
        answers.put("img_0007.jpg", true);
        return Collections.unmodifiableMap(answers);
    }

    public static void game(String[] args) {
        UI.launch(() -> {
            List<File> images = getLevelImages(args);
            if (images.isEmpty()) {
                return;
            }

            new Gameloop(images, -1, () -> { });
        });
    }

    public static List<File> getLevelImages(String[] args) {
        return findImages(getLevelDirectory(args));
    }

    public static File getLevelDirectory(String[] args) {
        return args.length > 0
                ? new File(args[0])
                : defaultImageDirectory();
    }

    public static boolean resetProgress(File directory) {
        String pathHash = pathHash(directory);
        PROGRESS.remove("unlocked_" + pathHash);
        PROGRESS.remove("tutorial_completed_" + pathHash);
        String answerPrefix = "answer_" + pathHash + "_";
        try {
            for (String key : PROGRESS.keys()) {
                if (key.startsWith(answerPrefix)) {
                    PROGRESS.remove(key);
                }
            }
        } catch (BackingStoreException exception) {
            UI.showError(
                    "Could not read saved progress for reset: " + exception.getMessage(),
                    "Progress reset error");
            return false;
        }

        return flushProgress();
    }

    public static int getUnlockedLevelCount(List<File> levels) {
        if (levels.isEmpty()) {
            return 0;
        }

        File directory = levels.get(0).getParentFile();
        if (!isTutorialCompleted(directory)) {
            return 0;
        }

        String key = progressKey(directory);
        int unlockedCount = PROGRESS.getInt(key, 1);
        return Math.max(1, Math.min(unlockedCount, levels.size()));
    }

    public static boolean isFinalLevelUnlocked(List<File> levels) {
        return !levels.isEmpty() && getUnlockedLevelCount(levels) == levels.size();
    }

    public static boolean[] getUnlockedFinales(List<File> levels) {
        boolean[] unlocked = new boolean[3];
        if (!isFinalLevelUnlocked(levels)) {
            return unlocked;
        }

        File directory = levels.get(0).getParentFile();
        int correctCount = 0;
        int incorrectCount = 0;
        for (int i = 0; i < levels.size(); i++) {
            int answerResult = PROGRESS.getInt(finaleAnswerKey(directory, i), -2);
            if (answerResult == -2) {
                return unlocked;
            }
            if (answerResult == 1) {
                correctCount++;
            } else if (answerResult == 0) {
                incorrectCount++;
            }
        }

        int scoredCount = correctCount + incorrectCount;
        if (scoredCount == 0) {
            return unlocked;
        }

        unlocked[0] = correctCount == scoredCount;
        unlocked[1] = incorrectCount == scoredCount;
        unlocked[2] = correctCount > 0 && incorrectCount > 0;
        return unlocked;
    }

    public static boolean isTutorialCompleted(File directory) {
        return PROGRESS.getBoolean(tutorialProgressKey(directory), false);
    }

    public static void completeTutorial(File directory) {
        PROGRESS.putBoolean(tutorialProgressKey(directory), true);
        flushProgress();
    }

    public static void game(File levelImage, int levelIndex, Runnable returnToLevels) {
        UI.launch(() -> {
            if (!levelImage.isFile()) {
                UI.showError("The selected level image could not be found.", "Level error");
                return;
            }

            new Gameloop(Collections.singletonList(levelImage), levelIndex, returnToLevels);
        });
    }

    private static String progressKey(File directory) {
        return "unlocked_" + pathHash(directory);
    }

    private static String tutorialProgressKey(File directory) {
        return "tutorial_completed_" + pathHash(directory);
    }

    private static String finaleAnswerKey(File directory, int levelIndex) {
        return "answer_" + pathHash(directory) + "_" + levelIndex;
    }

    private static String pathHash(File directory) {
        String normalizedPath = directory.getAbsolutePath().toLowerCase(Locale.ROOT);
        return Integer.toHexString(normalizedPath.hashCode());
    }

    private void saveLevelResult(boolean answerKnown, boolean answeredCorrectly) {
        if (levelIndex < 0) {
            return;
        }

        File directory = images.get(0).getParentFile();
        String key = progressKey(directory);
        int unlockedCount = PROGRESS.getInt(key, 1);
        PROGRESS.putInt(key, Math.max(unlockedCount, levelIndex + 2));
        PROGRESS.putInt(
                finaleAnswerKey(directory, levelIndex),
                answerKnown ? (answeredCorrectly ? 1 : 0) : -1);
        flushProgress();
    }

    private static boolean flushProgress() {
        try {
            PROGRESS.flush();
            return true;
        } catch (BackingStoreException exception) {
            UI.showError(
                    "Your progress could not be saved: " + exception.getMessage(),
                    "Progress save error");
            return false;
        }
    }
    
    private static File defaultImageDirectory() {
        File assetsPicsFolder = new File("Assets", "Pics");
        return assetsPicsFolder.isDirectory()
                ? assetsPicsFolder
                : new File("ASCEND", "Assets" + File.separator + "Pics");
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

        if (!ui.showImage(
                images.get(currentImage),
                currentImage + 1,
                images.size(),
                getHintForCurrentImage())) {
            currentImage++;
            showCurrentImage();
        }
    }

    private String getHintForCurrentImage() {
        if (!answerKnown[currentImage]) {
            return "No hint is available for this image yet.";
        }
        return "The intended choice for this image is "
                + answerText(correctAnswers[currentImage]) + ".";
    }

    private void recordDecision(boolean approved) {
        decisions[currentImage] = approved;
        reviewed[currentImage] = true;
        saveLevelResult(
                answerKnown[currentImage],
                answerKnown[currentImage] && decisions[currentImage] == correctAnswers[currentImage]);
        if (answerKnown[currentImage] && decisions[currentImage] == correctAnswers[currentImage]) {
            correctCount++;
        }
        currentImage++;
        showCurrentImage();
    }

    private void showResults() {
        int scoredCount = 0;
        int skippedCount = 0;
        int unscoredCount = 0;
        List<String> incorrectAnswers = new ArrayList<>();
        for (int i = 0; i < decisions.length; i++) {
            if (!reviewed[i]) {
                skippedCount++;
            } else if (answerKnown[i]) {
                scoredCount++;
                if (decisions[i] != correctAnswers[i]) {
                    incorrectAnswers.add(images.get(i).getName()
                            + "\nYour answer: " + answerText(decisions[i])
                            + "\nCorrect answer: " + answerText(correctAnswers[i]));
                }
            } else {
                unscoredCount++;
            }
        }

        ui.showResults(
                correctCount,
                scoredCount,
                skippedCount,
                unscoredCount,
                incorrectAnswers,
                returnToLevels);
    }

    private static String answerText(boolean approved) {
        return approved ? "Approve" : "Disapprove";
    }
}
