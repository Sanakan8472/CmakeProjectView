package com.github.sanakan8472.cmakeprojectview.projectView

import com.intellij.execution.ExecutionTarget
import com.intellij.execution.ExecutionTargetListener
import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.CMakeRunner.CMakeOutput
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspaceListener
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget
import com.jetbrains.cidr.project.workspace.CidrWorkspaceListener
import com.jetbrains.cidr.project.workspace.CidrWorkspaceState
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path

class CpvProjectViewEventListener(
    val project: Project,
    parentScope: CoroutineScope,
) : CMakeWorkspaceListener, ExecutionTargetListener, CidrWorkspaceListener {

    override fun activeTargetChanged(target: ExecutionTarget) {
        if (target !is CMakeBuildProfileExecutionTarget)
            return

        println("activeTargetChanged ${target.profileName}")
        // Refresh tree on target change
        project.messageBus.syncPublisher(CpvProjectViewRefreshListener.TOPIC).refreshProjectView(false)
    }

    override fun workspaceStateChanged(
        workspace: com.jetbrains.cidr.project.workspace.CidrWorkspace,
        oldState: com.jetbrains.cidr.project.workspace.CidrWorkspaceState,
        newState: com.jetbrains.cidr.project.workspace.CidrWorkspaceState,
        allWorkspaces: kotlin.collections.Map<com.jetbrains.cidr.project.workspace.CidrWorkspace, com.jetbrains.cidr.project.workspace.CidrWorkspaceState>
    ): kotlin.Unit {
        if (workspace is CMakeWorkspace && newState == CidrWorkspaceState.Initialized)
        {
            project.messageBus.syncPublisher(CpvProjectViewRefreshListener.TOPIC).refreshProjectView(false)
        }
        println("workspaceStateChanged")
    }

    override fun initialized(workspace: com.jetbrains.cidr.project.workspace.CidrWorkspace): kotlin.Unit { /* compiled code */
        println("initialized")
    }

    override fun shutdown(workspace: com.jetbrains.cidr.project.workspace.CidrWorkspace): kotlin.Unit { /* compiled code */
        println("shutdown")
    }

    override fun reloadingStarted() {
        println("reloadingStarted")
    }

    override fun reloadingScheduled() {
        println("reloadingScheduled")
    }

    override fun reloadingRescheduled() {
        println("reloadingRescheduled")
    }

    override fun generationStarted() {
        println("generationStarted")
    }

    override fun generationFinished() {
        println("generationFinished")
    }

    override fun generationCMakeExited(output: CMakeOutput) {
        println("generationCMakeExited")
    }

    override fun filesRefreshedAfterGeneration() {
        println("filesRefreshedAfterGeneration")
    }

    override fun beforeApplying() {
        println("beforeApplying")
    }

    override fun reloadingFinished(canceled: Boolean) {
        println("reloadingFinished")
        // Refresh tree when CMake reload is complete
        project.messageBus.syncPublisher(CpvProjectViewRefreshListener.TOPIC).refreshProjectView(true)
    }

    override open fun generationDirCleared(generationDir: Path) {
        println("generationDirCleared")
    }
}