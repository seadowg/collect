package org.odk.collect.android.feature.settings;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.FormLoadingUtils;
import org.odk.collect.android.support.NotificationDrawerRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.TestScheduler;
import org.odk.collect.android.support.pages.AdminSettingsPage;
import org.odk.collect.android.support.pages.FormManagementPage;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class FormManagementSettingsTest {

    private final TestDependencies testDependencies = new TestDependencies();
    private final NotificationDrawerRule notificationDrawer = new NotificationDrawerRule();

    public IntentsTestRule<MainMenuActivity> rule = new IntentsTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.GET_ACCOUNTS))
            .around(notificationDrawer)
            .around(rule);

    @Test
    public void whenMatchExactlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        List<TestScheduler.DeferredTask> deferredTasks = testDependencies.scheduler.getDeferredTasks();
        assertThat(deferredTasks, is(empty()));

        FormManagementPage page = new MainMenuPage(rule).assertOnPage()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.match_exactly);

        deferredTasks = testDependencies.scheduler.getDeferredTasks();
        assertThat(deferredTasks.size(), is(1));
        String matchExactlyTag = deferredTasks.get(0).getTag();

        page.clickAutomaticUpdateFrequency()
                .clickOption(R.string.every_one_hour);

        deferredTasks = testDependencies.scheduler.getDeferredTasks();
        assertThat(deferredTasks.size(), is(1));
        assertThat(deferredTasks.get(0).getTag(), is(matchExactlyTag));
        assertThat(deferredTasks.get(0).getRepeatPeriod(), is(1000L * 60 * 60));
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_changingAutomaticUpdateFrequency_changesTaskFrequency() {
        List<TestScheduler.DeferredTask> deferredTasks = testDependencies.scheduler.getDeferredTasks();
        assertThat(deferredTasks, is(empty()));

        FormManagementPage page = new MainMenuPage(rule).assertOnPage()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.previously_downloaded_only);

        deferredTasks = testDependencies.scheduler.getDeferredTasks();
        assertThat(deferredTasks.size(), is(1));
        String previouslyDownloadedTag = deferredTasks.get(0).getTag();

        page.clickAutomaticUpdateFrequency()
                .clickOption(R.string.every_one_hour);

        deferredTasks = testDependencies.scheduler.getDeferredTasks();
        assertThat(deferredTasks.size(), is(1));
        assertThat(deferredTasks.get(0).getTag(), is(previouslyDownloadedTag));
        assertThat(deferredTasks.get(0).getRepeatPeriod(), is(1000L * 60 * 60));
    }

    @Test
    public void whenPreviouslyDownloadedOnlyEnabled_checkingAutoDownload_downloadsUpdatedForms() throws Exception {
        FormManagementPage page = new MainMenuPage(rule).assertOnPage()
                .setServer(testDependencies.server.getURL())
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(R.string.previously_downloaded_only)
                .clickOnString(R.string.automatic_download);

        FormLoadingUtils.copyFormToStorage("one-question.xml");
        testDependencies.server.addForm("One Question Updated", "one_question", "one-question-updated.xml");
        testDependencies.scheduler.runDeferredTasks();

        page.pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickFillBlankForm()
                .assertText("One Question Updated");

        notificationDrawer.open()
                .assertAndDismissNotification("ODK Collect", "ODK auto-download results", "Success");
    }

    @Test
    public void whenFormUpdatesPrefsDisabledInAdminSettings_disablesPrefs() {
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickAdminSettings()
                .openUserSettings()
                .uncheckUserSettings(R.string.form_update_mode_title)
                .uncheckUserSettings(R.string.periodic_form_updates_check_title)
                .uncheckUserSettings(R.string.automatic_download)
                .uncheckUserSettings(R.string.hide_old_form_versions_setting_title)
                .pressBack(new AdminSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickOnMenu()
                .clickGeneralSettings()
                .clickFormManagement()
                .assertTextDoesNotExist(R.string.form_update_mode_title)
                .assertTextDoesNotExist(R.string.periodic_form_updates_check_title)
                .assertTextDoesNotExist(R.string.automatic_download)
                .assertTextDoesNotExist(R.string.hide_old_form_versions_setting_title);
    }
}
