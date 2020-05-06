package app.tcodes.scantext.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import app.tcodes.scantext.R
import kotlinx.android.synthetic.main.main_fragment.*
import kotlinx.android.synthetic.main.main_fragment.view.*

class MainFragment : Fragment() {

    companion object {
        const val READ_REQUEST_CODE = 124
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var clipboard: ClipboardManager
    private var clip: ClipData? = null
    // private lateinit var alertdialog: AlertDialog
    var isTextEmpty: Boolean = false
    // var progressBar : ProgressBar? = null

    private var bitmap: Bitmap? = null
    private var result: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        setupView(view)

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
        val subject = "I extract text from images using Snap Text \nhttps://play.google.com/store/apps/details?id=app.tcodes.scantext"
        val mimeType = "text/plain"

        if (item?.itemId == R.id.action_share) {
            ShareCompat.IntentBuilder.from(requireActivity())
                    .setText(subject)
                    .setType(mimeType)
                    .setChooserTitle(subject)
                    .setSubject(subject)
                    .startChooser()
            return true
        }
        if (item?.itemId == R.id.action_upload) {
            val photoGalleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            photoGalleryIntent.type = "image/*"
            startActivityForResult(photoGalleryIntent, READ_REQUEST_CODE)

        }

        if (item?.itemId == R.id.action_policy) {

            val builder = AlertDialog.Builder(this@MainFragment.requireContext())
            //  alertdialog = builder.create()

            val v = LayoutInflater.from(this@MainFragment.requireContext()).inflate(R.layout.policy_dialog, null)


            val okay = v.findViewById<Button>(R.id.okay_button)
            builder.setTitle("Privacy Policy")

            okay.setOnClickListener { builder.create().dismiss() }

            builder.setView(v)
            builder.show()
            builder.setCancelable(false)


        }



        return super.onOptionsItemSelected(item)

    }

    //

    private fun setupView(view: View) {
        view.buttonOCR.setOnClickListener { it ->
            if (isTextEmpty) {
                viewModel.ocrDetection(bitmap)
                progressBar.visibility = View.VISIBLE
            }

        }



        view.imageViewSelectedPhoto.setOnClickListener {
            val photoGalleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            photoGalleryIntent.type = "image/*"
            startActivityForResult(photoGalleryIntent, READ_REQUEST_CODE)

        }

        view.buttonCopy.setOnClickListener {
            if (textViewResult.text.isEmpty()) {
                Toast.makeText(requireContext(), "Text is empty", Toast.LENGTH_LONG).show()

            } else {
                val clipboard = requireContext().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("label", textViewResult.text.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Text copied", Toast.LENGTH_LONG).show()

            }
        }

        view.shareText.setOnClickListener {


            if (textViewResult.text.isEmpty()) {
                Toast.makeText(requireContext(), "Text is empty", Toast.LENGTH_LONG).show()

            } else {
                val mimeType = "text/plain"
                ShareCompat.IntentBuilder.from(requireActivity())
                        .setText(textViewResult.text.toString())
                        .setType(mimeType)
                        .startChooser()
            }
        }
    }

//    private fun toggleRefresh() {
//        if (mProgressBar.getVisibility() == View.INVISIBLE) {
//            mProgressBar.setVisibility(View.VISIBLE)
//            mRefreshImageView.setVisibility(View.INVISIBLE)
//
//            mHourlyButton.setVisibility(View.INVISIBLE)
//            mDailyButton.setVisibility(View.INVISIBLE)
//
//
//        } else {
//            mProgressBar.setVisibility(View.INVISIBLE)
//            mRefreshImageView.setVisibility(View.VISIBLE)
//
//            mHourlyButton.setVisibility(View.VISIBLE)
//            mDailyButton.setVisibility(View.VISIBLE)
//
//        }
//    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.processedBitmap.observe(this, Observer {
            bitmap = it
            view?.imageViewSelectedPhoto?.setImageBitmap(it)
        })
        viewModel.textResult.observe(this, Observer {
            view?.textViewResult?.text = it
            progressBar.visibility = View.INVISIBLE
            buttonCopy.visibility = View.VISIBLE
            shareText.visibility = View.VISIBLE
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                val uri = resultData.data
                Log.i("MainFragment", "Uri: " + uri!!.toString())
                showImage(uri)
                isTextEmpty = true
                textViewResult.visibility = View.VISIBLE
                textViewResult.text = ""
                emptyImage.visibility = View.GONE
                imageEmpty.visibility = View.GONE

            }
        }
    }

//

    private fun showImage(uri: Uri) {
        bitmap = BitmapUtils.loadBitmap(requireContext(), uri)
        view?.imageViewSelectedPhoto?.setImageBitmap(bitmap)
    }

}
