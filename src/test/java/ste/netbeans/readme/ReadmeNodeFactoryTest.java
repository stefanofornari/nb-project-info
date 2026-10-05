/**
 * Copyright 2026 the original author or authors from the nb-project-info project
 * (https://github.com/stefanofornari/nb-project-info).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */package ste.netbeans.readme;

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
