package org.odk.collect.android.feature.formentry

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.lessThan
import org.javarosa.measure.Measure
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain
import java.util.Arrays.asList

class RepeatXPathQueryTest {

    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun doesNotNeedToReCalculateRepeatedXPathPredicateBetweenDifferentValuesAndSessions() {
        rule.startAtMainMenu()
            .copyForm("select-from-file-multi-calc.xml", listOf("external.csv"))
            .let {
                val eqEvals = Measure.withMeasure(asList("PredicateEvaluation", "IndexEvaluation")) {
                    it.startBlankForm("select-from-file-50k")
                        .assertQuestion("Select a thing")
                        .openSelectMinimalDialog()
                        .clickOnText("My cool label 1")
                        .swipeToNextQuestion("col1 1col2 1col3 1col4 1col5 1col6 1col7 1col8 1col9 1col10 1")
                        .pressBackAndDiscardForm()
                        .startBlankForm("select-from-file-50k")
                        .assertQuestion("Select a thing")
                        .openSelectMinimalDialog()
                        .clickOnText("My cool label 1")
                        .swipeToNextQuestion("col1 1col2 1col3 1col4 1col5 1col6 1col7 1col8 1col9 1col10 1")
                }

                assertThat(eqEvals, lessThan(2 * 2)) // Less than 2 * number of items in external.csv evaluations
            }
    }
}
