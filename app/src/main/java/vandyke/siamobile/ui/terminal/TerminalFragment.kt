/*
 * Copyright (c) 2017 Nicholas van Dyke
 *
 * This file is subject to the terms and conditions defined in 'LICENSE.md'
 */

package vandyke.siamobile.ui.terminal

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
import vandyke.siamobile.backend.siad.SiadService
import vandyke.siamobile.ui.BaseFragment
import vandyke.siamobile.util.StorageUtil
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class TerminalFragment : BaseFragment(), SiadService.SiadListener {
    private var siacFile: File? = null

    private var outputBuffer: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_terminal, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        siacFile = StorageUtil.copyBinary("siac", context!!, true)

        input.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            try {
                if (siacFile == null) {
                    output.append("\nYour device's CPU architecture is not supported by siac. Sorry!\n")
                    return@OnEditorActionListener true
                }
                val enteredCommand = v.text.toString()
                v.text = ""
                val fullCommand = enteredCommand.split(" ".toRegex()).toMutableList()
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

                        activity!!.runOnUiThread { output.append(stdOut) }
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
        output.append(SiadService.bufferedOutput)
        SiadService.addListener(this)
    }

    override fun onSiadOutput(line: String) {
        if (isHidden)
            outputBuffer += "$line\n"
        else
            output.append("$line\n")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            output.append(outputBuffer)
            outputBuffer = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SiadService.removeListener(this)
    }
}
