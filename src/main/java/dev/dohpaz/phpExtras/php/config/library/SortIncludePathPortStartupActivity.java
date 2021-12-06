package dev.dohpaz.phpExtras.php.config.library;

import com.intellij.ProjectTopics;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import org.jetbrains.annotations.NotNull;

public class SortIncludePathPortStartupActivity implements StartupActivity, DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        final PhpIncludePathManager includePathManager = PhpIncludePathManager.getInstance(project);

        final SortIncludePathListener sortIncludePathListener = new SortIncludePathListener(includePathManager);
        project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, sortIncludePathListener);
    }
}
