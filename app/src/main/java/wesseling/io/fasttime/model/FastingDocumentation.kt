package wesseling.io.fasttime.model

/**
 * Documentation about fasting states and their scientific benefits
 */
object FastingDocumentation {
    
    /**
     * Get detailed documentation for a specific fasting state
     */
    fun getDocumentationForState(state: FastingState): FastingStateInfo {
        return when (state) {
            FastingState.NOT_FASTING -> FastingStateInfo(
                title = "Fed State (0-4 hours)",
                benefits = listOf(
                    "Digestion and absorption of nutrients",
                    "Energy storage for later use",
                    "Protein synthesis and tissue repair",
                    "Replenishment of glycogen stores"
                ),
                scientificDetails = "During the fed state (0-4 hours after eating), your body prioritizes using glucose from food as its primary energy source. Insulin levels rise to facilitate glucose uptake by cells and to promote energy storage. This is when your body is actively digesting and absorbing nutrients from your meal.",
                warnings = listOf(
                    "Extended periods without fasting may lead to consistently elevated insulin levels",
                    "Continuous feeding without fasting intervals can reduce metabolic flexibility",
                    "Constant digestion without breaks can stress the digestive system"
                )
            )
            
            FastingState.EARLY_FAST -> FastingStateInfo(
                title = "Early Fasting State (4-12 hours)",
                benefits = listOf(
                    "Blood glucose and insulin levels begin to drop",
                    "Your body starts to transition from using glucose to stored glycogen",
                    "Some fat-burning begins as glycogen is depleted",
                    "Digestive system gets a break, reducing inflammation"
                ),
                scientificDetails = "After 4-12 hours without food, your liver glycogen (stored glucose) begins to be used. As insulin levels fall, your body gradually shifts toward using stored fat for energy through a process called lipolysis. This is the beginning of the metabolic switch from glucose to fat utilization.",
                warnings = listOf(
                    "You may experience hunger pangs as your body adjusts",
                    "Blood sugar fluctuations may cause mild irritability or difficulty concentrating for some people",
                    "Those with medical conditions affecting blood sugar should consult healthcare providers"
                )
            )
            
            FastingState.GLYCOGEN_DEPLETION -> FastingStateInfo(
                title = "Glycogen Depletion (12-18 hours)",
                benefits = listOf(
                    "Liver glycogen stores become significantly depleted",
                    "Fat breakdown (lipolysis) increases substantially",
                    "Blood ketones begin to rise slightly",
                    "Autophagy (cellular cleanup) starts increasing",
                    "Improved mental clarity for many people"
                ),
                scientificDetails = "Between 12-18 hours of fasting, your liver glycogen stores become depleted, and your body increases fat oxidation. The liver begins converting fatty acids into ketone bodies (acetoacetate, beta-hydroxybutyrate, and acetone), which serve as an alternative fuel source for the brain and other organs. This marks the beginning of metabolic flexibility.",
                warnings = listOf(
                    "Initial adaptation to fat metabolism may cause temporary fatigue",
                    "People with certain medical conditions should consult healthcare providers",
                    "Proper hydration becomes increasingly important during this phase"
                )
            )
            
            FastingState.METABOLIC_SHIFT -> FastingStateInfo(
                title = "Metabolic Shift (18-24 hours)",
                benefits = listOf(
                    "Ketosis becomes more significant as fat metabolism ramps up",
                    "Blood ketone levels rise further, providing an alternative energy source",
                    "Growth hormone production increases to preserve muscle mass",
                    "Autophagy continues to accelerate",
                    "Reduced inflammation throughout the body"
                ),
                scientificDetails = "Between 18-24 hours, a significant metabolic shift occurs as your body relies more heavily on ketones for energy. Growth hormone secretion increases to preserve muscle mass and promote fat utilization. Autophagy (cellular cleanup) accelerates, removing damaged cellular components and proteins. This period represents a transition into deeper ketosis.",
                warnings = listOf(
                    "Some people may experience 'keto flu' symptoms as the body adapts",
                    "Those with medical conditions should consult healthcare providers",
                    "Electrolyte balance becomes increasingly important"
                )
            )
            
            FastingState.DEEP_KETOSIS -> FastingStateInfo(
                title = "Deep Ketosis & Increased Autophagy (24-48 hours)",
                benefits = listOf(
                    "The body relies primarily on fat and ketones for energy",
                    "Autophagy peaks, removing damaged cells and proteins",
                    "Inflammation decreases significantly",
                    "Insulin levels remain very low, improving insulin sensitivity",
                    "Human Growth Hormone (HGH) surges, supporting muscle preservation"
                ),
                scientificDetails = "Between 24-48 hours, your body enters deep ketosis, with ketones becoming a primary fuel source. Autophagy reaches peak levels, promoting cellular renewal and repair. Growth hormone levels increase significantly (up to 5x baseline), helping preserve lean muscle mass while fat burning accelerates. Insulin sensitivity improves dramatically during this phase.",
                warnings = listOf(
                    "Extended fasting should be approached with proper knowledge",
                    "Not recommended for those who are underweight or have eating disorders",
                    "May require electrolyte supplementation to prevent imbalances",
                    "Consult healthcare provider before attempting fasts longer than 24 hours"
                )
            )
            
            FastingState.IMMUNE_RESET -> FastingStateInfo(
                title = "Immune System Reset & Peak Fat Burning (48-72 hours)",
                benefits = listOf(
                    "Stem cell production increases (immune system regeneration begins)",
                    "Insulin sensitivity improves dramatically",
                    "The gut lining starts regenerating",
                    "Mental clarity and focus enhanced due to high ketone levels",
                    "Significant anti-inflammatory effects throughout the body"
                ),
                scientificDetails = "Between 48-72 hours, a process called 'autophagy-dependent immune system reconfiguration' begins. Studies show that prolonged fasting reduces circulating IGF-1 levels and PKA activity, promoting stem cell-based regeneration of immune cells. The gut microbiome undergoes significant changes, and the intestinal lining begins to regenerate. Fat oxidation remains high while protein breakdown is minimized through metabolic adaptations.",
                warnings = listOf(
                    "Extended fasting at this duration requires careful preparation",
                    "Electrolyte supplementation becomes essential",
                    "Breaking the fast properly is crucial to avoid digestive distress",
                    "Not recommended for those with certain medical conditions",
                    "Should be done under supervision for those new to extended fasting"
                )
            )
            
            FastingState.EXTENDED_FAST -> FastingStateInfo(
                title = "Prolonged Fasting Benefits (72+ hours)",
                benefits = listOf(
                    "Stem cell regeneration increases further",
                    "The immune system undergoes significant rejuvenation",
                    "Growth pathways like IGF-1 remain suppressed, potentially extending longevity",
                    "Maximum autophagy benefits throughout the body",
                    "Profound metabolic reset and inflammation reduction"
                ),
                scientificDetails = "Beyond 72 hours, fasting triggers significant stem cell regeneration and immune system rejuvenation. Research by Dr. Valter Longo and others shows that prolonged fasting can 'reset' the immune system through stem cell activation. The body enters a state of profound autophagy and cellular renewal. Growth pathways like IGF-1 remain suppressed, which has been linked to longevity benefits in research studies.",
                warnings = listOf(
                    "Risk of muscle breakdown increases slightly, though fat remains the primary fuel",
                    "Extended fasting should only be done with proper medical supervision",
                    "Not recommended for those who are underweight or have eating disorders",
                    "Requires careful refeeding protocol when breaking the fast",
                    "Potential for electrolyte imbalances if not properly managed",
                    "Consult healthcare provider before attempting fasts longer than 72 hours"
                )
            )
        }
    }
    
    /**
     * Data class to hold information about a fasting state
     */
    data class FastingStateInfo(
        val title: String,
        val benefits: List<String>,
        val scientificDetails: String,
        val warnings: List<String>
    )
}
