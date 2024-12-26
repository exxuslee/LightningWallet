package io.horizontalsystems.lightningwallet

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.WindowManager
import com.google.zxing.MultiFormatReader
import com.google.zxing.client.android.DecodeFormatManager
import com.google.zxing.client.android.DecodeHintManager
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.camera.CameraSettings
import io.horizontalsystems.lightningwallet.databinding.ActivityQrScannerBinding
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


abstract class QrScanActivity : BaseActivity() {

    private lateinit var binding: ActivityQrScannerBinding

    private val callback = BarcodeCallback {
        binding.barcodeView.pause()
        // Slow down fast transition to a new window
        Handler().postDelayed({
            onScan(it.text)
        }, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appBarLayout.setPadding(0, getStatusBarHeight(), 0, 0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.systemUiVisibility = 0

        initializeFromIntent(intent)

        binding.buttonPaste.setOnClickListener {
            onPaste()
        }

        binding.barcodeView.decodeContinuous(callback)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onResume() {
        super.onResume()
        openCameraWithPermission()
    }

    override fun onPause() {
        super.onPause()
        binding.barcodeView.pause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    abstract fun onScan(text: String)

    abstract fun onPaste()

    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSION)
    protected fun openCameraWithPermission() {
        val perms = arrayOf(Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            binding.barcodeView.resume()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.NodeCredentials_PleaseGrantCameraPermission),
                REQUEST_CAMERA_PERMISSION,
                *perms
            )
        }
    }

    private fun resetErrorWithDelay() {
        // Reset after 3 seconds
        Handler().postDelayed({
            resetInput()
        }, 3 * 1000)
    }

    abstract fun resetInput()

    protected fun showDescription(descriptionText: Int) {
        binding.errorTxt.visibility = View.INVISIBLE
        binding.descriptionTxt.setText(descriptionText)
        binding.descriptionTxt.visibility = View.VISIBLE
    }

    protected fun showError(errorText: Int) {
        binding.descriptionTxt.visibility = View.INVISIBLE
        binding.errorTxt.setText(errorText)
        binding.errorTxt.visibility = View.VISIBLE
        resetErrorWithDelay()
    }

    private fun initializeFromIntent(intent: Intent) {
        // Scan the formats the intent requested, and return the result to the calling activity.
        val decodeFormats = DecodeFormatManager.parseDecodeFormats(intent)
        val decodeHints = DecodeHintManager.parseDecodeHints(intent)
        val settings = CameraSettings()
        if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
            val cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1)
            if (cameraId >= 0) {
                settings.requestedCameraId = cameraId
            }
        }

        // Check what type of scan. Default: normal scan
        val scanType = intent.getIntExtra(Intents.Scan.SCAN_TYPE, 0)
        val characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET)
        val reader = MultiFormatReader()
        reader.setHints(decodeHints)
        binding.barcodeView.cameraSettings = settings
        binding.barcodeView.decoderFactory = DefaultDecoderFactory(
            decodeFormats,
            decodeHints,
            characterSet,
            scanType
        )
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 1
    }
}