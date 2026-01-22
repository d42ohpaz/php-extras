package dev.dohpaz.phpExtras.php.config.library;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.ProjectActivity;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SortIncludePathPostStartupActivity implements ProjectActivity, DumbAware {
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        final PhpIncludePathManager includePathManager = PhpIncludePathManager.getInstance(project);

        project
            .getMessageBus()
            .connect()
            .subscribe(ModuleRootListener.TOPIC, new SortIncludePathListener(includePathManager));

        return null;
    }
}
