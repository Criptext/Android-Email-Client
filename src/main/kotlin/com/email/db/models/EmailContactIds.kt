package com.email.db.models

/**
 * Created by gabriel on 4/24/18.
 */

class EmailContactIds(val email: Email, val toIds: List<String>, val ccIds: List<String>,
                      val bccIds: List<String>, val senderId: String)