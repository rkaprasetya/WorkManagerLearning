
package com.raka.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import io.reactivex.Single
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageUtil {

    fun saveBitmap(context: Context,bitmap: Bitmap,filename:String):Single<String>{
        return Single.create<String>{emitter ->
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG,30,stream)
            val mediaByteArray = stream.toByteArray()
            try {
                val myDir = context.filesDir
                val path = "$myDir/media/"
                Log.e("eje","dor $path")
                val secondFile = File("$myDir/media/",filename)
                if(!secondFile.parentFile.exists()){
                    secondFile.parentFile.mkdirs()
                }
                secondFile.createNewFile()
               FileOutputStream(secondFile).apply {
                    write(mediaByteArray)
                    flush()
                    close()
                }
                emitter.onSuccess(path)

            }catch (e:IOException){
                e.printStackTrace()
                emitter.onError(e)
            }
        }
    }
}