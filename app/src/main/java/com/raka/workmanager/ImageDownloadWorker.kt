package com.raka.workmanager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

class ImageDownloadWorker(private val context: Context, workerParameters: WorkerParameters):Worker(context,workerParameters) {
    private val notificationId: Int = 500
    private val notificationChannel: String = "demo"
    private val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    override fun doWork(): Result {
        displayNotification(ProgressUpdateEvent("please wait..",3,0))
        val imagesJson = inputData.getString("images")
        val gson = Gson()
        val listImages = gson.fromJson<List<Image>>(imagesJson, object :TypeToken<List<Image>>(){}.type)
        listImages.forEachIndexed{index,image->
            Thread.sleep(1000)
            downloadImage(image,index)
        }
        notificationManager.cancel(notificationId)
        return Result.success()
    }

    @SuppressLint("CheckResult")
    private fun downloadImage(image: Image, index: Int) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(image.url)
            .build()
        try {
            val response = client.newCall(request).execute()
            val bitmap = BitmapFactory.decodeStream(response.body?.byteStream())
            ImageUtil.saveBitmap(context,bitmap,image.title).subscribe({img->
                displayNotification(ProgressUpdateEvent(image.title,3,index+1))
                EventBus.getDefault().post(ImageDownloadedEvent(img,image.title,image.id))
            },{error->
                error.printStackTrace()
            })
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun displayNotification(prog:ProgressUpdateEvent){
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            val channel = NotificationChannel(notificationChannel,notificationChannel,NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(applicationContext,notificationChannel)
        val remoteView = RemoteViews(applicationContext.packageName,R.layout.custom_notif)
        remoteView.apply {
            setImageViewResource(R.id.iv_notif,R.drawable.eminem)
            setTextViewText(R.id.tv_notif_progress,"${prog.message} (${prog.progress}/${prog.total} complete")
            setTextViewText(R.id.tv_notif_title,"Downloading Images")
            setProgressBar(R.id.pb_notif,prog.total,prog.progress,false)
        }
        notificationBuilder.setContent(remoteView)
            .setSmallIcon(R.drawable.eminem)
        notificationManager.notify(notificationId,notificationBuilder.build())
    }

    override fun onStopped() {
        super.onStopped()
        notificationManager.cancel(notificationId)
    }

    data class ProgressUpdateEvent(var message: String, var total: Int, var progress: Int)
}