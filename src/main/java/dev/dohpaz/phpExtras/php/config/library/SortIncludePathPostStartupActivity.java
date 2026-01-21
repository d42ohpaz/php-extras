package dev.dohpaz.phpExtras.php.config.library;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupActivity;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import org.jetbrains.annotations.NotNull;

public class SortIncludePathPostStartupActivity implements StartupActivity, DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        final PhpIncludePathManager includePathManager = PhpIncludePathManager.getInstance(project);

        project
            .getMessageBus()
            .connect()
            .subscribe(ModuleRootListener.TOPIC, new SortIncludePathListener(includePathManager));
    }
}
