package app.tcodes.scantext.ui

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.ShareCompat
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import app.tcodes.scantext.BuildConfig
import app.tcodes.scantext.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.main_fragment.view.*
import java.io.File
import java.io.FileOutputStream

class MainFragment : Fragment() {

    companion object {
        const val READ_REQUEST_CODE = 124
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    lateinit var mInterstitialAd : InterstitialAd

    private var bitmap: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        setupView(view)

        MobileAds.initialize(requireActivity().applicationContext, getString(R.string.adsID))

        mInterstitialAd = InterstitialAd(requireActivity().applicationContext)
        mInterstitialAd.adUnitId = "ca-app-pub-7015642679712581/1559194454"
        mInterstitialAd.loadAd(AdRequest.Builder()
               // .addTestDevice("B4D488B978346ED1FC19DFC4AD74E049")
                .build())

        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.main_menu, menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val subject = "I extracted this text from an image using Snap Text \nhttps://play.google.com/store/apps/details?id=app.tcodes.scantext"
        val mimeType = "text/plain"

        if (item?.itemId == R.id.action_share){
            ShareCompat.IntentBuilder.from(requireActivity())
                    .setText(subject + "\n\n" + textViewResult.text.toString())
                    .setType(mimeType)
                    .setChooserTitle(subject)
                    .setSubject(subject)
                    .startChooser()
                    return true
        }
        if (item?.itemId == R.id.action_upload){
            val photoGalleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            photoGalleryIntent.type = "image/*"
            startActivityForResult(photoGalleryIntent, READ_REQUEST_CODE)
            emptyImage.visibility = View.GONE
            loadAd()


        }


        return super.onOptionsItemSelected(item)

    }

    //
    fun loadAd(){

        Handler().postDelayed({
            kotlin.run {if(mInterstitialAd.isLoaded) {mInterstitialAd.show()}
            else{ Log.d ("AdError", "The interstitial wasn't loaded yet")} }
        }, 10000)

    }

    private fun setupView(view: View) {
        view.buttonOCR.setOnClickListener{
            viewModel.ocrDetection(bitmap)
            Handler().postDelayed({
                kotlin.run {if(mInterstitialAd.isLoaded) {mInterstitialAd.show()}
                else{ Log.d ("AdError", "The interstitial wasn't loaded yet")} }
            }, 5000)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.processedBitmap.observe(this, Observer {
            bitmap = it
            view?.imageViewSelectedPhoto?.setImageBitmap(it)
        })
        viewModel.textResult.observe(this, Observer { it ->
            view?.textViewResult?.text = it
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                val uri = resultData.data
                Log.i("MainFragment", "Uri: " + uri!!.toString())
                showImage(uri)
            }
        }
    }

    private fun showImage(uri: Uri) {
        bitmap = BitmapUtils.loadBitmap(requireContext(), uri)
        view?.imageViewSelectedPhoto?.setImageBitmap(bitmap)
    }

    private fun shareImage() {
        val cachePath = File(requireActivity().cacheDir, "images")
        cachePath.mkdirs()
        val imagePath = File(requireActivity().cacheDir, "images/temp_image.png")
        val stream = FileOutputStream(imagePath) // overwrites this image every time
        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(requireContext(), BuildConfig.APPLICATION_ID + ".fileprovider", imagePath)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, requireActivity().contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            shareIntent.putExtra(Intent.EXTRA_TEXT,textViewResult.text)
            startActivity(Intent.createChooser(shareIntent, getString(R.string.choose_an_app)))
        }
    }

}
