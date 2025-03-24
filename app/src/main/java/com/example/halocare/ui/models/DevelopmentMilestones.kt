package com.example.halocare.ui.models

import java.util.UUID

// Individual milestone
data class Milestone(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    var isAchieved: Boolean = false
)

// Subcategory (Motor, Cognitive, Social, etc.)
data class MilestoneCategory(
    val name: String, // e.g., "Motor Skills", "Cognitive Skills"
    val milestones: List<Milestone>
)

// Age Group (0-3 months, 4-6 months, etc.)
data class MilestoneAgeRange(
    val ageRange: String, // e.g., "0-3 Months"
    val categories: List<MilestoneCategory>
)
