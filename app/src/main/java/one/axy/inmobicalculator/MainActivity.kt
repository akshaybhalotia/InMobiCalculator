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
    lateinit var txtInput: TextView

    // Represent whether the lastly pressed key is numeric or not
    var lastNumeric: Boolean = false

    // Represent that current state is in error or not
    var stateError: Boolean = false

    // If true, do not allow to add another DOT
    var lastDot: Boolean = false

    lateinit var interstitialAd : InMobiInterstitial

    override fun onCreate(savedInstanceState: Bundle?) {
        var consentObject: JSONObject = JSONObject()
        try {
            // Provide correct consent value to sdk which is obtained by User
//            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_AVAILABLE, true);
            // Provide 0 if GDPR is not applicable and 1 if applicable
            consentObject.put("gdpr", "0")
            // Provide user consent in IAB format
//            consentObject.put(InMobiSdk.IM_GDPR_CONSENT_IAB, “<<consent in IAB format>>”);
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        InMobiSdk.init(this, "07bbd7266ce348028a5669f9896a79ee", consentObject)
//        InMobiSdk.setLocationWithCityStateCountry("Bangalore", "Karantaka", "India")
        InMobiSdk.setLogLevel(InMobiSdk.LogLevel.DEBUG)
        var myBannerAdEventListener = MyBannerAdEventListener()

        var mInterstitialAdEventListener = MyInterstitialAdEventListener()

        interstitialAd = InMobiInterstitial(
            this@MainActivity,
            1582132586349L,
            mInterstitialAdEventListener
        )
        interstitialAd.load()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        txtInput = findViewById(R.id.txtInput)

        val bannerAd = findViewById(R.id.banner) as InMobiBanner
        bannerAd.setListener(myBannerAdEventListener)
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

    /**
     * Calculate the output using Exp4j
     */
    fun onEqual(view: View) {
        interstitialAd.show()

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

class MyBannerAdEventListener : BannerAdEventListener() {
    override fun onAdLoadFailed(p0: InMobiBanner?, p1: InMobiAdRequestStatus?) {
        println("Banner----------------------------------")
        println(p1?.statusCode)
        println(p1?.message)
        println("----------------------------------")
    }

    override fun onAdLoadSucceeded(p0: InMobiBanner?) {
        println("Banner++++++++++++++++++++++++++++++++++")
    }
}

class MyInterstitialAdEventListener : InterstitialAdEventListener() {
    override fun onAdLoadFailed(p0: InMobiInterstitial?, p1: InMobiAdRequestStatus?) {
        println("Interstitial++++++++++++++++++++++++++++++++++")
        println(p1?.statusCode)
        println(p1?.message)
        println("++++++++++++++++++++++++++++++++++")
    }

    override fun onAdLoadSucceeded(p0: InMobiInterstitial?) {
        println("Interstitial++++++++++++++++++++++++++++++++++")
    }
}