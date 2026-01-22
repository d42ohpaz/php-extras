package dev.dohpaz.phpExtras.php.codeInspection;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.json.JsonUtil;
import com.intellij.json.psi.*;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.php.composer.json.PhpComposerJsonUtils;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.inspections.quickfix.PhpQuickFixBase;
import dev.dohpaz.phpExtras.NotificationUtil;
import dev.dohpaz.phpExtras.php.PhpExtrasBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PhpComposerAutoloadDevPackageToContentRootInspection extends PhpInspection {
    /**
     * Search the autoload-dev in the main project's composer.json and suggest
     * adding any packages that are not already content roots.
     *
     * <ol>
     * <li>Check if the file is a valid composer.json file</li>
     * <li>Parse the autoload-dev section for a list of local packages</li>
     * <li>Filter the list against existing project modules</li>
     * <li>Report inspection for each package not added as a module</li>
     * </ol>
     *
     * @param holder     where visitor will register problems found.
     * @param isOnTheFly true if inspection was run in non-batch mode
     * @return PsiElementVisitor
     */
    public @NotNull PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        PsiFile file = holder.getFile();

        if (!PhpComposerJsonUtils.insideComposerJson(file)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        JsonFile composerJson = ObjectUtils.tryCast(file, JsonFile.class);
        final JsonObject topLevelObject = JsonUtil.getTopLevelObject(composerJson);

        if (topLevelObject == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        Project project = holder.getProject();
        VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();

        // Each Pair<> is the name of the content root (first) and its fully-qualified filesystem path (second).
        // The main top-level project is filtered out.
        Collection<Pair<String, String>> roots = ContainerUtil.map(
                ContainerUtil.filter(
                        contentRoots,
                        (i) -> !project.getName().equals(i.getPresentableName())
                ),
                (i) -> Pair.create(i.getPresentableName(), i.getCanonicalPath())
        );

        return new JsonElementVisitor() {
            @Override
            public void visitObject(@NotNull JsonObject o) {
                if (o.equals(topLevelObject)) {
                    final JsonObject autoloadDevSection = JsonUtil.getPropertyValueOfType(o, "autoload-dev", JsonObject.class);

                    if (autoloadDevSection != null) {
                        final JsonObject psr0 = JsonUtil.getPropertyValueOfType(autoloadDevSection, "psr-0", JsonObject.class);
                        final JsonObject psr4 = JsonUtil.getPropertyValueOfType(autoloadDevSection, "psr-4", JsonObject.class);

                        final List<JsonProperty> autoloadDevProperties = autoloadDevSection.getPropertyList();

                        if (psr0 != null) {
                            ContainerUtil.addAll(autoloadDevProperties, psr0.getPropertyList());
                            autoloadDevProperties.removeIf(p -> p.getName().equals("psr-0"));
                        }

                        if (psr4 != null) {
                            ContainerUtil.addAll(autoloadDevProperties, psr4.getPropertyList());
                            autoloadDevProperties.removeIf(p -> p.getName().equals("psr-4"));
                        }

                        // The idea is that we need to take each path defined in the autoload-dev and determine (by path)
                        // which ones do not already exist as a content root. Those are the paths that we will report
                        // with an inspection.
                        Collection<JsonProperty> paths = ContainerUtil.filter(autoloadDevProperties, (property) -> !ContainerUtil.map(roots, pair -> {
                            String root = JsonPsiUtil.stripQuotes(pair.second);
                            return toAbsolutePath(project, root);
                        }).contains(toAbsolutePath(project, JsonPsiUtil.stripQuotes(Objects.requireNonNull(property.getValue()).getText()))));

                        // Filter out any paths that exist within the base path of the project
                        ContainerUtil.filter(paths, (p) -> {
                            final JsonValue value = p.getValue();

                            if (value == null) {
                                return false;
                            }

                            final Path root = toAbsolutePath(project, JsonPsiUtil.stripQuotes(value.getText()));
                            final String basePath = project.getBasePath();

                            return root != null && basePath != null && !root.startsWith(basePath);
                        }).forEach(this::makeReport);
                    }
                }
            }

            public void makeReport(@NotNull JsonProperty property) {
                Path path = toAbsolutePath(project, JsonPsiUtil.stripQuotes(Objects.requireNonNull(property.getValue()).getText()));
                if (path == null) {
                    return;
                }

                String name = path.getName(path.getNameCount() - 1).toString();
                holder.registerProblem(property.getValue(), PhpExtrasBundle.message("inspection.json.packageToContentRoot.0", name), AddPackageAsContentRootQuickFix.INSTANCE);
            }
        };
    }

    @Nullable
    public static Path toAbsolutePath(Project project, final String path) {
        if (!FileUtil.isAbsolute(path)) {
            try {
                return Path.of(Objects.requireNonNull(project.getBasePath()), path).toRealPath(LinkOption.NOFOLLOW_LINKS);
            } catch (IOException ignored) {
                return null;
            }
        }

        return Path.of(path);
    }

    private static class AddPackageAsContentRootQuickFix extends PhpQuickFixBase {
        static AddPackageAsContentRootQuickFix INSTANCE = new AddPackageAsContentRootQuickFix();

        private AddPackageAsContentRootQuickFix() {
        }

        @Override
        public @IntentionFamilyName
        @NotNull String getFamilyName() {
            return PhpExtrasBundle.message("inspection.json.packageToContentRoot.fix");
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            // Discern the package name and path on filesystem and add as a content root
            PsiElement element = descriptor.getPsiElement();
            Path path = PhpComposerAutoloadDevPackageToContentRootInspection.toAbsolutePath(project, JsonPsiUtil.stripQuotes(element.getText()));

            if (path == null) {
                NotificationUtil.warn(project, JsonPsiUtil.stripQuotes(element.getText()), "Invalid package path");
                return;
            }

            String name = path.getName(path.getNameCount() - 1).toString();

            ModuleManager manager = ModuleManager.getInstance(project);
            ModuleRootModificationUtil.addContentRoot(manager.newModule(path, name), path.toString());
        }
    }
}
