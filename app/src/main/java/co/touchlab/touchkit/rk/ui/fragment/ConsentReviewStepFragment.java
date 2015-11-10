package co.touchlab.touchkit.rk.ui.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import co.touchlab.touchkit.rk.R;
import co.touchlab.touchkit.rk.common.answerformat.TextAnswerFormat;
import co.touchlab.touchkit.rk.common.model.ConsentSignature;
import co.touchlab.touchkit.rk.common.result.ConsentSignatureResult;
import co.touchlab.touchkit.rk.common.result.StepResult;
import co.touchlab.touchkit.rk.common.result.TextQuestionResult;
import co.touchlab.touchkit.rk.common.step.ConsentReviewStep;
import co.touchlab.touchkit.rk.common.step.FormStep;
import co.touchlab.touchkit.rk.dev.DevUtils;
import co.touchlab.touchkit.rk.ui.callbacks.ConsentReviewCallback;
import co.touchlab.touchkit.rk.ui.scene.ConsentReviewDocumentScene;
import co.touchlab.touchkit.rk.ui.scene.ConsentReviewSignatureScene;
import co.touchlab.touchkit.rk.ui.scene.FormScene;
import co.touchlab.touchkit.rk.ui.scene.Scene;

public class ConsentReviewStepFragment extends MultiSceneStepFragment implements ConsentReviewCallback
{
    public static final String TAG = ConsentReviewStepFragment.class.getSimpleName();

    public static final int SECTION_REVIEW_DOCUMENT = 0;
    public static final int SECTION_REVIEW_NAME = 1;
    public static final int SECTION_REVIEW_SIGNATURE = 2;

    private static final String NameFormIdentifier = "nameForm";
    private static final String GivenNameIdentifier = "given";
    private static final String FamilyNameIdentifier = "family";

    public List<Integer> sections;

    public ConsentReviewStepFragment()
    {
        super();
    }

    public static Fragment newInstance(ConsentReviewStep step)
    {
        ConsentReviewStepFragment fragment = new ConsentReviewStepFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_QUESTION_STEP, step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ConsentReviewStep step = (ConsentReviewStep) getStep();

        sections = new ArrayList<>();

        if (step.getDocument() != null) {
            sections.add(SECTION_REVIEW_DOCUMENT);
        }

        if (step.getSignature().isRequiresName()) {
            sections.add(SECTION_REVIEW_NAME);
        }

        if (step.getSignature().isRequiresSignatureImage()) {
            sections.add(SECTION_REVIEW_SIGNATURE);
        }
    }

    @Override
    public Scene onCreateScene(LayoutInflater inflater, int scenePos)
    {
        ConsentReviewStep step = (ConsentReviewStep) getStep();

        int section = sections.get(scenePos);

        if (section == SECTION_REVIEW_DOCUMENT)
        {
            ConsentReviewDocumentScene layout = new ConsentReviewDocumentScene(getContext());
            layout.setCallback(this);
            return layout;
        }
        else if (section == SECTION_REVIEW_NAME)
        {

            FormStep formStep = new FormStep(NameFormIdentifier,
                                             getString(R.string.consent_name_title),
                                             step.getText());
            formStep.setUseSurveyMode(false);
            formStep.setOptional(false);

            TextAnswerFormat format = new TextAnswerFormat();
            format.setIsMultipleLines(false);
//            TODO TODO set the following
//            nameAnswerFormat.autocapitalizationType = UITextAutocapitalizationTypeWords;
//            nameAnswerFormat.autocorrectionType = UITextAutocorrectionTypeNo;
//            nameAnswerFormat.spellCheckingType = UITextSpellCheckingTypeNo;

            List<FormScene.FormItem> items = new ArrayList<>();
            String placeholder = getResources().getString(R.string.consent_name_placeholder);

            String givenText = getResources().getString(R.string.consent_name_first);
            FormScene.FormItem givenName = new FormScene.FormItem(
                    GivenNameIdentifier, givenText, format, placeholder);
            items.add(givenName);

            String familyText = getResources().getString(R.string.consent_name_last);
            FormScene.FormItem familyName = new FormScene.FormItem(
                    FamilyNameIdentifier, familyText, format, placeholder);
            items.add(familyName);

            formStep.setFormItems(items);

            if (getResources().getBoolean(R.bool.lang_display_last_name_first)) {
                Collections.reverse(items);
            }

            return new FormScene(getContext(), formStep);
        }
        else if (section == SECTION_REVIEW_SIGNATURE)
        {
            ConsentReviewSignatureScene layout = new ConsentReviewSignatureScene(getContext());
            layout.setTitle(R.string.consent_signature_title);
            layout.setSummary(R.string.consent_signature_instruction);
            layout.setSkip(false, 0, null);
            return layout;
        }
        else
        {
            DevUtils.throwUnsupportedOpException();
            return null;
        }
    }

