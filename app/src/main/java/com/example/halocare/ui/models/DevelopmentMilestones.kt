package com.example.halocare.ui.models

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.util.UUID

// Individual milestone
data class Milestone(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val isAchieved: MutableState<Boolean> = mutableStateOf(false)
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

val pediatricMilestones = listOf(
    MilestoneAgeRange(
        ageRange = "1–1.5 Months",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "When held upright, holds head erect and steady.")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Cooes and babbles at parents and people they know")
            )),
            MilestoneCategory("Vision and Hearing", listOf(
                Milestone(description = "Focuses on parents."),
                Milestone(description = "Startled by sudden noises"),
                Milestone(description = "Recognition of familiar individuals")
            )),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Loves looking at new faces"),
                Milestone(description = "Starts to smile at parents")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "1.6–2 Months",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "When prone, lifts self by arms"),
                Milestone(description = "Rolls from side to back")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Vocalizes"),
                Milestone(description = "Cooes (makes vowel-like noises) or babbles.")
            )),
            MilestoneCategory("Vision and Hearing", listOf(
                Milestone(description = "Focuses on objects as well as adults")
            )),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Loves looking at new faces"),
                Milestone(description = "Smiles at parent"),
                Milestone(description = "Starting to smile")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "2.1–2.5 Months",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Rolls from tummy to side"),
                Milestone(description = "Rests on elbows, lifts head 90 degrees"),
                Milestone(description = "Sits propped up with hands, head steady for a short time")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Changes sounds while verbalizing, 'eee-ahhh'"),
                Milestone(description = "Verbalizes to engage someone in an interaction"),
                Milestone(description = "Blows bubbles, plays with tongue"),
                Milestone(description = "Deep belly laughs")
            )),
            MilestoneCategory("Vision and Hearing", listOf(
                Milestone(description = "Hand regard: following the hand with the eyes"),
                Milestone(description = "Color vision adult-like."),
                Milestone(description = "Serves to practice emerging visual skills.")
            )),
            MilestoneCategory("Social", emptyList()) // No specific entries for social here
        )
    ),
    MilestoneAgeRange(
        ageRange = "3 Months",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Prone: head held up for prolonged periods"),
                Milestone(description = "No grasp reflex")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Makes vowel noises")
            )),
            MilestoneCategory("Vision and Hearing", listOf(
                Milestone(description = "Follows dangling toy from side to side"),
                Milestone(description = "Turns head around to sound"),
                Milestone(description = "Follows adults' gaze (joint attention)"),
                Milestone(description = "Sensitivity to binocular cues emerges")
            )),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Squeals with delight appropriately"),
                Milestone(description = "Discriminates smile. Smiles often"),
                Milestone(description = "Laughs at simple things"),
                Milestone(description = "Reaches out for objects")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "5 Months",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Holds head steady"),
                Milestone(description = "Goes for objects and gets them"),
                Milestone(description = "Objects taken to the mouth")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Enjoys vocal play")
            )),
            MilestoneCategory("Vision and Hearing", listOf(
                Milestone(description = "Able to reach hanging objects and grab them"),
                Milestone(description = "Noticing colors"),
                Milestone(description = "Adjusts hand shape to the shape of toy before picking up")
            )),
            MilestoneCategory("Social", emptyList()) // No new data listed
        )
    ),
    MilestoneAgeRange(
        ageRange = "6 Months",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Transfers objects from one hand to the other"),
                Milestone(description = "Pulls self up to sit and sits erect with supports"),
                Milestone(description = "Rolls over from tummy to back"),
                Milestone(description = "Palmar grasp of cube; hand-to-hand eye coordination")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Double syllable sounds such as 'mum' and 'dada'"),
                Milestone(description = "Babbles (consonant-vowel combinations)")
            )),
            MilestoneCategory("Vision and Hearing", listOf(
                Milestone(description = "Localises sound 45 cm (18 in) lateral to either ear"),
                Milestone(description = "Visual acuity adult-like (20/20)"),
                Milestone(description = "Sensitivity to pictorial depth cues emerges")
            )),
            MilestoneCategory("Social", listOf(
                Milestone(description = "May show stranger anxiety")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "9–10 Months",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Wiggles and crawls"),
                Milestone(description = "Sits unsupported"),
                Milestone(description = "Picks up objects with pincer grasp")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Babbles tunefully")
            )),
            MilestoneCategory("Vision and Hearing", listOf(
                Milestone(description = "Looks for toys dropped")
            )),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Apprehensive about strangers")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "1 Year",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Stands holding furniture"),
                Milestone(description = "Stands alone for a second or two, then collapses with a bump")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Babbles 2 or 3 words repeatedly")
            )),
            MilestoneCategory("Vision and Hearing", listOf(
                Milestone(description = "Drops toys, and watches where they go")
            )),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Cooperates with dressing"),
                Milestone(description = "Waves goodbye"),
                Milestone(description = "Understands simple commands")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "18 Months",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Can walk alone"),
                Milestone(description = "Picks up a toy without falling over"),
                Milestone(description = "Gets up/down stairs holding onto rail"),
                Milestone(description = "Begins to jump with both feet"),
                Milestone(description = "Can build a tower of 3 or 4 cubes and throw a ball"),
                Milestone(description = "Supinate grasping position is usually seen as the first grasping position utilized")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "'Jargon': Many intelligible words"),
                Milestone(description = "Recognizes favourite songs and tries to join in")
            )),
            MilestoneCategory("Vision and Hearing", emptyList()), // Not explicitly mentioned
            MilestoneCategory("Social", listOf(
                Milestone(description = "Demands constant mothering"),
                Milestone(description = "Drinks from a cup with both hands"),
                Milestone(description = "Feeds self with a spoon")
            ))
        )
    ),

    MilestoneAgeRange(
        ageRange = "2 Years",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Able to run"),
                Milestone(description = "Walks up and down stairs using two footsteps per stair step"),
                Milestone(description = "Builds tower of 6 cubes")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Joins 2–3 words in sentences"),
                Milestone(description = "Able to repeat words that they hear"),
                Milestone(description = "Gradually builds their vocabulary"),
                Milestone(description = "Able to recognize words")
            )),
            MilestoneCategory("Vision and Hearing", emptyList()),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Parallel play"),
                Milestone(description = "Daytime bladder control")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "3 Years",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Goes upstairs one footstep per stair step and downstairs two footsteps per stair step"),
                Milestone(description = "Copies circle, imitates hand motions and draws man on request"),
                Milestone(description = "Builds tower of 9 cubes"),
                Milestone(description = "Pronate method of grasping develops")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Constantly asks questions"),
                Milestone(description = "Speaks in sentences")
            )),
            MilestoneCategory("Vision and Hearing", emptyList()),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Cooperative play"),
                Milestone(description = "Undresses with assistance"),
                Milestone(description = "Imaginary companions")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "4 Years",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Goes both up and down stairs using one footstep per stair step"),
                Milestone(description = "Postural capacity needed to control balance in walking not attained yet"),
                Milestone(description = "Skips on one foot"),
                Milestone(description = "Imitates gate with cubes"),
                Milestone(description = "Copies a cross"),
                Milestone(description = "Classic tripod grip develops and is made more efficient (between 4 and 6 years)")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Questioning at its height"),
                Milestone(description = "Many infantile substitutions in speech")
            )),
            MilestoneCategory("Vision and Hearing", emptyList()),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Dresses and undresses with assistance"),
                Milestone(description = "Attends to own toilet needs")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "5 Years",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Skips on both feet and hops"),
                Milestone(description = "Begins to control balance not attained at 3–4 years of age"),
                Milestone(description = "Begins to control gravitational forces in walking"),
                Milestone(description = "Draws a stick figure and copies a hexagonal-based pyramid using graphing paper")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Gives age"),
                Milestone(description = "Fluent speech with few infantile substitutions")
            )),
            MilestoneCategory("Vision and Hearing", emptyList()),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Dresses and undresses alone")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "6 Years",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Adult muscle activation pattern in walking begins to complete"),
                Milestone(description = "Leads to head control and trunk coordination while walking by age 8"),
                Milestone(description = "Mechanical energy transfer exists"),
                Milestone(description = "Copies a diamond")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Knows right from left and number of fingers"),
                Milestone(description = "Fluent speech")
            )),
            MilestoneCategory("Vision and Hearing", emptyList()),
            MilestoneCategory("Social", emptyList())
        )
    ),
    MilestoneAgeRange(
        ageRange = "7 Years",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Hand-eye coordination is well developed"),
                Milestone(description = "Has good balance"),
                Milestone(description = "Can execute simple gymnastic movements, such as somersaults")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Uses a vocabulary of several thousand words"),
                Milestone(description = "Demonstrates a longer attention span"),
                Milestone(description = "Uses serious, logical attention span"),
                Milestone(description = "Able to understand reasoning and make decisions")
            )),
            MilestoneCategory("Vision and Hearing", emptyList()),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Desires to be perfect and is quite self-critical"),
                Milestone(description = "Worries more and may have low self-confidence"),
                Milestone(description = "Tends to complain and has strong emotional reactions")
            ))
        )
    ),
    MilestoneAgeRange(
        ageRange = "8 Years",
        categories = listOf(
            MilestoneCategory("Motor", listOf(
                Milestone(description = "Can tie shoelaces"),
                Milestone(description = "Can draw a diamond shape"),
                Milestone(description = "Increasingly skilled in hobbies, sports, and active play")
            )),
            MilestoneCategory("Speech", listOf(
                Milestone(description = "Well-developed speech with mostly correct grammar"),
                Milestone(description = "Interested in reading books"),
                Milestone(description = "Still developing spelling and grammar in writing")
            )),
            MilestoneCategory("Vision and Hearing", emptyList()),
            MilestoneCategory("Social", listOf(
                Milestone(description = "Shows more independence from parents and family"),
                Milestone(description = "Thinks about the future"),
                Milestone(description = "Understands place in the world"),
                Milestone(description = "Pays more attention to friendships and teamwork")
            ))
        )
    )
)
