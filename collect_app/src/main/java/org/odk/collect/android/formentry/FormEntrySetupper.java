package org.odk.collect.android.formentry;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.helpers.FormsDaoHelper;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.audit.IdentityPromptViewModel;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.tasks.SaveFormIndexTask;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import timber.log.Timber;

public class FormEntrySetupper {

    private final AppCompatActivity activity;
    private final FormLoaderTask task;
    private final FormEntryViewModel formEntryViewModel;
    private final FormController formController;
    private final String formPath;
    private final IdentityPromptViewModel identityPromptViewModel;
    private Boolean allowMovingBackwards = false;

    public FormEntrySetupper(AppCompatActivity activity, FormLoaderTask task, String formPath, IdentityPromptViewModel identityPromptViewModel, FormEntryViewModel formEntryViewModel) {
        this.activity = activity;
        this.task = task;
        this.formEntryViewModel = formEntryViewModel;
        this.formPath = formPath;
        this.identityPromptViewModel = identityPromptViewModel;
        this.formController = task.getFormController();
    }

    public void setup(Listener listener) {
        task.cancel(true);
        task.destroy();

        Collect.getInstance().setFormController(formController);
        Collect.getInstance().setExternalDataManager(task.getExternalDataManager());
        formEntryViewModel.formFinishedLoading();

        setFormLanguage();

        boolean pendingActivityResult = task.hasPendingActivityResult();
        if (pendingActivityResult) {
            listener.onPendingActivityResult(formController, task.getRequestCode(), task.getResultCode(), task.getIntent());
        } else {
            // it can be a normal flow for a pending activity result to restore from a savepoint
            // (the call flow handled by the above if statement). For all other use cases, the
            // user should be notified, as it means they wandered off doing other things then
            // returned to ODK Collect and chose Edit Saved Form, but that the savepoint for
            // that form is newer than the last saved version of their form data.
            boolean hasUsedSavepoint = task.hasUsedSavepoint();
            if (hasUsedSavepoint) {
                listener.onRecoveringSavepoint(formController);
            }

            if (formController.getInstanceFile() == null) {
                setupNewForm(listener);
            } else {
                setupResumingForm(listener);
            }
        }
    }

    public void setAllowMovingBackwards(Boolean allowMovingBackwards) {
        this.allowMovingBackwards = allowMovingBackwards;
    }

    private void setupResumingForm(Listener listener) {
        Intent reqIntent = activity.getIntent();
        boolean showFirst = reqIntent.getBooleanExtra("start", false);

        if (!showFirst) {
            if (!allowMovingBackwards) {
                FormIndex formIndex = SaveFormIndexTask.loadFormIndexFromFile();
                if (formIndex != null) {
                    formController.jumpToIndex(formIndex);
                    listener.onResumeFormEntryAtIndex(formController);
                    return;
                }
            }

            // we've just loaded a saved form, so start in the hierarchy view
            String formMode = reqIntent.getStringExtra(ApplicationConstants.BundleKeys.FORM_MODE);
            if (formMode == null || ApplicationConstants.FormModes.EDIT_SAVED.equalsIgnoreCase(formMode)) {
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_RESUME, true, System.currentTimeMillis());
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.HIERARCHY, true, System.currentTimeMillis());
                listener.onResumeFormEntryAtHierarchy(formController);
                return; // so we don't show the intro screen before jumping to the hierarchy
            } else {
                if (ApplicationConstants.FormModes.VIEW_SENT.equalsIgnoreCase(formMode)) {
                    listener.onViewSentForm(formController);
                }

                listener.onUnknownFormMode(formController);
            }
        } else {
            identityPromptViewModel.setAuditEventLogger(formController.getAuditEventLogger());
            identityPromptViewModel.requiresIdentity().observe(activity, requiresIdentity -> {
                if (!requiresIdentity) {
                    formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_RESUME, true, System.currentTimeMillis());
                    listener.onResumeFormEntry(formController);
                }
            });
        }
    }

    private void setupNewForm(Listener listener) {
        createInstanceDirectory(formController);
        identityPromptViewModel.setAuditEventLogger(formController.getAuditEventLogger());
        identityPromptViewModel.requiresIdentity().observe(activity, requiresIdentity -> {
            if (!requiresIdentity) {
                formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_START, true, System.currentTimeMillis());
                listener.onStartFormEntry(formController);
            }
        });
    }

    private void setFormLanguage() {
        // Set the language if one has already been set in the past
        String[] languageTest = formController.getLanguages();
        if (languageTest != null) {
            String defaultLanguage = formController.getLanguage();
            String newLanguage = FormsDaoHelper.getFormLanguage(formPath);

            long start = System.currentTimeMillis();
            Timber.i("calling formController.setLanguage");
            try {
                formController.setLanguage(newLanguage);
            } catch (Exception e) {
                // if somehow we end up with a bad language, set it to the default
                Timber.e("Ended up with a bad language. %s", newLanguage);
                formController.setLanguage(defaultLanguage);
            }
            Timber.i("Done in %.3f seconds.", (System.currentTimeMillis() - start) / 1000F);
        }
    }

    private void createInstanceDirectory(FormController formController) {
        String time = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss",
                Locale.ENGLISH).format(Calendar.getInstance().getTime());
        String file = formPath.substring(formPath.lastIndexOf('/') + 1,
                formPath.lastIndexOf('.'));
        String path = Collect.INSTANCES_PATH + File.separator + file + "_"
                + time;
        if (FileUtils.createFolder(path)) {
            File instanceFile = new File(path + File.separator + file + "_" + time + ".xml");
            formController.setInstanceFile(instanceFile);
        }
    }

    public interface Listener {
        void onPendingActivityResult(FormController formController, int requestCode, int resultCode, Intent intent);

        void onRecoveringSavepoint(FormController formController);

        void onStartFormEntry(FormController formController);

        void onResumeFormEntryAtIndex(FormController formController);

        void onResumeFormEntry(FormController formController);

        void onResumeFormEntryAtHierarchy(FormController formController);

        void onViewSentForm(FormController formController);

        void onUnknownFormMode(FormController formController);
    }
}
