/**
 * Copyright 2026 the original author or authors from the nb-jwt-edito project
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
 */
package ste.netbeans.readme;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

public class ReadmeNodeFactory implements NodeFactory {

    public static final Logger LOG = Logger.getLogger(ReadmeNodeFactory.class.getName());

    private static final List<String> TARGET_BASE_NAMES = List.of(
        "readme.md", "readme.txt", "readme", "readme.me",
        "changelog.md", "changelog.txt", "changelog",
        "license.md", "license.txt", "license", "license.html"
    );

    @Override
    public NodeList<?> createNodes(Project project) {
        FileObject projectDir = project.getProjectDirectory();
        if (projectDir == null) {
            return NodeFactorySupport.fixedNodeList();
        }

        // We always return the folder node now, because the folder itself
        // will dynamically hide/show its contents or handle its own visibility.
        Node infoFolderNode = new ProjectInfoFolderNode(projectDir);
        return NodeFactorySupport.fixedNodeList(infoFolderNode);
    }

    /**
     * Helper to scan for files case-insensitively.
     */
    private static List<FileObject> findDocumentationFiles(FileObject projectDir) {
        List<FileObject> results = new ArrayList<>();
        FileObject[] children = projectDir.getChildren();
        if (children != null) {
            for (FileObject child : children) {
                if (!child.isFolder() && VisibilityQuery.getDefault().isVisible(child)) {
                    String lowerName = child.getNameExt().toLowerCase();
                    if (TARGET_BASE_NAMES.contains(lowerName)) {
                        results.add(child);
                    }
                }
            }
        }
        return results;
    }

    @NbBundle.Messages("CTL_ProjectInfoFiles=Project Info")
    public static class ProjectInfoFolderNode extends AbstractNode {

        public ProjectInfoFolderNode(FileObject projectDir) {
            super(new ProjectInfoChildren(projectDir));
            setName("ProjectInfoFiles");
            setDisplayName(Bundle.CTL_ProjectInfoFiles());
        }

        @Override
        public java.awt.Image getIcon(int type) {
            // Loads the native NetBeans blue "i" info icon dynamically
            java.awt.Image img = ImageUtilities.loadImage("org/openide/awt/resources/info.png");

            // Fallback strategy: If your specific NetBeans platform cluster uses
            // a slightly different asset track, we fall back to a safe core option
            if (img == null) {
                img = ImageUtilities.loadImage("org/netbeans/modules/dialogs/info.png");
            }
            if (img == null) {
                // Absolute safety net: default back to the standard configuration gear/file layout
                img = ImageUtilities.loadImage("org/netbeans/modules/project/ui/resources/projectFiles.png");
            }

            return img;
        }

        @Override
        public java.awt.Image getOpenedIcon(int type) {
            // Keep the icon identical even when expanded so it doesn't flip back to an open folder
            return getIcon(type);
        }
    }

    private static class ProjectInfoChildren extends Children.Keys<FileObject> {
        private final FileObject projectDir;
        private FileChangeAdapter fileListener;

        public ProjectInfoChildren(FileObject projectDir) {
            this.projectDir = projectDir;
        }

        private void refreshKeys() {
            // Re-scan the folder and update NetBeans keys
            List<FileObject> updatedFiles = findDocumentationFiles(projectDir);
            setKeys(updatedFiles);
        }

        @Override
        protected void addNotify() {
            // 1. Initial scan when the node opens
            refreshKeys();

            // 2. Attach a live listener to the project directory for hard drive updates
            if (fileListener == null) {
                fileListener = new FileChangeAdapter() {
                    @Override
                    public void fileDataCreated(FileEvent fe) {
                        refreshKeys();
                    }

                    @Override
                    public void fileDeleted(FileEvent fe) {
                        refreshKeys();
                    }

                    @Override
                    public void fileRenamed(org.openide.filesystems.FileRenameEvent fe) {
                        refreshKeys();
                    }
                };
                projectDir.addFileChangeListener(fileListener);
            }
        }

        @Override
        protected void removeNotify() {
            // Detach the listener cleanly when the project closes to prevent memory leaks
            if (fileListener != null) {
                projectDir.removeFileChangeListener(fileListener);
                fileListener = null;
            }
            setKeys(new ArrayList<>());
        }

        @Override
        protected Node[] createNodes(FileObject key) {
            try {
                DataObject dobj = DataObject.find(key);
                return new Node[] { new FilterNode(dobj.getNodeDelegate(), Children.LEAF) };
            } catch (DataObjectNotFoundException ex) {
                // If a file was just deleted, find() might fail right before refreshKeys fires
                return new Node[0];
            }
        }
    }

    // --- Registrations ---
    @NodeFactory.Registration(projectType = "org-netbeans-modules-maven", position = 10)
    public static class MavenReadmeFactory extends ReadmeNodeFactory {}

    @NodeFactory.Registration(projectType = "org-netbeans-modules-gradle", position = 10)
    public static class GradleReadmeFactory extends ReadmeNodeFactory {}

    @NodeFactory.Registration(projectType = "org-netbeans-modules-java-j2seproject", position = 10)
    public static class AntReadmeFactory extends ReadmeNodeFactory {}
}