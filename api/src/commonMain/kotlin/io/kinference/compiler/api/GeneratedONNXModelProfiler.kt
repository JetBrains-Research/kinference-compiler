package io.kinference.compiler.api

import io.kinference.graph.ProfileAnalysisEntry

interface GeneratedONNXModelProfiler : GeneratedONNXModel {
    fun analyzeProfilingResults(): ProfileAnalysisEntry
    fun resetProfiles()
}
