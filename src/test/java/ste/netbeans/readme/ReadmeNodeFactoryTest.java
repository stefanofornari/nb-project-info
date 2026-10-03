package ste.netbeans.readme;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadmeNodeFactoryTest {

    @Test
    void findsProjectInfoFilesMatchingExpectedNames() throws Exception {
        Path tempDir = Files.createTempDirectory("project-info");
        Path changelog = Files.createFile(tempDir.resolve("CHANGELOG.txt"));
        Files.createFile(tempDir.resolve("changeslog.txt"));
        Files.createFile(tempDir.resolve("changes.txt"));
        Files.createFile(tempDir.resolve("LICENSE"));
        Files.createFile(tempDir.resolve("LICENSE.txt"));
        Files.createFile(tempDir.resolve("TODO"));
        Files.createFile(tempDir.resolve("TODO.txt"));
        Files.createFile(tempDir.resolve("README.md"));
        Files.createFile(tempDir.resolve("notes.txt"));

        LocalFileSystem fileSystem = new LocalFileSystem();
        fileSystem.setRootDirectory(tempDir.toFile());
        FileObject projectDir = fileSystem.getRoot();

        Method method = ReadmeNodeFactory.class.getDeclaredMethod("findDocumentationFiles", FileObject.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        List<FileObject> files = (List<FileObject>) method.invoke(null, projectDir);

        Set<String> fileNames = files.stream()
            .map(FileObject::getNameExt)
            .collect(Collectors.toSet());

        assertEquals(Set.of(
            changelog.getFileName().toString(),
            "changeslog.txt",
            "changes.txt",
            "LICENSE",
            "LICENSE.txt",
            "TODO",
            "TODO.txt",
            "README.md"
        ), fileNames);
        assertTrue(fileNames.stream().noneMatch(name -> name.equals("notes.txt")));
    }
}
