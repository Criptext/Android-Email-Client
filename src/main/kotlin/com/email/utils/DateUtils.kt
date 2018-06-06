package com.email.utils

import android.content.Context
import com.email.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class with functions for converting timestamps to formatted strings
 * Created by gabriel on 6/1/17.
 */

class DateUtils {
    companion object {
        fun getDateFromString(stringDate: String, pattern: String?) : Date{
            val sdf = if(pattern == null){
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            } else {
                SimpleDateFormat(pattern)
            }
            sdf.isLenient = false
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            return sdf.parse(stringDate)
        }

        fun parseDateWithServerFormat(dateString: String, isUTC: Boolean): Date {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            if (isUTC)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.parse(dateString)
        }
        fun printDateWithServerFormat(date: Date): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(date)
        }

        fun getHoraVerdadera(timestamp: Long): String {
            val formattedDate = SimpleDateFormat("h:mm a").format(timestamp).toUpperCase()
            return formattedDate.replace("P.M.".toRegex(), "PM").replace("A.M.".toRegex(), "AM")
        }

        fun isTheSameDay(timestampActual: Long, timestampAnterior: Long): Boolean {

            val fechaAct = java.util.Date(timestampActual)
            val fechaAnt = java.util.Date(timestampAnterior)
            val fechaActual = Calendar.getInstance()
            val fechaAnterior = Calendar.getInstance()
            fechaActual.time = fechaAct
            fechaAnterior.time = fechaAnt

            val diferenciaDias = Math.abs(fechaActual.get(Calendar.DAY_OF_MONTH) - fechaAnterior.get(Calendar.DAY_OF_MONTH))
            val diferenciaMeses = fechaActual.get(Calendar.MONTH) - fechaAnterior.get(Calendar.MONTH)

            return diferenciaMeses == 0  && diferenciaDias == 0
        }

        fun getFormattedDay(timestamp: Long, context: Context?): String {

            var formattedDate: String
            val fechaMsj = java.util.Date(timestamp)
            val fechaAct = java.util.Date()
            val fechaMensaje = Calendar.getInstance()
            val fechaActual = Calendar.getInstance()
            fechaMensaje.time = fechaMsj
            fechaActual.time = fechaAct

            var strAyer = "Yesterday"
            var strHoy = "Today"
            if (context != null) {
                strAyer = context.resources.getString(R.string.mk_label_yesterday)
                strHoy = context.resources.getString(R.string.mk_label_today)
            }

            val diferenciaDias = Math.abs(fechaMensaje.get(Calendar.DAY_OF_MONTH) - fechaActual.get(Calendar.DAY_OF_MONTH))
            val diferenciaMeses = fechaMensaje.get(Calendar.MONTH) - fechaActual.get(Calendar.MONTH)

            if (diferenciaMeses == 0) {
                if (diferenciaDias == 0) {
                    formattedDate = strHoy
                } else if (diferenciaDias == 1 || diferenciaDias == -1) {
                    formattedDate = strAyer
                } else if (diferenciaDias in 1..6) {
                    formattedDate = SimpleDateFormat("EEEE").format(timestamp)
                    formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1).toLowerCase()
                } else {
                    formattedDate = SimpleDateFormat("MM/dd/yy").format(timestamp)
                }
            } else
                formattedDate = SimpleDateFormat("MM/dd/yy").format(timestamp)

            return formattedDate
        }

        fun getFormattedDate(timestamp: Long): String {

            var formattedDate: String
            val fechaMsj = java.util.Date(timestamp)
            val fechaAct = java.util.Date()
            val fechaMensaje = Calendar.getInstance()
            val fechaActual = Calendar.getInstance()
            fechaMensaje.time = fechaMsj
            fechaActual.time = fechaAct

            val strAyer = "Yesterday"
            val diferenciaDias = Math.abs(fechaMensaje.get(Calendar.DAY_OF_MONTH) - fechaActual.get(Calendar.DAY_OF_MONTH))
            val diferenciaMeses = fechaMensaje.get(Calendar.MONTH) - fechaActual.get(Calendar.MONTH)

            if (diferenciaMeses == 0) {
                if (diferenciaDias == 0) {
                    formattedDate = SimpleDateFormat("h:mm a").format(timestamp).toUpperCase()
                    formattedDate = formattedDate.replace("P.M.".toRegex(), "PM")
                    formattedDate = formattedDate.replace("A.M.".toRegex(), "AM")
                } else if (diferenciaDias == 1 || diferenciaDias == -1) {
                    formattedDate = strAyer
                } else if (diferenciaDias in 1..6) {
                    formattedDate = SimpleDateFormat("EEEE").format(timestamp)
                    formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1).toLowerCase()
                } else {
                    formattedDate = SimpleDateFormat("MM/dd/yy").format(timestamp)
                }
            } else
                formattedDate = SimpleDateFormat("MM/dd/yy").format(timestamp)

            return formattedDate
        }

        fun getFormattedDateForActivity(timestamp: Long): String {
            var formattedDate: String
            val fechaMsj = java.util.Date(timestamp)
            val fechaAct = java.util.Date()
            val fechaMensaje = Calendar.getInstance()
            val fechaActual = Calendar.getInstance()
            fechaMensaje.time = fechaMsj
            fechaActual.time = fechaAct

            val strAyer = "Yesterday "
            val diferenciaDias = Math.abs(fechaMensaje.get(Calendar.DAY_OF_MONTH) - fechaActual.get(Calendar.DAY_OF_MONTH))
            val diferenciaMeses = fechaMensaje.get(Calendar.MONTH) - fechaActual.get(Calendar.MONTH)

            if (diferenciaMeses == 0) {
                if (diferenciaDias == 0) {
                    formattedDate = SimpleDateFormat("h:mm a").format(timestamp).toUpperCase()
                } else if (diferenciaDias == 1 || diferenciaDias == -1) {
                    formattedDate = strAyer + SimpleDateFormat("h:mm a").format(timestamp).toUpperCase()
                } else if (diferenciaDias in 1..6) {
                    formattedDate = SimpleDateFormat("EEEE h:mm a").format(timestamp)
                    formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1).toLowerCase()
                } else {
                    formattedDate = SimpleDateFormat("MMM dd, yyyy h:mm aa").format(timestamp)
                }
            } else
                formattedDate = SimpleDateFormat("MMM dd, yyyy h:mm aa").format(timestamp)

            formattedDate = formattedDate.replace("P.M.".toRegex(), "PM")
            formattedDate = formattedDate.replace("A.M.".toRegex(), "AM")

            return formattedDate
        }
    }

}