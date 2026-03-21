package com.siganus.oexengine

import android.os.Bundle
import android.content.res.AssetManager
import java.io.File
import android.text.method.LinkMovementMethod
import android.widget.TextView
import android.widget.Button
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val workingDir: File by lazy { getExternalFilesDir(null) ?: filesDir }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setTitle("YaneuraOu7.5 + Suisho 5")
        setContentView(R.layout.activity_main)

        val linkText = findViewById<TextView>(R.id.linkText)
        // Enable clicking on links
        linkText.movementMethod = LinkMovementMethod.getInstance()

        val logText = findViewById<TextView>(R.id.log)
        val testButton = findViewById<Button>(R.id.testButton)

        testButton.setOnClickListener {
            testButton.isEnabled = false
            val originalText = testButton.text
            testButton.text = "実行中..."
            logText.text = ""
            Thread {
                try {
                    // 1) copy assets engine_data -> workingDir
                    copyAssetDirContents(assets, "engine_data", workingDir)

                    // 2) start process with working dir
                    val execPath = resolveExecutableAssetPath()
                    val pb = ProcessBuilder(execPath)
                    pb.redirectErrorStream(true)
                    pb.directory(workingDir)
                    val process = pb.start()

                    // 3) perform USI handshake: send "usi" -> wait for "usiok" -> send "isready" -> wait for "readyok"
                    val writer = process.outputStream.bufferedWriter()
                    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                        // send usi
                        writer.write("usi\n")
                        writer.flush()

                        var line: String?
                        var stage = 0 // 0: waiting usiok, 1: waiting readyok
                        while (reader.readLine().also { line = it } != null) {
                            val toAppend = line + "\n"
                            runOnUiThread { logText.append(toAppend) }

                            val trimmed = line?.trim() ?: ""
                            if (stage == 0 && trimmed.equals("usiok", ignoreCase = true)) {
                                writer.write("isready\n")
                                writer.flush()
                                stage = 1
                            } else if (stage == 1 && trimmed.equals("readyok", ignoreCase = true)) {
                                break
                            }
                        }
                    }
                    writer.close()

                    process.waitFor()
                } catch (e: Exception) {
                    val sw = java.io.StringWriter()
                    e.printStackTrace(java.io.PrintWriter(sw))
                    val trace = sw.toString()
                    runOnUiThread { logText.append("Error: ${e.message}\n$trace") }
                } finally {
                    runOnUiThread {
                        testButton.isEnabled = true
                        testButton.text = originalText
                    }
                }
            }.start()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun resolveExecutableAssetPath(): String {
        val nativeLibDir = applicationInfo.nativeLibraryDir

        return "$nativeLibDir/$EXECUTABLE_NAME"
    }

    // 中身だけコピーする場合
    private fun copyAssetDirContents(assets: AssetManager, srcPath: String, destDir: File) {
        destDir.mkdirs()
        assets.list(srcPath)?.forEach { child ->
            copyAssetDir(assets, "$srcPath/$child", destDir)
        }
    }

    private fun copyAssetDir(assets: AssetManager, srcPath: String, destDir: File) {
        val list = assets.list(srcPath)

        println("src $srcPath => $destDir")
        if (list.isNullOrEmpty()) {
            // file
            assets.open(srcPath).use { input ->
                File(destDir, srcPath.substringAfterLast("/"))
                    .outputStream().use { output -> input.copyTo(output) }
            }
        } else {
            // directory -> recurse
            val subDir = File(destDir, srcPath.substringAfterLast("/"))
            if (!subDir.exists()) subDir.mkdirs()
            list.forEach { child ->
                copyAssetDir(assets, "$srcPath/$child", subDir)
            }
        }
    }

    companion object {
        private const val EXECUTABLE_NAME = "libyaneuraou.so"
    }
}