package io.samagra.oce_sample

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast

import io.samagra.odk.collect.extension.components.DaggerFormsDatabaseInteractorComponent
import io.samagra.odk.collect.extension.components.DaggerFormsNetworkInteractorComponent
import io.samagra.odk.collect.extension.components.DaggerODKInteractorComponent
import io.samagra.odk.collect.extension.interactors.FormsDatabaseInteractor
import io.samagra.odk.collect.extension.interactors.FormsNetworkInteractor
import io.samagra.odk.collect.extension.interactors.ODKInteractor
import io.samagra.odk.collect.extension.listeners.FileDownloadListener
import io.samagra.odk.collect.extension.listeners.FormsProcessListener
import io.samagra.odk.collect.extension.listeners.ODKProcessListener
import org.apache.commons.io.IOUtils
import org.odk.collect.android.injection.config.DaggerAppDependencyComponent
import org.odk.collect.forms.instances.InstancesRepository
import timber.log.Timber
import java.io.File

class ODKFeatureTesterActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var openFormsInput: EditText
    private lateinit var downloadFormsInput: EditText
    private lateinit var deleteFormsInput: EditText
    private lateinit var openSavedInput: EditText
    private lateinit var openSavedButton: Button
    private lateinit var openFormsButton: Button
    private lateinit var downloadFormsButton: Button
    private lateinit var deleteFormsButton: Button
    private lateinit var downloadAllFormsButton: Button
    private lateinit var clearAllFormsButton: Button
    private lateinit var showAllForms: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var odkInteractor: ODKInteractor
    private lateinit var networkInteractor: FormsNetworkInteractor
    private lateinit var formsDatabaseInteractor: FormsDatabaseInteractor
    private lateinit var instancesRepository: InstancesRepository

    private lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_odkfeature_tester)

        context = this
        openFormsInput = findViewById(R.id.open_form_input)
        downloadFormsInput = findViewById(R.id.download_form_input)
        deleteFormsInput = findViewById(R.id.delete_form_input)
        openFormsButton = findViewById(R.id.open_form_button)
        deleteFormsButton = findViewById(R.id.delete_form_button)
        downloadFormsButton = findViewById(R.id.download_form_button)
        downloadAllFormsButton = findViewById(R.id.download_all_forms)
        clearAllFormsButton = findViewById(R.id.clear_all_forms)
        showAllForms = findViewById(R.id.show_all_forms)
        progressBar = findViewById(R.id.form_progress)
        openSavedInput = findViewById(R.id.open_saved_form_input)
        openSavedButton = findViewById(R.id.open_saved_form_button)

        odkInteractor = DaggerODKInteractorComponent.factory().create(application).getODKInteractor()
        progressBar.visibility = View.VISIBLE
        odkInteractor.setupODK(IOUtils.toString(resources.openRawResource(R.raw.settings)), false, object :
            ODKProcessListener {
            override fun onProcessComplete() {
                val currentProjectProvider = DaggerAppDependencyComponent.builder().application(application).build().currentProjectProvider()
                currentProjectProvider.getCurrentProject().name
                formsDatabaseInteractor = DaggerFormsDatabaseInteractorComponent.factory().create(application).getFormsDatabaseInteractor()
                networkInteractor = DaggerFormsNetworkInteractorComponent.factory().create(application).getFormsNetworkInteractor()
                progressBar.visibility = View.INVISIBLE
            }
            override fun onProcessingError(exception: Exception) {
                exception.printStackTrace()
                progressBar.visibility = View.INVISIBLE
            }
        })

        openFormsButton.setOnClickListener(this)
        downloadFormsButton.setOnClickListener(this)
        deleteFormsButton.setOnClickListener(this)
        downloadAllFormsButton.setOnClickListener(this)
        clearAllFormsButton.setOnClickListener(this)
        showAllForms.setOnClickListener(this)
        openSavedButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.open_form_button -> {
                val formId: String = openFormsInput.text.toString().trim()
                if (formId.isNotBlank()) {
                    progressBar.visibility = View.VISIBLE
                    odkInteractor.openForm(formId, context, object : FormsProcessListener {
                        override fun onProcessed() {
                            progressBar.visibility = View.INVISIBLE
                        }

                        override fun onProcessingError(e: Exception) {
                            progressBar.visibility = View.INVISIBLE
                            Timber.e(e)
                            showToast(e.message)
                        }

                    })
                }
            }
            R.id.download_form_button -> {
                val formId: String = downloadFormsInput.text.toString().trim()
                if (formId.isNotBlank()) {
                    progressBar.visibility = View.VISIBLE
                    networkInteractor.downloadFormById(formId, object : FileDownloadListener {
                        override fun onProgress(progress: Int) {
                        }

                        override fun onComplete(downloadedFile: File) {
                            progressBar.visibility = View.INVISIBLE
                            showToast("Download Complete")
                        }

                        override fun onCancelled(exception: Exception) {
                            progressBar.visibility = View.INVISIBLE
                            showToast(exception.message)
                        }

                    })
                }
            }
            R.id.delete_form_button -> {
                val formId: String = deleteFormsInput.text.toString().trim()
                if (formId.isNotBlank()) {
                    progressBar.visibility = View.VISIBLE
                    formsDatabaseInteractor.deleteByFormId(formId)
                    showToast("$formId Form Deleted")
                    progressBar.visibility = View.INVISIBLE
                }
            }
            R.id.download_all_forms -> {
                progressBar.visibility = View.VISIBLE
                networkInteractor.downloadRequiredForms(object: FileDownloadListener {
                    override fun onProgress(progress: Int) {
                    }
                    override fun onComplete(downloadedFile: File) {
                        progressBar.visibility = View.INVISIBLE
                        runOnUiThread{
                            showToast("Downloaded all forms!")
                        }
                    }

                    override fun onCancelled(exception: Exception) {
                        progressBar.visibility = View.INVISIBLE
                        Timber.e(exception)
                        runOnUiThread{ showToast(exception.message) }
                    }

                })
            }
            R.id.clear_all_forms -> {
                progressBar.visibility = View.VISIBLE
                formsDatabaseInteractor.clearDatabase()
                showToast("All Forms Cleared!")
                progressBar.visibility = View.INVISIBLE
            }
            R.id.show_all_forms -> {
                startActivity(Intent(this, FormsListActivity::class.java))
            }
            R.id.open_saved_form_button -> {
                val formId: String = openSavedInput.text.toString().trim()
                if (formId.isNotBlank()) {
                    progressBar.visibility = View.VISIBLE
                    odkInteractor.openSavedForm(formId, context, object : FormsProcessListener {
                        override fun onProcessed() {
                            progressBar.visibility = View.INVISIBLE
                        }

                        override fun onProcessingError(e: Exception) {
                            progressBar.visibility = View.INVISIBLE
                            Timber.e(e)
                            showToast(e.message)
                        }

                    })
                }
            }
        }
    }

    fun showToast(text: String?) {
        if (text != null)
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}