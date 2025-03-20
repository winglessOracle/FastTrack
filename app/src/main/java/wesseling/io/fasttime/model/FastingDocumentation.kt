package wesseling.io.fasttime.model

import android.content.Context
import wesseling.io.fasttime.R

/**
 * Documentation about fasting states and their scientific benefits
 */
object FastingDocumentation {
    
    /**
     * Get detailed documentation for a specific fasting state
     */
    fun getDocumentationForState(state: FastingState, context: Context): FastingStateInfo {
        return when (state) {
            FastingState.NOT_FASTING -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_not_fasting,
                benefitResourceIds = listOf(
                    R.string.fasting_state_benefit_not_fasting_1,
                    R.string.fasting_state_benefit_not_fasting_2,
                    R.string.fasting_state_benefit_not_fasting_3,
                    R.string.fasting_state_benefit_not_fasting_4
                ),
                scientificDetailsResourceId = R.string.fasting_state_details_not_fasting,
                warningResourceIds = listOf(
                    R.string.fasting_state_warning_not_fasting_1,
                    R.string.fasting_state_warning_not_fasting_2,
                    R.string.fasting_state_warning_not_fasting_3
                ),
                context = context
            )
            
            FastingState.EARLY_FAST -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_early_fast,
                benefitResourceIds = listOf(
                    R.string.fasting_state_benefit_early_fast_1,
                    R.string.fasting_state_benefit_early_fast_2,
                    R.string.fasting_state_benefit_early_fast_3,
                    R.string.fasting_state_benefit_early_fast_4
                ),
                scientificDetailsResourceId = R.string.fasting_state_details_early_fast,
                warningResourceIds = listOf(
                    R.string.fasting_state_warning_early_fast_1,
                    R.string.fasting_state_warning_early_fast_2,
                    R.string.fasting_state_warning_early_fast_3
                ),
                context = context
            )
            
