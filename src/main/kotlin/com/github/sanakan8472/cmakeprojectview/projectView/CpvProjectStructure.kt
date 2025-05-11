package com.github.sanakan8472.cmakeprojectview.projectView

import com.intellij.openapi.project.Project
import com.intellij.ide.projectView.ProjectView
import com.intellij.ide.projectView.ProjectViewSettings
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ProjectTreeStructure
import com.intellij.ide.util.treeView.AbstractTreeNode


open class CpvTargetFolder(val name: String) {
    val folders = mutableListOf<CpvTargetFolder>()
    val files = mutableListOf<String>()

    fun findOrAddFolder(name: String): CpvTargetFolder {
        // Look for existing folder with the same name
        val existingFolder = folders.find { it.name == name }
        if (existingFolder != null) {
            return existingFolder
        }

        // Create new folder
        val newFolder = CpvTargetFolder(name)
        folders.add(newFolder)

        // Sort folders by name
        folders.sortBy { it.name }

        return newFolder
    }
}

class CpvTarget(name: String) : CpvTargetFolder(name) {

    fun addFolder(folder: String): CpvTargetFolder {
        var folder2 = folder.replace('\\', '/')
        folder2 = folder2.trim('/')
        val folders = folder2.split('/')
        var currentFolder: CpvTargetFolder = this
        // Process folders sequentially
        folders.forEach { folder ->
            if (folder.isNotEmpty()) {
                currentFolder = currentFolder.findOrAddFolder(folder)
            }
        }
        return currentFolder
    }
}

open class CpvProjectFolder(val name: String) {
    val folders = mutableListOf<CpvProjectFolder>()
    val targets = mutableListOf<CpvTarget>()

    fun findOrAddFolder(name: String): CpvProjectFolder {
        // Look for existing folder with the same name
        val existingFolder = folders.find { it.name == name }
        if (existingFolder != null) {
            return existingFolder
        }

        // Create new folder
        val newFolder = CpvProjectFolder(name)
        folders.add(newFolder)

        // Sort folders by name
        folders.sortBy { it.name }

        return newFolder
    }
}

class CpvProject(name: String) : CpvProjectFolder(name) {

    fun addTarget(folder: String, name: String): CpvTarget {
        var folder2 = folder.replace('\\', '/')
        folder2 = folder2.trim('/')
        val folders = folder2.split('/')
        var currentFolder: CpvProjectFolder = this
        // Process folders sequentially
        folders.forEach { folder ->
            if (folder.isNotEmpty()) {
                currentFolder = currentFolder.findOrAddFolder(folder)
            }
        }
        // Add target to the final current folder
        currentFolder.targets.add(CpvTarget(name))
        return currentFolder.targets.last()
    }
}

class CpvConfiguration(val profileName: String) {
    val projects = mutableListOf<CpvProject>()
}

class ProjectViewPaneTreeStructure(private val project: Project, private val id: String) :
    ProjectTreeStructure(project, id), ProjectViewSettings, CpvProjectViewRefreshListener {

    var rootNode: CpvProjectRootNode? = null

    init {
        // Subscribe to refresh events
        project.messageBus.connect().subscribe(CpvProjectViewRefreshListener.TOPIC, this)
    }

    override fun refreshProjectView(force: Boolean) {
        rootNode?.updateRoot(force)
        val projectView = ProjectView.getInstance(project)
        projectView.refresh()
    }

    override fun createRoot(project: Project, settings: ViewSettings): AbstractTreeNode<*> {
        val newRootNode = CpvProjectRootNode(project, project, settings)
        rootNode = newRootNode
        return newRootNode
    }

    override fun isShowLibraryContents() = false
}