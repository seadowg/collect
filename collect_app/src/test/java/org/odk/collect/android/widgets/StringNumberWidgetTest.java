package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralStringWidgetTest;

import java.util.Random;

/**
 * @author James Knight
 */
public class StringNumberWidgetTest
        extends GeneralStringWidgetTest<StringNumberWidget, StringData> {

    @NonNull
    @Override
    public StringNumberWidget createWidget() {
        Random random = new Random();
        boolean useThousandSeparator = random.nextBoolean();
        return new StringNumberWidget(activity, new QuestionDetails(formEntryPrompt), false, useThousandSeparator);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }
}
