package org.odk.collect.android.formentry.audit;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IdentityPromptViewModel extends ViewModel {

    private final MutableLiveData<Boolean> formEntryCancelled = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> requiresIdentity = new MutableLiveData<>(false);

    @Nullable
    private AuditEventLogger auditEventLogger;

    public IdentityPromptViewModel() {
        updateRequiresIdentity();
    }

    public LiveData<Boolean> requiresIdentity() {
        return requiresIdentity;
    }

    public LiveData<Boolean> isFormEntryCancelled() {
        return formEntryCancelled;
    }

    public void setAuditEventLogger(AuditEventLogger auditEventLogger) {
        this.auditEventLogger = auditEventLogger;
        updateRequiresIdentity();
    }

    public void setIdentity(String identity) {
        auditEventLogger.setUser(identity);
        updateRequiresIdentity();
    }

    public void promptClosing() {
        if (requiresIdentity.getValue()) {
            formEntryCancelled.setValue(true);
        }
    }

    private void updateRequiresIdentity() {
        this.requiresIdentity.setValue(
                auditEventLogger != null &&
                        auditEventLogger.isUserRequired() &&
                        (auditEventLogger.getUser() == null || auditEventLogger.getUser().isEmpty())
        );
    }
}
