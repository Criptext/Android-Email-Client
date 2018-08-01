package com.criptext.mail.scenes.mailbox

import com.criptext.mail.db.models.FullEmail

interface OnDeleteEmailListener {
    fun onDeleteConfirmed(fullEmail: FullEmail)
}