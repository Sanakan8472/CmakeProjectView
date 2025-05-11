package com.github.sanakan8472.cmakeprojectview.projectView

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.annotations.Unmodifiable
import java.io.File

open class CpvTargetFolderNode<VALUE: Any>(
    project: Project,
    viewSettings: ViewSettings,
    var cpvTargetFolder: CpvTargetFolder,
    value: VALUE,
) : ProjectViewNode<VALUE>(project, value, viewSettings) {

    override fun contains(p0: VirtualFile): Boolean {
        return false
    }

    // This method makes the node sort like a directory in "show folders on top" mode
    //override fun getWeight(): Int = 10000

    override fun getChildren(): @Unmodifiable Collection<AbstractTreeNode<*>?> {
        val mutableList = mutableListOf<ProjectViewNode<*>>()
        for (cpvFolder in cpvTargetFolder.folders) {
            val folderNode = CpvTargetFolderNode<CpvTargetFolder>(project, settings, cpvFolder, cpvFolder)
            mutableList.add(folderNode)
        }

        for (relativeFilePath in cpvTargetFolder.files) {
            // Convert relative path to absolute path
            val projectBasePath = project.basePath
            if (projectBasePath != null) {
                val absolutePath = File(projectBasePath, relativeFilePath).path
                
                // Find VirtualFile for the path
                val virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath)
                
                // Create a node for the virtual file
                if (virtualFile != null && virtualFile.isValid) {
                    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                    if (psiFile != null) {
                        val fileNode = PsiFileNode(project, psiFile, settings)
                        mutableList.add(fileNode)
                    }
                }
            }
        }
        return mutableList
    }

    override fun update(presentation: PresentationData) {
        presentation.presentableText = cpvTargetFolder.name
        presentation.setIcon(AllIcons.Nodes.Folder)
        presentation.setTooltip("CMake Target Folder: ${cpvTargetFolder.name}")
    }

    override fun getSortOrder(settings: NodeSortSettings): NodeSortOrder {
        return if (settings.isFoldersAlwaysOnTop) NodeSortOrder.FOLDER else super.getSortOrder(settings)
    }

    override fun isAlwaysShowPlus(): Boolean {
        return !cpvTargetFolder.folders.isEmpty() || !cpvTargetFolder.files.isEmpty()
    }

    override fun isAlwaysLeaf(): Boolean {
        return cpvTargetFolder.folders.isEmpty() && cpvTargetFolder.files.isEmpty()
    }
}

class CpvTargetNode(
    project: Project,
    viewSettings: ViewSettings,
    var cpvTarget: CpvTarget,
) : CpvTargetFolderNode<CpvTarget>(project, viewSettings, cpvTarget, cpvTarget) {

    override fun update(presentation: PresentationData) {
        presentation.presentableText = cpvTarget.name
        presentation.setIcon(AllIcons.Nodes.Project)
        presentation.setTooltip("CMake Target: ${cpvTarget.name}")
    }
}

open class CpvProjectFolderNode(
    project: Project,
    viewSettings: ViewSettings,
    var cpvProjectFolder: CpvProjectFolder,
) : ProjectViewNode<CpvProjectFolder>(project, cpvProjectFolder, viewSettings) {

    override fun contains(p0: VirtualFile): Boolean {
        return false
    }

    override fun getChildren(): @Unmodifiable Collection<AbstractTreeNode<*>?> {
        val mutableList = mutableListOf<ProjectViewNode<*>>()
        for (cpvFolder in cpvProjectFolder.folders) {
            val folderNode = CpvProjectFolderNode(project, settings, cpvFolder)
            mutableList.add(folderNode)
        }
        for (cpvTarget in cpvProjectFolder.targets) {
            val folderNode = CpvTargetNode(project, settings, cpvTarget)
            mutableList.add(folderNode)
        }
        return mutableList
    }

    override fun update(presentation: PresentationData) {
        presentation.presentableText = cpvProjectFolder.name
        presentation.setIcon(AllIcons.Nodes.Folder)
        presentation.setTooltip("CMake Project Folder: ${value.name}")
    }

    override fun getSortOrder(settings: NodeSortSettings): NodeSortOrder {
        return if (settings.isFoldersAlwaysOnTop) NodeSortOrder.FOLDER else super.getSortOrder(settings)
    }

    override fun isAlwaysShowPlus(): Boolean {
        return !cpvProjectFolder.folders.isEmpty() || !cpvProjectFolder.targets.isEmpty()
    }

    override fun isAlwaysLeaf(): Boolean {
        return cpvProjectFolder.folders.isEmpty() && cpvProjectFolder.targets.isEmpty()
    }
}

class CpvProjectNode(
    project: Project,
    viewSettings: ViewSettings,
    var cpvProject: CpvProject,
) : CpvProjectFolderNode(project, viewSettings, cpvProject) {

    override fun update(presentation: PresentationData) {
        presentation.presentableText = cpvProject.name

        // Optional: Set a location string (shown in gray next to the name)
        //val targetCount = cpvProject.targets.count()
        //presentation.locationString = "($targetCount targets)"

        presentation.setIcon(AllIcons.Nodes.HomeFolder)
        presentation.setTooltip("CMake Project: ${value.name}")
    }
}

