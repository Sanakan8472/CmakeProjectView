package com.github.sanakan8472.cmakeprojectview.projectView

import com.intellij.openapi.project.Project
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget
import com.intellij.execution.ExecutionTargetManager
import java.nio.file.Files
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.collections.mutableMapOf
import kotlin.io.resolve


class CpvQueryResultParser() {

    @Serializable
    data class CpvTargetFolderObj(val name: String)

    @Serializable
    data class CpvTargetPaths(
        val source: String,
        val build: String,
    )

    @Serializable
    data class CpvTargetSourceGroup(
        val name: String,
        val sourceIndexes: List<Int>,
    )

    @Serializable
    data class CpvTargetSourceEntry(
        val sourceGroupIndex: Int?,
        val path: String,
    )

    @Serializable
    data class CpvTargetFile(
        val id: String,
        val type: String,
        val name: String,
        val nameOnDisk: String? = null,
        val paths: CpvTargetPaths,
        val folder: CpvTargetFolderObj? = null,
        val sourceGroups: List<CpvTargetSourceGroup>? = null,
        val sources: List<CpvTargetSourceEntry>,
    )


    @Serializable
    data class CpvCodeModelProject(
        val name: String,
        val parentIndex: Int? = null,
        val childIndexes: List<Int>? = null,
        val targetIndexes: List<Int>? = null,
    )

    @Serializable
    data class CpvCodeModelTarget(
        val directoryIndex: Int,
        val id: String,
        val jsonFile: String,
        val name: String,
        val projectIndex: Int,
    )

    @Serializable
    data class CpvCodeModelConfigurations(
        val projects: List<CpvCodeModelProject>,
        val targets: List<CpvCodeModelTarget>,
    )

    @Serializable
    data class CpvCodeModelFile(
        val configurations: List<CpvCodeModelConfigurations>,
    )

    data class CpvCodeModel(var config: CpvCodeModelConfigurations, var targets: Map<String, CpvTargetFile>)

    private fun parseCMakeFiles(generatorDir: java.io.File): CpvCodeModel? {
        val apiResultPath = generatorDir.resolve(".cmake/api/v1/reply")
        if (!apiResultPath.exists())
            return null

        val codemodelFile = apiResultPath.listFiles()?.find { it.name.startsWith("codemodel-v2") }
        if (codemodelFile == null)
            return null

        val relaxedJson = Json {
            ignoreUnknownKeys = true
        }

        val jsonContent = Files.readString(codemodelFile.toPath())
        var codeModel: CpvCodeModelFile
        try {
            codeModel = relaxedJson.decodeFromString<CpvCodeModelFile>(jsonContent)

        } catch (e: Exception) {
            println("Exception: $e");
            return null
        }

        val config = codeModel.configurations.firstOrNull()
        if (config == null)
            return null
        val targets = mutableMapOf<String, CpvTargetFile>()

        config.targets.forEach { target ->
            try {
                val absJsonPath = apiResultPath.resolve(target.jsonFile)
                val jsonContent = Files.readString(absJsonPath.toPath())
                val targetFile = relaxedJson.decodeFromString<CpvTargetFile>(jsonContent)
                targets.put(target.id, targetFile)
            } catch (e: Exception) {
                println("");
                // Code for handling the exception
            }
            println("");
        }

        return CpvCodeModel(config, targets)

    }

    private fun parseConfigurationForProfile(project: Project, profileName: String): CpvConfiguration? {
        val configuration = CpvConfiguration(profileName)
        //val workspace = project.workspaceFile


        val cmakeWorkspace = CMakeWorkspace.getInstance(project)
        if (!cmakeWorkspace.isInitialized)
            return null

        val profile = cmakeWorkspace.getCMakeProfileInfoByName(profileName)
        if (profile == null)
            return null

        val generatorDir = profile.generationDir
        val codeModel = parseCMakeFiles(generatorDir)
        if (codeModel == null)
            return null

        codeModel.config.projects.forEach { projectModel ->
            val name = projectModel.name
            val resolvedTargets = mutableListOf<CpvTargetFile>()
            projectModel.targetIndexes?.forEach { targetIndex ->
                val targetId = codeModel.config.targets[targetIndex].id
                val target = codeModel.targets[targetId]
                if (target != null) {
                    resolvedTargets.add(target)
                }
            }

            val newProject = CpvProject(name)
            for (targetFile in resolvedTargets) {
                val projectFolder = targetFile.folder?.name ?: ""
                val target: CpvTarget = newProject.addTarget(projectFolder, targetFile.name)

                if (targetFile.sourceGroups != null) {
                    for (sourceGroup in targetFile.sourceGroups) {
                        var targetFolder: CpvTargetFolder = target
                        // Inline these two folder that CMake seems to always produce.
                        if (sourceGroup.name != "Precompile Header File" && sourceGroup.name != "Source Files" ) {
                            targetFolder = target.addFolder(sourceGroup.name)
                        }
                        for (sourceIndex in sourceGroup.sourceIndexes) {
                            val file = targetFile.sources[sourceIndex].path
                            // Ignore PCH files
                            if (file.endsWith("/cmake_pch.hxx.cxx") || file.endsWith("/cmake_pch.hxx"))
                                continue
                            targetFolder.files.add(file)
                        }
                        targetFolder.files.sort()
                    }
                }
                // Files not in a source group
                for (source in targetFile.sources) {
                    if (source.sourceGroupIndex == null) {
                        target.files.add(source.path);
                    }
                }
                target.files.sort()
            }

            configuration.projects.add(newProject)
        }
        return configuration
    }

    fun parseConfigurationForActiveProfile(project: Project): CpvConfiguration? {
        val target = ExecutionTargetManager.getInstance(project).activeTarget
        if (target !is CMakeBuildProfileExecutionTarget)
            return null

        val configuration = parseConfigurationForProfile(project, target.profileName)
        return configuration
    }
}