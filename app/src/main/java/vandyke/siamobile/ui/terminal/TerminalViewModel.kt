/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package vandyke.siamobile.ui.terminal

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import vandyke.siamobile.App
import vandyke.siamobile.R
import vandyke.siamobile.siadOutput
import vandyke.siamobile.util.StorageUtil
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class TerminalViewModel(application: Application) : AndroidViewModel(application) {
    val output = MutableLiveData<String>()

    private val handler = Handler(Looper.getMainLooper())

    private var siacFile: File? = null
    private val subscription = siadOutput.observeOn(AndroidSchedulers.mainThread()).subscribe {
        appendToOutput(it + "\n")
    }

    init {
        output.value = getApplication<App>().getString(R.string.terminal_warning)
        siacFile = StorageUtil.copyBinary("siac", application)
    }

    override fun onCleared() {
        super.onCleared()
        subscription.dispose()
    }

    private fun appendToOutput(text: String) {
        /* need to use this handler so that it's run on the main thread */
        handler.post {
            output.value = text
        }
    }

    fun runSiacCommand(command: String) {
        if (siacFile == null) {
            appendToOutput("\nCould not run siac.\n")
            return
        }
        val fullCommand = command.split(" ".toRegex()).toMutableList()
        fullCommand.add(0, siacFile!!.absolutePath)
        val pb = ProcessBuilder(fullCommand)
        pb.redirectErrorStream(true)
        val siac = pb.start()

        launch(CommonPool) {
            try {
                val stdOut = SpannableStringBuilder()
                stdOut.append("\n" + command + "\n")
                stdOut.setSpan(StyleSpan(Typeface.BOLD), 0, stdOut.length, 0)

                val inputReader = BufferedReader(InputStreamReader(siac.inputStream))
                var line: String? = inputReader.readLine()
                while (line != null) {
                    val toBeAppended = line.replace(siacFile!!.absolutePath, "siac")
                    stdOut.append(toBeAppended + "\n")
                    line = inputReader.readLine()
                }
                inputReader.close()

                appendToOutput(stdOut.toString())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}