package de.marmaro.krt.ffupdater

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import de.marmaro.krt.ffupdater.device.DeviceSdkTester

class CrashReportActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.crash_report_layout)

        findViewById<TextView>(R.id.crash_report__explanation_textview).text =
            intent.extras?.getString(EXTRA_EXCEPTION_EXPLANATION, "/")
        findViewById<Button>(R.id.crash_report__copy_error_message_to_clipboard_button).setOnClickListener {
            copyErrorMessageToClipboard()
        }
        findViewById<Button>(R.id.crash_report__got_to_notabug_org_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, NOTABUG_URI))
        }
        findViewById<Button>(R.id.crash_report__got_to_github_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, GITHUB_URI))
        }
        findViewById<Button>(R.id.crash_report__got_to_gitlab_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, GITLAB_URI))
        }

        findViewById<TextView>(R.id.crash_report__exception_stack_trace).text =
            intent.extras?.getString(EXTRA_EXCEPTION_STACK_TRACE) ?: "/"
    }

    private fun copyErrorMessageToClipboard() {
        val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val crashReport = getCrashReport()
        val clip = ClipData.newPlainText("FFUpdater crash report", crashReport)
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(this, "Crash report is copied to your clipboard", Toast.LENGTH_LONG)
            .show()
    }

    private fun getCrashReport(): String {
        return """
            |Stacktrace:
            |```
            |${intent.extras?.getString(EXTRA_EXCEPTION_STACK_TRACE)}
            |```
            |Device information:
            || key | value |
            || --- | --- |
            || version name | ${BuildConfig.VERSION_NAME} |
            || version code | ${BuildConfig.VERSION_CODE} |
            || build type | ${BuildConfig.BUILD_TYPE} |
            || SDK version | ${Build.VERSION.SDK_INT} |
            || android release | ${Build.VERSION.RELEASE} |
            || board | ${Build.BOARD} |
            || brand | ${Build.BRAND} |
            || device | ${Build.DEVICE} |
            || manufacturer | ${Build.MANUFACTURER} |
            || model | ${Build.MODEL} |
            || product | ${Build.PRODUCT} |
            || SOC manufacturer | ${if (DeviceSdkTester.supportsAndroid12()) Build.SOC_MANUFACTURER else "/"} |
            || SOC model | ${if (DeviceSdkTester.supportsAndroid12()) Build.SOC_MODEL else "/"} |
            || supported ABIs | ${Build.SUPPORTED_ABIS.joinToString()} |
            || tags | ${Build.TAGS} |
            || build time | ${Build.TIME} |
        """.trimMargin()
    }

    companion object {
        const val EXTRA_EXCEPTION_STACK_TRACE = "exception_stack_trace"
        const val EXTRA_EXCEPTION_EXPLANATION = "exception_explanation"
        val NOTABUG_URI: Uri = Uri.parse("https://notabug.org/Tobiwan/ffupdater/issues")
        val GITHUB_URI: Uri = Uri.parse("https://github.com/Tobi823/ffupdater/issues")
        val GITLAB_URI: Uri = Uri.parse("https://gitlab.com/Tobiwan/ffupdater_gitlab/-/issues")

        fun createIntent(context: Context, throwable: Throwable, description: String): Intent {
            val intent = Intent(context, CrashReportActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra(EXTRA_EXCEPTION_STACK_TRACE, throwable.stackTraceToString().trim())
            intent.putExtra(EXTRA_EXCEPTION_EXPLANATION, description)
            return intent
        }
    }
}