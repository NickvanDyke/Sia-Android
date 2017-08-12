/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.terminal

import android.app.Fragment
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.ScrollingMovementMethod
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_terminal.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import vandyke.siamobile.R
import vandyke.siamobile.ui.MainActivity
import vandyke.siamobile.util.StorageUtil
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class TerminalFragment : Fragment() {

    private var siacFile: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_terminal, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        if (MainActivity.appTheme === MainActivity.Theme.AMOLED || MainActivity.appTheme === MainActivity.Theme.CUSTOM) {
            bot_shadow.visibility = View.GONE
        } else if (MainActivity.appTheme === MainActivity.Theme.DARK) {
            bot_shadow.setBackgroundResource(R.drawable.bot_shadow_dark)
        }

        siacFile = StorageUtil.copyBinary("siac", activity, true)

        input.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            try {
                if (siacFile == null) {
                    output.append("\nYour device's CPU architecture is not supported by siac. Sorry! There's nothing Sia Mobile can do about this\n")
                    return@OnEditorActionListener true
                }
                val enteredCommand = v.text.toString()
                v.text = ""
                val fullCommand = ArrayList(Arrays.asList(*enteredCommand.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
                fullCommand.add(0, siacFile!!.absolutePath)
                val pb = ProcessBuilder(fullCommand)
                pb.redirectErrorStream(true)
                val siac = pb.start()

                async(CommonPool) {
                    try {
                        val stdOut = SpannableStringBuilder()
                        stdOut.append("\n" + enteredCommand + "\n")
                        stdOut.setSpan(StyleSpan(Typeface.BOLD), 0, stdOut.length, 0)

                        val inputReader = BufferedReader(InputStreamReader(siac.inputStream))
                        var line: String? = inputReader.readLine()
                        while (line != null) {
                            val toBeAppended = line.replace(siacFile!!.absolutePath, "siac")
                            stdOut.append(toBeAppended + "\n")
                            line = inputReader.readLine()
                        }
                        inputReader.close()

                        activity.runOnUiThread { output.append(stdOut) }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            true
        })

        output.movementMethod = ScrollingMovementMethod()
    }
}
