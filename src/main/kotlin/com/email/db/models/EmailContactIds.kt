package com.email.db.models

/**
 * Created by gabriel on 4/24/18.
 */

class EmailContactIds(val email: Email, val toIds: List<Long>, val ccIds: List<Long>,
                      val bccIds: List<Long>, val senderId: Long)