    @Override
    public int getSceneCount()
    {
        return sections.size();
    }

    @Override
    public StepResult createNewStepResult(String stepIdentifier)
    {
        StepResult<ConsentSignatureResult> parentResult = new StepResult<>(getStep().getIdentifier());

        ConsentSignatureResult result = new ConsentSignatureResult(step.getIdentifier());
        result.setStartDate(new Date());

        ConsentSignature clone = ((ConsentReviewStep) getStep()).getSignature();
        result.setSignature(clone);

        parentResult.getResults()
                .put(result.getIdentifier(), result);

        return parentResult;
    }

    @Override
    public void scenePoppedOffViewStack(Scene scene)
    {
        Log.i(TAG, "scenePoppedOff " + scene.getClass().getSimpleName());

        StepResult parentResult = getStepResult();
        ConsentSignatureResult result = (ConsentSignatureResult) parentResult
                .getResultForIdentifier(getStep().getIdentifier());

        ConsentSignature signature = result.getSignature();

//        TODO handle signature date-format string
//        if (signature.getSignatureDateFormatString().length() > 0) {
//            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//            [dateFormatter setDateFormat:_currentSignature.signatureDateFormatString];
//            _currentSignature.signatureDate = [dateFormatter stringFromDate:[NSDate date]];
//        } else {
//            _currentSignature.signatureDate = ORKSignatureStringFromDate([NSDate date]);
//        }

        if (scene instanceof ConsentReviewDocumentScene)
        {
            result.setConsented(true);
        }
        else if (scene instanceof FormScene)
        {
            StepResult formResult = scene.getResult();

            TextQuestionResult firstNameResult = (TextQuestionResult) formResult
                    .getResultForIdentifier(GivenNameIdentifier);
            signature.setGivenName(firstNameResult.getTextAnswer());

            TextQuestionResult lastNameResult = (TextQuestionResult) formResult
                    .getResultForIdentifier(FamilyNameIdentifier);
            signature.setFamilyName(lastNameResult.getTextAnswer());
        }
        else if (scene instanceof ConsentReviewSignatureScene)
        {

            ConsentReviewSignatureScene sigScene = (ConsentReviewSignatureScene) scene;

            //TODO The follow is less than ideal
            Bitmap bitmap = sigScene.getSignatureImage();
            if (bitmap != null)
            {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                signature.setSignatureImage(byteArray);
            }

            // If we get here, this means our last scene scene will trigger the step to finish. Its
            // a good idea to set the end date now.
            result.setEndDate(new Date());
        }
        else
        {
            String message = scene.getClass().getSimpleName() + " not supported";
            DevUtils.throwUnsupportedOpException(message);
        }

        callbacks.onStepResultChanged(getStep(), result);
    }

