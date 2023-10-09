package org.odk.collect.android.support.pages;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;

import org.odk.collect.android.R;
import org.odk.collect.android.support.StorageUtils;
import org.odk.collect.android.support.WaitFor;

import java.io.IOException;
import java.util.List;

public class MainMenuPage extends Page<MainMenuPage> {

    @Override
    public MainMenuPage assertOnPage() {
        return WaitFor.waitFor(() -> {
            onView(withText(org.odk.collect.strings.R.string.enter_data)).check(matches(isDisplayed()));
            onView(withText(containsString(getTranslatedString(org.odk.collect.strings.R.string.collect_app_name)))).perform(scrollTo()).check(matches(isDisplayed()));
            return this;
        });
    }

    public ProjectSettingsDialogPage openProjectSettingsDialog() {
        assertOnPage(); // Make sure we've waited for the application load correctly

        clickOnContentDescription(org.odk.collect.strings.R.string.projects);
        return new ProjectSettingsDialogPage().assertOnPage();
    }

    public FormEntryPage startBlankForm(String formName) {
        goToBlankForm(formName);
        return new FormEntryPage(formName).assertOnPage();
    }

    public AddNewRepeatDialog startBlankFormWithRepeatGroup(String formName, String repeatName) {
        goToBlankForm(formName);
        return new AddNewRepeatDialog(repeatName).assertOnPage();
    }

    public ErrorDialog startBlankFormWithError(String formName) {
        goToBlankForm(formName);
        return new ErrorDialog().assertOnPage();
    }

    public OkDialog startBlankFormWithDialog(String formName) {
        goToBlankForm(formName);
        return new OkDialog().assertOnPage();
    }

    public FillBlankFormPage clickFillBlankForm() {
        onView(withId(R.id.enter_data)).perform(click());
        return new FillBlankFormPage().assertOnPage();
    }

    private void goToBlankForm(String formName) {
        clickFillBlankForm().clickOnForm(formName);
    }

    public EditSavedFormPage clickDrafts() {
        onView(withId(R.id.review_data)).perform(click());
        return new EditSavedFormPage().assertOnPage();
    }

    public EditSavedFormPage clickDrafts(int formCount) {
        assertNumberOfEditableForms(formCount);
        return clickDrafts();
    }

    public MainMenuPage assertNumberOfFinalizedForms(int number) {
        if (number == 0) {
            onView(allOf(withId(R.id.number), isDescendantOfA(withId(R.id.send_data)))).check(matches(withText("")));
        } else {
            onView(allOf(withId(R.id.number), isDescendantOfA(withId(R.id.send_data)))).check(matches(withText(String.valueOf(number))));
        }
        return this;
    }

    public MainMenuPage assertNumberOfEditableForms(int number) {
        if (number == 0) {
            onView(allOf(withId(R.id.number), isDescendantOfA(withId(R.id.review_data)))).check(matches(withText("")));
        } else {
            onView(allOf(withId(R.id.number), isDescendantOfA(withId(R.id.review_data)))).check(matches(withText(String.valueOf(number))));
        }

        return this;
    }

    private MainMenuPage assertNumberOfSentForms(int number) {
        if (number == 0) {
            onView(allOf(withId(R.id.number), isDescendantOfA(withId(R.id.view_sent_forms)))).check(matches(withText("")));
        } else {
            onView(allOf(withId(R.id.number), isDescendantOfA(withId(R.id.view_sent_forms)))).check(matches(withText(String.valueOf(number))));
        }

        return this;
    }

    public GetBlankFormPage clickGetBlankForm() {
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.get_forms))).perform(scrollTo(), click());
        return new GetBlankFormPage().assertOnPage();
    }

    public SendFinalizedFormPage clickSendFinalizedForm(int number) {
        assertNumberOfFinalizedForms(number);
        onView(withId(R.id.send_data)).perform(click());
        return new SendFinalizedFormPage();
    }

    public MainMenuPage setServer(String url) {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickServerSettings()
                .clickOnURL()
                .inputText(url)
                .clickOKOnDialog()
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enableManualUpdates() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(org.odk.collect.strings.R.string.manual)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enablePreviouslyDownloadedOnlyUpdates() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(org.odk.collect.strings.R.string.previously_downloaded_only)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enablePreviouslyDownloadedOnlyUpdatesWithAutomaticDownload() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(org.odk.collect.strings.R.string.previously_downloaded_only)
                .clickOnString(org.odk.collect.strings.R.string.automatic_download)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enableMatchExactly() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickUpdateForms()
                .clickOption(org.odk.collect.strings.R.string.match_exactly)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage enableAutoSend() {
        return openProjectSettingsDialog()
                .clickSettings()
                .clickFormManagement()
                .clickOnString(org.odk.collect.strings.R.string.autosend)
                .clickOnString(org.odk.collect.strings.R.string.wifi_cellular_autosend)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage());
    }

    public MainMenuPage addAndSwitchToProject(String serverUrl) {
        return openProjectSettingsDialog()
                .clickAddProject()
                .switchToManualMode()
                .inputUrl(serverUrl)
                .addProject();
    }

    public ServerAuthDialog clickGetBlankFormWithAuthenticationError() {
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.get_forms))).perform(scrollTo(), click());
        return new ServerAuthDialog().assertOnPage();
    }

    public OkDialog clickGetBlankFormWithError() {
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.get_forms))).perform(scrollTo(), click());
        return new OkDialog().assertOnPage();
    }

    public ViewSentFormPage clickViewSentForm(int number) {
        assertNumberOfSentForms(number);
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.view_sent_forms))).perform(click());
        return new ViewSentFormPage().assertOnPage();
    }

    public DeleteSavedFormPage clickDeleteSavedForm() {
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.manage_files))).perform(scrollTo(), click());
        return new DeleteSavedFormPage().assertOnPage();
    }

    public MainMenuPage assertProjectIcon(String projectIcon) {
        onView(allOf(hasDescendant(withText(projectIcon)), withId(R.id.projects))).check(matches(isDisplayed()));
        return this;
    }

    public MainMenuPage copyForm(String formFilename) {
        return copyForm(formFilename, null, "Demo project");
    }

    public MainMenuPage copyForm(String formFilename, String projectName) {
        return copyForm(formFilename, null, projectName);
    }

    public MainMenuPage copyForm(String formFilename, List<String> mediaFilePaths) {
        return copyForm(formFilename, mediaFilePaths, "Demo project");
    }

    public MainMenuPage copyForm(String formFilename, List<String> mediaFilePaths, String projectName) {
        return copyForm(formFilename, mediaFilePaths, false, projectName);
    }

    public MainMenuPage copyForm(String formFilename, List<String> mediaFilePaths, boolean copyToDatabase, String projectName) {
        try {
            StorageUtils.copyFormToStorage(formFilename, mediaFilePaths, copyToDatabase, formFilename, projectName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public MainMenuPage copyInstance(String instanceFileName) {
        copyInstance(instanceFileName, "Demo project");
        return this;
    }

    public MainMenuPage copyInstance(String instanceFileName, String projectName) {
        try {
            StorageUtils.copyInstance(instanceFileName, projectName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public EntitiesPage openEntityBrowser() {
        openProjectSettingsDialog()
                .clickSettings()
                .clickExperimental()
                .clickOnString(org.odk.collect.strings.R.string.entities_title);

        return new EntitiesPage().assertOnPage();
    }
}

