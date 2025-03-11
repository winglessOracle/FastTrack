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
                title = "Feeding State",
                benefits = listOf(
                    "Your body is in the process of digesting and absorbing nutrients",
                    "Insulin levels are elevated to help cells absorb glucose",
                    "Energy is primarily derived from recently consumed food"
                ),
                scientificDetails = "During the feeding state, your body prioritizes using glucose from food as its primary energy source. Insulin levels rise to facilitate glucose uptake by cells and to promote energy storage.",
                warnings = listOf(
                    "Extended periods without fasting may lead to consistently elevated insulin levels",
                    "Continuous feeding without fasting intervals can reduce metabolic flexibility"
                )
            )
            
            FastingState.EARLY_FAST -> FastingStateInfo(
                title = "Early Fasting (5-13 hours)",
                benefits = listOf(
                    "Blood glucose and insulin levels begin to drop",
                    "Your body starts to transition from using glucose to fat for energy",
                    "Digestive system gets a break, reducing inflammation"
                ),
                scientificDetails = "After 5-6 hours without food, your liver glycogen (stored glucose) begins to be depleted. As insulin levels fall, your body gradually shifts toward using stored fat for energy through a process called lipolysis.",
                warnings = listOf(
                    "You may experience hunger pangs as your body adjusts",
                    "Blood sugar fluctuations may cause mild irritability or difficulty concentrating for some people"
                )
            )
            
            FastingState.KETOSIS -> FastingStateInfo(
                title = "Ketosis (13-17 hours)",
                benefits = listOf(
                    "Fat burning accelerates as liver glycogen is depleted",
                    "Your liver produces ketone bodies, an alternative fuel source for your brain",
                    "Improved mental clarity and focus for many people",
                    "Reduced inflammation markers"
                ),
                scientificDetails = "After 13+ hours of fasting, liver glycogen is significantly depleted, and your body increases fat oxidation. The liver converts fatty acids into ketone bodies (acetoacetate, beta-hydroxybutyrate, and acetone), which serve as an efficient alternative fuel source for the brain and other organs.",
                warnings = listOf(
                    "Initial adaptation to ketosis may cause temporary fatigue or 'keto flu' symptoms",
                    "People with certain medical conditions (like type 1 diabetes) should consult healthcare providers before pursuing ketosis",
                    "Ketosis is different from ketoacidosis, which is a dangerous condition primarily affecting people with diabetes"
                )
            )
            
            FastingState.AUTOPHAGY -> FastingStateInfo(
                title = "Autophagy (17-25 hours)",
                benefits = listOf(
                    "Cellular 'self-eating' process removes damaged components",
                    "Recycling of old and dysfunctional proteins",
                    "May help reduce risk of neurodegenerative diseases",
                    "Potential anti-aging effects at cellular level",
                    "Enhanced cellular repair mechanisms"
                ),
                scientificDetails = "Autophagy is a cellular cleaning process where cells break down and recycle damaged components. Research suggests autophagy is significantly upregulated after 17-25 hours of fasting. This process is regulated by several nutrient-sensing pathways, including mTOR (inhibited during fasting) and AMPK (activated during fasting).",
                warnings = listOf(
                    "Extended fasting isn't recommended for pregnant or breastfeeding women",
                    "Those with medical conditions should consult healthcare providers before extended fasting",
                    "While autophagy has promising research, many human studies are still preliminary"
                )
            )
            
            FastingState.DEEP_FASTING -> FastingStateInfo(
                title = "Deep Fasting (25+ hours)",
                benefits = listOf(
                    "Significant increase in human growth hormone (HGH)",
                    "Enhanced fat breakdown and ketone production",
                    "Potential stem cell regeneration effects",
                    "Profound autophagy throughout the body",
                    "Possible immune system 'reset' effects"
                ),
                scientificDetails = "After 25 hours, growth hormone secretion increases to preserve muscle mass and promote fat utilization. Studies show a 300-1000% increase in growth hormone levels. Extended fasting also activates stem cell production and may trigger immune system regeneration through a process called apoptosis of old immune cells.",
                warnings = listOf(
                    "Extended fasting should only be done with proper preparation and knowledge",
                    "Not recommended for those who are underweight or have eating disorders",
                    "May require electrolyte supplementation to prevent imbalances",
                    "Should be broken carefully with easily digestible foods",
                    "Consult healthcare provider before attempting fasts longer than 24 hours"
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
