package com.email.scenes.mailbox

import com.email.db.models.FullEmail

interface OnDeleteEmailListener {
    fun onDeleteConfirmed(fullEmail: FullEmail)
}