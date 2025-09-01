package org.odk.collect.android.formhierarchy

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.strings.localization.LocalizedActivity

class FormEntryFragmentHostActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerUtils.getComponent(this).inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.form_entry_host_layout)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        navController.setGraph(R.navigation.form_entry)
        setSupportActionBar(findViewById(org.odk.collect.androidshared.R.id.toolbar))
    }
}
