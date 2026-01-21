package dev.dohpaz.phpExtras.composer.packages;

import com.intellij.openapi.project.*;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.php.config.library.PhpIncludePathManager;
import dev.dohpaz.phpExtras.php.config.library.SortIncludePathListener;
import org.jetbrains.annotations.NotNull;

public class RootPostStartupActivity implements StartupActivity, DumbAware {
    final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

    @Override
    public void runActivity(@NotNull Project project) {
        final String basePath = project.getBasePath();
        final VirtualFile composerJson = this.localFileSystem.findFileByPath(basePath + "/composer.json");
        final PhpIncludePathManager includePathManager = PhpIncludePathManager.getInstance(project);

        final RootModuleRootListener rootModuleRootListener = new RootModuleRootListener(includePathManager, this.localFileSystem, composerJson);
        project
            .getMessageBus()
            .connect()
            .subscribe(ModuleRootListener.TOPIC, rootModuleRootListener);
    }
}
