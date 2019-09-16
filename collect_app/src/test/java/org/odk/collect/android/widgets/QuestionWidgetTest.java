package org.odk.collect.android.widgets;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.audio.AudioButton;
import org.odk.collect.android.audio.AudioHelper;
import org.odk.collect.android.formentry.media.AudioHelperFactory;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.android.support.TestScreenContextActivity;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.Helpers.buildMockForm;
import static org.odk.collect.android.support.Helpers.setupMockReference;

@RunWith(RobolectricTestRunner.class)
public class QuestionWidgetTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Mock
    public ReferenceManager referenceManager;

    @Mock
    public AudioHelper audioHelper;

    @Before
    public void setup() {
        overrideDependencyModule();
        when(audioHelper.setAudio(any(AudioButton.class), any(), any())).thenReturn(new MutableLiveData<>());
    }

    @Test
    public void whenQuestionHasAudio_audioButtonUsesIndexAsClipID() throws Exception {
        FormEntryPrompt formEntryPrompt = buildMockForm();
        when(formEntryPrompt.getIndex().toString()).thenReturn("i am index");

        when(formEntryPrompt.getAudioText()).thenReturn("file://blah.mp3");
        setupMockReference("file://blah.mp3", referenceManager);

        TestScreenContextActivity activity = RobolectricHelpers.createThemedActivity(TestScreenContextActivity.class);
        new TestWidget(activity, new QuestionDetails(formEntryPrompt));

        verify(audioHelper).setAudio(any(AudioButton.class), eq("file://blah.mp3"), eq("i am index"));
    }

    private void overrideDependencyModule() {
        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {

            @Override
            public ReferenceManager providesReferenceManager() {
                return referenceManager;
            }

            @Override
            public AudioHelperFactory providesAudioHelperFactory() {
                return context -> audioHelper;
            }
        });
    }

    private static class TestWidget extends QuestionWidget {

        TestWidget(Context context, QuestionDetails questionDetails) {
            super(context, questionDetails);
        }

        @Override
        public IAnswerData getAnswer() {
            return null;
        }

        @Override
        public void clearAnswer() {

        }

        @Override
        public void setOnLongClickListener(OnLongClickListener l) {

        }
    }
}
