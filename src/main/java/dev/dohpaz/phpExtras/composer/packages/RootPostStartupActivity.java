package dev.dohpaz.phpExtras.composer.packages;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RootPostStartupActivity implements ProjectActivity, DumbAware {
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        final String basePath = project.getBasePath();
        final VirtualFile composerJson = this.localFileSystem.findFileByPath(basePath + "/composer.json");
        final PhpIncludePathManager includePathManager = PhpIncludePathManager.getInstance(project);

        final RootModuleRootListener rootModuleRootListener = new RootModuleRootListener(includePathManager, this.localFileSystem, composerJson);
        project
            .getMessageBus()
            .connect()
            .subscribe(ModuleRootListener.TOPIC, rootModuleRootListener);

        return null;
    }
}
