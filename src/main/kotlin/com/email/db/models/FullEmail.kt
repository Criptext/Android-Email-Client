package com.email.db.models

/**
 * Created by sebas on 3/12/18.
 */

data class FullEmail(
        val email: Email,
        val labels: ArrayList<Label>,
        val cc: ArrayList<Contact>,
        val to: ArrayList<Contact>,
        val bcc: ArrayList<Contact>,
        val files: ArrayList<File>)
