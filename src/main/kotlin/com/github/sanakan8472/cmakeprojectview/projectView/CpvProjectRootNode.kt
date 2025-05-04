package com.github.sanakan8472.cmakeprojectview.projectView

import com.intellij.execution.ExecutionTargetManager
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget

class CpvProjectRootNode(
    project: Project,
    value: Project,
    viewSettings: ViewSettings
) : ProjectViewNode<Project>(project, value, viewSettings) {

    val parser = CpvQueryResultParser()
    var configuration: CpvConfiguration? = null

    init {
        configuration = parser.parseConfigurationForActiveProfile(project)
    }

    override fun getChildren(): Collection<ProjectViewNode<*>> {
        val mutableList = mutableListOf<ProjectViewNode<*>>()
        configuration?.let {
            for (cpvProject in it.projects) {
                val projectNode = CpvProjectNode(project, settings, cpvProject)
                mutableList.add(projectNode)
            }
        }

        return mutableList
    }

    override fun contains(file: VirtualFile): Boolean {
        return false
    }

    override fun update(presentation: com.intellij.ide.projectView.PresentationData) {}

    fun updateRoot(force: Boolean) {
        if (configuration == null) {
            configuration = parser.parseConfigurationForActiveProfile(project)
        } else {
            val target = ExecutionTargetManager.getInstance(project).activeTarget
            if (target !is CMakeBuildProfileExecutionTarget)
                return

            if (configuration!!.profileName != target.profileName) {
                configuration = parser.parseConfigurationForActiveProfile(project)
            }
        }

    }
}
