package com.criptext.mail.services.jobs

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.Account
import com.criptext.mail.services.CloudBackupForegroundService
import com.criptext.mail.services.DecryptionService
import com.criptext.mail.services.data.JobIdData
import com.criptext.mail.utils.AccountUtils
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest


class CloudBackupJobService: Job() {

    private fun getAccountsSavedData(storage: KeyValueStorage): MutableList<JobIdData> {
        val savedJobsString = storage.getString(KeyValueStorage.StringKey.SavedJobs, "")
        return if(savedJobsString.isEmpty()) mutableListOf()
        else JobIdData.fromJson(savedJobsString)
    }

    private fun deleteJobForMissingAccount(accountId: Long, storage: KeyValueStorage){
        val jobList = getAccountsSavedData(storage)
        val job = jobList.find { it.accountId == accountId } ?: return
        jobList.remove(job)
        cancel(context, accountId)
    }

    fun run(){
        val builder = JobRequest.Builder(JOB_TAG)
        builder.setRequiredNetworkType(JobRequest.NetworkType.ANY)
        builder.startNow()
                .build()
                .schedule()
    }

    fun schedule(context: Context, intervalMillis: Long, accountId: Long, useWifiOnly: Boolean) {
        val builder = JobRequest.Builder(JOB_TAG)
        builder.setRequiredNetworkType(JobRequest.NetworkType.ANY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setPeriodic(intervalMillis, JobInfo.getMinFlexMillis())
        }else {
            builder.setPeriodic(intervalMillis)
        }
        val id = builder.build()
                .schedule()
        val storage = KeyValueStorage.SharedPrefs(context)
        val listOfJobs = getAccountsSavedData(storage)
        val accountSavedData = listOfJobs.find { it.accountId == accountId}
        if(accountSavedData != null) {
            listOfJobs.remove(accountSavedData)
        }
        listOfJobs.add(JobIdData(accountId, id, useWifiOnly))
        storage.putString(KeyValueStorage.StringKey.SavedJobs, JobIdData.toJSON(listOfJobs).toString())

        isJobServiceOn(context, id)
    }

    fun cancel(context: Context, accountId: Long) {
        val storage = KeyValueStorage.SharedPrefs(context)
        val listOfJobs = getAccountsSavedData(storage)
        val accountSavedData = listOfJobs.find { it.accountId == accountId}
        if(accountSavedData != null) {
            listOfJobs.remove(accountSavedData)
            if(listOfJobs.isNotEmpty())
                storage.putString(KeyValueStorage.StringKey.SavedJobs, JobIdData.toJSON(listOfJobs).toString())
            else
                storage.remove(listOf(KeyValueStorage.StringKey.SavedJobs))
            JobManager.instance().cancel(accountSavedData.jobId)
            Log.e("JOBSERVICE:", "Canceled!!!")
        }

    }

    private fun isJobServiceOn(context: Context, id: Int) {
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        for (jobInfo in scheduler.allPendingJobs) {
            if (jobInfo.id == id) {
                Log.e("JOBSERVICE:", "SCHEDULED!!!")
                break
            }
        }
    }

    override fun onRunJob(params: Params): Result {
        Log.e("JOBSERVICE:", "STARTED RUNNING!!!")
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnected == true
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val isWiFi: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
        }
        val storage = KeyValueStorage.SharedPrefs(context)
        val listOfJobs = getAccountsSavedData(storage)
        val accountSavedData = listOfJobs.find { it.jobId == params.id } ?: return Result.FAILURE
        val useWifiOnly = accountSavedData.useWifiOnly
        if((useWifiOnly && isConnected && isWiFi) ||
                (!useWifiOnly && isConnected && !isWiFi)) {
            val account = getAccount(accountSavedData.accountId)
            if(account != null){
                val intent = Intent(context, CloudBackupForegroundService::class.java)
                intent.putExtra("accountId", accountSavedData.accountId)
                intent.putExtra("accountEmail", account.recipientId.plus("@${account.domain}"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } else {
                deleteJobForMissingAccount(accountSavedData.accountId, storage)
                return Result.FAILURE
            }
        }
        return Result.SUCCESS
    }

    private fun getAccount(accountId: Long): Account? {
        val db = AppDatabase.getAppDatabase(context)
        return db.accountDao().getAccountById(accountId)
    }

    companion object {
        const val JOB_TAG = "CRIPTEXT_CLOUD_BACKUP_JOB_SERVICE"

        fun scheduleJob(storage: KeyValueStorage, account: Account){

            val builder = JobRequest.Builder(JOB_TAG)
            builder.setRequiredNetworkType(JobRequest.NetworkType.ANY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setPeriodic(AccountUtils.getFrequencyPeriod(account.autoBackupFrequency), JobInfo.getMinFlexMillis())
            }else {
                builder.setPeriodic(AccountUtils.getFrequencyPeriod(account.autoBackupFrequency))
            }
            val id = builder.build()
                    .schedule()
            val savedJobsString = storage.getString(KeyValueStorage.StringKey.SavedJobs, "")
            val listOfJobs = if(savedJobsString.isEmpty()) mutableListOf()
            else JobIdData.fromJson(savedJobsString)
            val accountSavedData = listOfJobs.find { it.accountId == account.id}
            if(accountSavedData != null) {
                listOfJobs.remove(accountSavedData)
            }
            listOfJobs.add(JobIdData(account.id, id, account.wifiOnly))
            storage.putString(KeyValueStorage.StringKey.SavedJobs, JobIdData.toJSON(listOfJobs).toString())
        }

        fun cancelJob(storage: KeyValueStorage, accountId: Long){
            val savedJobsString = storage.getString(KeyValueStorage.StringKey.SavedJobs, "")
            val listOfJobs = if(savedJobsString.isEmpty()) mutableListOf()
            else JobIdData.fromJson(savedJobsString)
            val accountSavedData = listOfJobs.find { it.accountId == accountId}
            if(accountSavedData != null) {
                listOfJobs.remove(accountSavedData)
                if(listOfJobs.isNotEmpty())
                    storage.putString(KeyValueStorage.StringKey.SavedJobs, JobIdData.toJSON(listOfJobs).toString())
                else
                    storage.remove(listOf(KeyValueStorage.StringKey.SavedJobs))
                JobManager.instance().cancel(accountSavedData.jobId)
                Log.e("JOBSERVICE:", "Canceled!!!")
            }
        }
    }

}