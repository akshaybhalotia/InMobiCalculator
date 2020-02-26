package one.axy.inmobicalculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.inmobi.ads.InMobiAdRequestStatus
import com.inmobi.ads.InMobiBanner
import com.inmobi.ads.InMobiInterstitial
import com.inmobi.ads.listeners.BannerAdEventListener
import com.inmobi.ads.listeners.InterstitialAdEventListener
import com.inmobi.sdk.InMobiSdk
import net.objecthunter.exp4j.ExpressionBuilder
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    // TextView used to display the input and output
    private lateinit var txtInput: TextView

    // Represent whether the lastly pressed key is numeric or not
    private var lastNumeric: Boolean = false

    // Represent that current state is in error or not
    private var stateError: Boolean = false

    // If true, do not allow to add another DOT
    private var lastDot: Boolean = false

    inner class MyBannerAdEventListener : BannerAdEventListener() {

        override fun onAdClicked(p0: InMobiBanner?, p1: MutableMap<Any, Any>?) {
            println("Banner clicked - yayyyyy, monies")
            println(p1)
            p0?.load()
        }

        override fun onAdLoadFailed(p0: InMobiBanner?, p1: InMobiAdRequestStatus?) {
            println("Banner load failed")
            println(p1?.statusCode)
            println(p1?.message)
            p0?.load()
        }

        override fun onAdLoadSucceeded(p0: InMobiBanner?) {
            println("Banner load succeeded")
        }

    }

    inner class MyInterstitialAdEventListener : InterstitialAdEventListener() {

        override fun onAdClicked(p0: InMobiInterstitial?, p1: MutableMap<Any, Any>?) {
            println("Interstitial clicked - yayyyyy, more monies")
            println(p1)
            interstitialAdLoaded = false
            p0?.load()
            evaluate()
        }

        override fun onAdDisplayFailed(p0: InMobiInterstitial?) {
            println("Interstitial failed to display")
            interstitialAdLoaded = false
            p0?.load()
            evaluate()
        }

        override fun onAdDismissed(p0: InMobiInterstitial?) {
            println("Interstitial dismissed")
            interstitialAdLoaded = false
            p0?.load()
            evaluate()
        }

        override fun onAdLoadFailed(p0: InMobiInterstitial?, p1: InMobiAdRequestStatus?) {
            println("Interstitial load failed")
            println(p1?.statusCode)
            println(p1?.message)
            interstitialAdLoaded = false
            p0?.load()
        }

        override fun onAdLoadSucceeded(p0: InMobiInterstitial?) {
            println("Interstitial load succeeded")
            interstitialAdLoaded = true
        }

    }

    private lateinit var interstitialAd : InMobiInterstitial
    private lateinit var mInterstitialAdEventListener: InterstitialAdEventListener
    private var interstitialAdLoaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val consentObject = JSONObject()
        try {
            consentObject.put("gdpr", "0")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        InMobiSdk.init(this, "07bbd7266ce348028a5669f9896a79ee", consentObject)
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        txtInput = findViewById(R.id.txtInput)

        mInterstitialAdEventListener = MyInterstitialAdEventListener()
        interstitialAd = InMobiInterstitial(
            this@MainActivity,
            1582132586349L,
            mInterstitialAdEventListener
        )
        interstitialAd.load()

        val mBannerAdEventListener = MyBannerAdEventListener()
        val bannerAd = findViewById<InMobiBanner>(R.id.banner)
        bannerAd.setListener(mBannerAdEventListener)
        bannerAd.setAnimationType(InMobiBanner.AnimationType.ROTATE_HORIZONTAL_AXIS)
        bannerAd.load()
    }

    /**
     * Append the Button.text to the TextView
     */
    fun onDigit(view: View) {
        if (stateError) {
            // If current state is Error, replace the error message
            txtInput.text = (view as Button).text
            stateError = false
        } else {
            // If not, already there is a valid expression so append to it
            txtInput.append((view as Button).text)
        }
        // Set the flag
        lastNumeric = true
    }

    /**
     * Append . to the TextView
     */
    fun onDecimalPoint(view: View) {
        if (lastNumeric && !stateError && !lastDot) {
            txtInput.append(".")
            lastNumeric = false
            lastDot = true
        }
    }

    /**
     * Append +,-,*,/ operators to the TextView
     */
    fun onOperator(view: View) {
        if (lastNumeric && !stateError) {
            txtInput.append((view as Button).text)
            lastNumeric = false
            lastDot = false    // Reset the DOT flag
        }
    }


    /**
     * Clear the TextView
     */
    fun onClear(view: View) {
        this.txtInput.text = ""
        lastNumeric = false
        stateError = false
        lastDot = false
    }

    fun onEqual(view: View) {
        if (interstitialAdLoaded)
            interstitialAd.show()
        else
            evaluate()
    }

    /**
     * Calculate the output using Exp4j
     */
    fun evaluate() {
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError) {
            // Read the expression
            val txt = txtInput.text.toString()
            // Create an Expression (A class from exp4j library)
            val expression = ExpressionBuilder(txt).build()
            try {
                // Calculate the result and display
                val result = expression.evaluate()
                txtInput.text = result.toString()
                lastDot = true // Result contains a dot
            } catch (ex: ArithmeticException) {
                // Display an error message
                txtInput.text = "Error"
                stateError = true
                lastNumeric = false
            }
        }
    }
}
