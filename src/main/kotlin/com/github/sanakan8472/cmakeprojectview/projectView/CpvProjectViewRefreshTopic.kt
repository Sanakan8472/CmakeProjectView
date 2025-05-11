package com.github.sanakan8472.cmakeprojectview.projectView

import com.intellij.util.messages.Topic

interface CpvProjectViewRefreshListener {
    fun refreshProjectView(force: Boolean)
    
    companion object {
        val TOPIC = Topic.create("CMake Project View Refresh", CpvProjectViewRefreshListener::class.java)
    }
}