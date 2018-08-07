package com.criptext.mail.scenes.settings.views

import android.content.res.Resources
import android.view.View
import android.widget.TextView
import com.criptext.mail.BuildConfig
import com.criptext.mail.R
import com.criptext.mail.scenes.settings.SettingsUIObserver
import com.criptext.mail.utils.ui.TabView
import kotlinx.android.synthetic.main.deckard.view.*

class GeneralSettingsView(view: View, title: String): TabView(view, title) {

    private lateinit var settingsProfileName: View
    private lateinit var settingsSignature: View
    private lateinit var settingsPrivacyPolicies: View
    private lateinit var settingsTermsOfService: View
    private lateinit var settingsOpenSourceLibraries: View
    private lateinit var settingsLogout: View
    private lateinit var versionText: TextView

    private var settingsUIObserver: SettingsUIObserver? = null

    private val versionString ="Criptext System Version ${BuildConfig.VERSION_NAME}"

    override fun onCreateView(){

        settingsProfileName = view.findViewById(R.id.settings_profile_name)
        settingsSignature = view.findViewById(R.id.settings_signature)
        settingsPrivacyPolicies = view.findViewById(R.id.settings_privacy_policy)
        settingsTermsOfService = view.findViewById(R.id.settings_terms_of_service)
        settingsOpenSourceLibraries = view.findViewById(R.id.settings_open_source_libraries)
        settingsLogout = view.findViewById(R.id.settings_logout)
        versionText = view.findViewById(R.id.version_text) as TextView
        versionText.text = BuildConfig.VERSION_NAME

        setButtonListeners()
    }

    fun setExternalListeners(settingsUIObserver: SettingsUIObserver?){
        this.settingsUIObserver = settingsUIObserver
    }

    private fun setButtonListeners(){
        settingsProfileName.setOnClickListener {
            settingsUIObserver?.onProfileNameClicked()
        }
        settingsSignature.setOnClickListener {
            settingsUIObserver?.onSignatureOptionClicked()
        }
        settingsPrivacyPolicies.setOnClickListener {
            settingsUIObserver?.onPrivacyPoliciesClicked()
        }
        settingsTermsOfService.setOnClickListener {
            settingsUIObserver?.onTermsOfServiceClicked()
        }
        settingsOpenSourceLibraries.setOnClickListener {
            settingsUIObserver?.onOpenSourceLibrariesClicked()
        }
        settingsLogout.setOnClickListener {
            settingsUIObserver?.onLogoutClicked()
        }
    }

}