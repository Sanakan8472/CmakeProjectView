package com.github.sanakan8472.cmakeprojectview.projectView

import com.intellij.ide.projectView.impl.AbstractProjectViewPaneWithAsyncSupport
import com.intellij.openapi.project.Project
import com.intellij.ide.SelectInTarget
import com.intellij.ide.impl.ProjectViewSelectInTarget
import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase
import com.intellij.ide.projectView.impl.ProjectViewTree
import com.intellij.psi.PsiFileSystemItem
import javax.swing.tree.DefaultTreeModel

class CpvProjectView(project: Project) : AbstractProjectViewPaneWithAsyncSupport(project) {
    override fun getTitle(): String = "CMake Project"
    override fun getId(): String = "CMAKE_PROJECT_VIEW"
    override fun getIcon() = AllIcons.General.ProjectTab
    override fun getWeight() = 100
    override fun createSelectInTarget(): SelectInTarget {
        return object : ProjectViewSelectInTarget(myProject) {
            override fun toString(): String {
                return title
            }

            override fun getMinorViewId(): String {
                return id
            }

            override fun getWeight(): Float {
                return this@CpvProjectView.getWeight().toFloat()
            }

            override fun canSelect(file: PsiFileSystemItem): Boolean {
                return super.canSelect(file)
            }
        }
    }

    override fun createStructure(): ProjectAbstractTreeStructureBase {
        return ProjectViewPaneTreeStructure(myProject, getId())
    }

    override fun createTree(treeModel: DefaultTreeModel): ProjectViewTree {
        return object : ProjectViewTree(treeModel) {
            override fun toString(): String {
                return title + " " + super.toString()
            }
        }
    }
}

