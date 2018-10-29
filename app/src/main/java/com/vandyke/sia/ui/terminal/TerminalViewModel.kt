/*
 * Copyright (c) 2017 Nicholas van Dyke. All rights reserved.
 */

package com.vandyke.sia.ui.terminal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.vandyke.sia.App
import com.vandyke.sia.R
import com.vandyke.sia.data.local.Prefs
import com.vandyke.sia.util.StorageUtil
import kotlinx.coroutines.CommonPool
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class TerminalViewModel(application: Application) : AndroidViewModel(application) {

    val output = MutableLiveData<String>()

    private var siacFile: File? = null

    init {
        output.value = getApplication<App>().getString(R.string.terminal_warning)
        siacFile = StorageUtil.copyFromAssetsToAppStorage("siac-${Prefs.siaVersion}", application)
    }

    private fun appendToOutput(text: String) {
        output.postValue(text + "\n")
    }

    fun runSiacCommand(command: String) {
        if (siacFile == null) {
            appendToOutput("\nCould not run siac.\n")
            return
        }
        val fullCommand = command.split(" ").toMutableList()
        fullCommand.add(0, siacFile!!.absolutePath)
        val pb = ProcessBuilder(fullCommand)
        pb.redirectErrorStream(true)
        val siac = pb.start()

        launch(CommonPool) {
            try {
                val stdOut = StringBuilder()
                stdOut.append("\n" + command + "\n")

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