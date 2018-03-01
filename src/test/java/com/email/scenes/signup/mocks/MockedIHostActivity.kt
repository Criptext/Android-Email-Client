package com.email.scenes.signup.mocks

import com.email.IHostActivity
import com.email.scenes.params.SceneParams
import com.email.utils.UIMessage

/**
 * Created by sebas on 2/27/18.
 */

class MockedIHostActivity: IHostActivity {
    override fun getLocalizedString(message: UIMessage): String {
        return ""
    }

    override fun refreshToolbarItems() {
    }

    override fun goToScene(params: SceneParams) {
    }

    override fun finishScene() {
    }
}