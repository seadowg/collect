package org.odk.collect.android.feature.formmanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.TestRuleChain;

@RunWith(AndroidJUnit4.class)
public class SearchBlankFormsTest {

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(new CopyFormRule("one-question.xml"))
            .around(new CopyFormRule("one-question-repeat.xml"))
            .around(rule);

    @Test //Issue NODK-244 TestCase12
    public void pressingSearch_andSearching_showsMatchingForms() {
        rule.mainMenu()
                .clickFillBlankForm()
                .assertText("One Question")
                .assertText("One Question Repeat")
                .clickMenuFilter()
                .searchInBar("repeat")
                .assertTextDoesNotExist("One Question")
                .assertText("One Question Repeat");
    }
}
