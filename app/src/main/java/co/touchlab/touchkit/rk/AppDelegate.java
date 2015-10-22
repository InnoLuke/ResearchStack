package co.touchlab.touchkit.rk;
public class AppDelegate
{

    public static AppDelegate instance;

    //TODO Thread safe
    public static AppDelegate getInstance()
    {
        if(instance == null)
        {
            instance = new AppDelegate();
        }

        return instance;
    }

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // File Names
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public int getStudyOverviewResourceId()
    {
        return R.raw.study_overview;
    }

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Resource Names
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    public int getLargeLogoDiseaseIcon()
    {
        return R.drawable.logo_disease_large;
    }

    public int getConsentForm()
    {
        return R.raw.study_overview_consent_form;
    }

    public String getExternalSDAppFolder()
    {
        return "demo_touchkit";
    }

    public int getAppName()
    {
        return R.string.app_name;
    }
}