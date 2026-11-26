package ASCEND;

import java.io.File;
import java.util.List;

public class Menu {
    public static void show(String[] args) {
        String[] gameArgs = args.clone();
        UI.launch(() -> UI.showMenu(
                () -> {
                    List<File> levels = Gameloop.getLevelImages(gameArgs);
                    showLevelPicker(gameArgs, levels);
                },
                () -> Gameloop.resetProgress(Gameloop.getLevelDirectory(gameArgs)),
                () -> System.exit(0)));
    }

    private static void showLevelPicker(String[] args, List<File> levels) {
        if (levels.isEmpty()) {
            UI.showLevelPicker(
                    levels,
                    false,
                    () -> { },
                    new boolean[3],
                    (finaleIndex, finaleName) -> { },
                    (level, levelIndex) -> Gameloop.game(level, levelIndex, () -> { }),
                    () -> show(args));
            return;
        }

        File directory = levels.get(0).getParentFile();
        boolean tutorialCompleted = Gameloop.isTutorialCompleted(directory);
        UI.showLevelPicker(
                levels,
                tutorialCompleted,
                () -> UI.showTutorial(() -> {
                    Gameloop.completeTutorial(directory);
                    showLevelPicker(args, levels);
                }),
                Gameloop.getUnlockedFinales(levels),
                (finaleIndex, finaleName) -> UI.showFinalLevel(
                        finaleName,
                        () -> showLevelPicker(args, levels)),
                (level, levelIndex) -> Gameloop.game(
                        level,
                        levelIndex,
                        () -> showLevelPicker(args, levels)),
                () -> show(args));
    }

}