    @Override
    public void showConfirmationDialog()
    {
        ConsentReviewStep step = (ConsentReviewStep) getStep();

        new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog)
                .setTitle(R.string.consent_review_alert_title)
                .setMessage(step.getReasonForConsent()).setCancelable(false)
                .setPositiveButton(R.string.agree, (dialog, which) -> {
                    showScene(SECTION_REVIEW_NAME, true);
                }).setNegativeButton(R.string.cancel, null)
                .show();
    }


    @Override
    public void closeToWelcomeFlow()
    {
        //TODO Clear activity stack up until OnboardingActivity is visible
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }


        /*

     - (ORKFormStepViewController *)makeNameFormViewController {
        ORKFormStep *formStep = [[ORKFormStep alloc] initWithIdentifier:_NameFormIdentifier
                                                                title:self.step.title ? : ORKLocalizedString(@"CONSENT_NAME_TITLE", nil)
                                                                 text:self.step.text];
        formStep.useSurveyMode = NO;

        ORKTextAnswerFormat *nameAnswerFormat = [ORKTextAnswerFormat textAnswerFormat];
        nameAnswerFormat.multipleLines = NO;
        nameAnswerFormat.autocapitalizationType = UITextAutocapitalizationTypeWords;
        nameAnswerFormat.autocorrectionType = UITextAutocorrectionTypeNo;
        nameAnswerFormat.spellCheckingType = UITextSpellCheckingTypeNo;
        ORKFormItem *givenName = [[ORKFormItem alloc] initWithIdentifier:_GivenNameIdentifier
                                                                  text:ORKLocalizedString(@"CONSENT_NAME_FIRST", nil)
                                                          answerFormat:nameAnswerFormat];
        givenName.placeholder = ORKLocalizedString(@"CONSENT_NAME_PLACEHOLDER", nil);

        ORKFormItem *familyName = [[ORKFormItem alloc] initWithIdentifier:_FamilyNameIdentifier
                                                                 text:ORKLocalizedString(@"CONSENT_NAME_LAST", nil)
                                                         answerFormat:nameAnswerFormat];
        familyName.placeholder = ORKLocalizedString(@"CONSENT_NAME_PLACEHOLDER", nil);

        NSArray *formItems = @[givenName, familyName];
        if ([self currentLocalePresentsFamilyNameFirst]) {
            formItems = @[familyName, givenName];
        }

        [formStep setFormItems:formItems];

        formStep.optional = NO;

        ORKTextQuestionResult *givenNameDefault = [[ORKTextQuestionResult alloc] initWithIdentifier:_GivenNameIdentifier];
        givenNameDefault.textAnswer = _signatureFirst;
        ORKTextQuestionResult *familyNameDefault = [[ORKTextQuestionResult alloc] initWithIdentifier:_FamilyNameIdentifier];
        familyNameDefault.textAnswer = _signatureLast;
        ORKStepResult *defaults = [[ORKStepResult alloc] initWithStepIdentifier:_NameFormIdentifier results:@[givenNameDefault, familyNameDefault]];

        ORKFormStepViewController *viewController = [[ORKFormStepViewController alloc] initWithStep:formStep result:defaults];
        viewController.delegate = self;

        return viewController;
    }

    - (ORKConsentReviewController *)makeDocumentReviewViewController {
        ORKConsentSignature *originalSignature = [self.consentReviewStep signature];
        ORKConsentDocument *origninalDocument = self.consentReviewStep.consentDocument;

        NSUInteger index = [origninalDocument.signatures indexOfObject:originalSignature];

        // Deep copy
        ORKConsentDocument *document = [origninalDocument copy];

        if (index != NSNotFound) {
            ORKConsentSignature *signature = document.signatures[index];

            if (signature.requiresName) {
                signature.givenName = _signatureFirst;
                signature.familyName = _signatureLast;
            }
        }

        NSString *html = [document mobileHTMLWithTitle:ORKLocalizedString(@"CONSENT_REVIEW_TITLE", nil)
                                                 detail:ORKLocalizedString(@"CONSENT_REVIEW_INSTRUCTION", nil)];

        ORKConsentReviewController *reviewViewController = [[ORKConsentReviewController alloc] initWithHTML:html delegate:self];
        reviewViewController.localizedReasonForConsent = [[self consentReviewStep] reasonForConsent];
        return reviewViewController;
    }

    - (ORKConsentSignatureController *)makeSignatureViewController {
        ORKConsentSignatureController *signatureController = [[ORKConsentSignatureController alloc] init];
        signatureController.delegate = self;
        return signatureController;
    }

     */
}