            FastingState.GLYCOGEN_DEPLETION -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_glycogen_depletion,
                benefitResourceIds = listOf(
                    R.string.fasting_state_benefit_glycogen_depletion_1,
                    R.string.fasting_state_benefit_glycogen_depletion_2,
                    R.string.fasting_state_benefit_glycogen_depletion_3,
                    R.string.fasting_state_benefit_glycogen_depletion_4,
                    R.string.fasting_state_benefit_glycogen_depletion_5
                ),
                scientificDetailsResourceId = R.string.fasting_state_details_glycogen_depletion,
                warningResourceIds = listOf(
                    R.string.fasting_state_warning_glycogen_depletion_1,
                    R.string.fasting_state_warning_glycogen_depletion_2,
                    R.string.fasting_state_warning_glycogen_depletion_3
                ),
                context = context
            )
            
            FastingState.METABOLIC_SHIFT -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_metabolic_shift,
                benefitResourceIds = listOf(
                    R.string.fasting_state_benefit_metabolic_shift_1,
                    R.string.fasting_state_benefit_metabolic_shift_2,
                    R.string.fasting_state_benefit_metabolic_shift_3,
                    R.string.fasting_state_benefit_metabolic_shift_4,
                    R.string.fasting_state_benefit_metabolic_shift_5
                ),
                scientificDetailsResourceId = R.string.fasting_state_details_metabolic_shift,
                warningResourceIds = listOf(
                    R.string.fasting_state_warning_metabolic_shift_1,
                    R.string.fasting_state_warning_metabolic_shift_2,
                    R.string.fasting_state_warning_metabolic_shift_3
                ),
                context = context
            )
            
            FastingState.DEEP_KETOSIS -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_deep_ketosis,
                benefitResourceIds = listOf(
                    R.string.fasting_state_benefit_deep_ketosis_1,
                    R.string.fasting_state_benefit_deep_ketosis_2,
                    R.string.fasting_state_benefit_deep_ketosis_3,
                    R.string.fasting_state_benefit_deep_ketosis_4,
                    R.string.fasting_state_benefit_deep_ketosis_5
                ),
                scientificDetailsResourceId = R.string.fasting_state_details_deep_ketosis,
                warningResourceIds = listOf(
                    R.string.fasting_state_warning_deep_ketosis_1,
                    R.string.fasting_state_warning_deep_ketosis_2,
                    R.string.fasting_state_warning_deep_ketosis_3,
                    R.string.fasting_state_warning_deep_ketosis_4
                ),
                context = context
            )
            
            FastingState.IMMUNE_RESET -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_immune_reset,
                benefitResourceIds = listOf(
                    R.string.fasting_state_benefit_immune_reset_1,
                    R.string.fasting_state_benefit_immune_reset_2,
                    R.string.fasting_state_benefit_immune_reset_3,
                    R.string.fasting_state_benefit_immune_reset_4,
                    R.string.fasting_state_benefit_immune_reset_5
                ),
                scientificDetailsResourceId = R.string.fasting_state_details_immune_reset,
                warningResourceIds = listOf(
                    R.string.fasting_state_warning_immune_reset_1,
                    R.string.fasting_state_warning_immune_reset_2,
                    R.string.fasting_state_warning_immune_reset_3,
                    R.string.fasting_state_warning_immune_reset_4,
                    R.string.fasting_state_warning_immune_reset_5
                ),
                context = context
            )
            
            FastingState.EXTENDED_FAST -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_extended_fast,
                benefitResourceIds = listOf(
                    R.string.fasting_state_benefit_extended_fast_1,
                    R.string.fasting_state_benefit_extended_fast_2,
                    R.string.fasting_state_benefit_extended_fast_3,
                    R.string.fasting_state_benefit_extended_fast_4,
                    R.string.fasting_state_benefit_extended_fast_5
                ),
                scientificDetailsResourceId = R.string.fasting_state_details_extended_fast,
                warningResourceIds = listOf(
                    R.string.fasting_state_warning_extended_fast_1,
                    R.string.fasting_state_warning_extended_fast_2,
                    R.string.fasting_state_warning_extended_fast_3,
                    R.string.fasting_state_warning_extended_fast_4,
                    R.string.fasting_state_warning_extended_fast_5,
                    R.string.fasting_state_warning_extended_fast_6
                ),
                context = context
            )
        }
    }
    
    /**
     * Get detailed documentation for a specific fasting state
     * Legacy method for backward compatibility
     */
    fun getDocumentationForState(state: FastingState): FastingStateInfo {
        return when (state) {
            FastingState.NOT_FASTING -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_not_fasting,
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
                titleResourceId = R.string.fasting_state_title_early_fast,
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
                titleResourceId = R.string.fasting_state_title_glycogen_depletion,
                benefits = listOf(
                    "Liver glycogen stores become significantly depleted",
                    "Fat breakdown (lipolysis) increases substantially",
                    "Blood ketones begin to rise slightly",
                    "Autophagy (cellular cleanup) starts increasing",
                    "Improved mental clarity for many people"
                ),
                scientificDetails = "Between 12-18 hours of fasting, your liver glycogen stores become depleted, and your body increases fat oxidation. The liver begins converting fatty acids into ketone bodies (acetoacetate, beta-hydroxybutyrate, and acetone), which serve as an alternative fuel source for the brain and other organs. This marks the beginning of metabolic flexibility. During this phase, initial autophagy processes begin, where your cells start to remove damaged components. This cellular cleanup process will intensify in later fasting stages.",
                warnings = listOf(
                    "Initial adaptation to fat metabolism may cause temporary fatigue",
                    "People with certain medical conditions should consult healthcare providers",
                    "Proper hydration becomes increasingly important during this phase"
                )
            )
            
            FastingState.METABOLIC_SHIFT -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_metabolic_shift,
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
                titleResourceId = R.string.fasting_state_title_deep_ketosis,
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
                titleResourceId = R.string.fasting_state_title_immune_reset,
                benefits = listOf(
                    "Stem cell production increases (immune system regeneration begins)",
                    "Insulin sensitivity improves dramatically",
                    "The gut lining starts regenerating",
                    "Mental clarity and focus enhanced due to high ketone levels",
                    "Significant anti-inflammatory effects throughout the body"
                ),
                scientificDetails = "Between 48-72 hours, a process called 'autophagy-dependent immune system reconfiguration' begins. Studies show that prolonged fasting reduces circulating IGF-1 levels and PKA activity, promoting stem cell-based regeneration of immune cells. This doesn't completely reset your immunity, but rather enhances specific aspects of immune function through cellular regeneration and reduced inflammation. The gut microbiome undergoes significant changes, and the intestinal lining begins to regenerate. Fat oxidation remains high while protein breakdown is minimized through metabolic adaptations.",
                warnings = listOf(
                    "Extended fasting at this duration requires careful preparation",
                    "Electrolyte supplementation becomes essential",
                    "Breaking the fast properly is crucial to avoid digestive distress",
                    "Not recommended for those with certain medical conditions",
                    "Should be done under supervision for those new to extended fasting"
                )
            )
            
            FastingState.EXTENDED_FAST -> FastingStateInfo(
                titleResourceId = R.string.fasting_state_title_extended_fast,
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
        val titleResourceId: Int,  // Resource ID for localized title
        
        // Legacy fields for backward compatibility
        val benefits: List<String> = emptyList(),
        val scientificDetails: String = "",
        val warnings: List<String> = emptyList()
    ) {
        // New fields for localized content
        private var _benefitResourceIds: List<Int>? = null
        private var _scientificDetailsResourceId: Int? = null
        private var _warningResourceIds: List<Int>? = null
        
        // Context for resource resolution
        private var _context: Context? = null
        
        // Secondary constructor for localized content
        constructor(
            titleResourceId: Int,
            benefitResourceIds: List<Int>,
            scientificDetailsResourceId: Int,
            warningResourceIds: List<Int>,
            context: Context
        ) : this(titleResourceId) {
            _benefitResourceIds = benefitResourceIds
            _scientificDetailsResourceId = scientificDetailsResourceId
            _warningResourceIds = warningResourceIds
            _context = context
        }
        
        // Getter for localized benefits
        fun getLocalizedBenefits(): List<String> {
            if (_context != null && _benefitResourceIds != null) {
                return _benefitResourceIds!!.map { _context!!.getString(it) }
            }
            return benefits
        }
        
        // Getter for localized scientific details
        fun getLocalizedScientificDetails(): String {
            if (_context != null && _scientificDetailsResourceId != null) {
                return _context!!.getString(_scientificDetailsResourceId!!)
            }
            return scientificDetails
        }
        
        // Getter for localized warnings
        fun getLocalizedWarnings(): List<String> {
            if (_context != null && _warningResourceIds != null) {
                return _warningResourceIds!!.map { _context!!.getString(it) }
            }
            return warnings
        }
        
        // For backward compatibility - using an empty string as this field should no longer be used
        val title: String = ""
        
        // For backward compatibility
        val titleKey: String = ""
    }
}
