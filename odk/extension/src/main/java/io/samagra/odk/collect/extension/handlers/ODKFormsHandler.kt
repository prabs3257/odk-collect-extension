package io.samagra.odk.collect.extension.handlers

import android.content.Context
import android.content.Intent
import android.util.Log
import io.samagra.odk.collect.extension.interactors.FormsDatabaseInteractor
import io.samagra.odk.collect.extension.interactors.FormsInteractor
import io.samagra.odk.collect.extension.listeners.FormsProcessListener
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.forms.Form
import org.w3c.dom.Document
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class ODKFormsHandler @Inject constructor(
        private val currentProjectProvider: CurrentProjectProvider,
        private val formsDatabaseInteractor: FormsDatabaseInteractor
): FormsInteractor {

    override fun openFormWithFormId(formId: String, context: Context) {
        val form = formsDatabaseInteractor.getLatestFormById(formId)
        // Note: If the given form does not exist, it is the responsibility
        // of the caller to download it.
        if (form == null) {
            Log.e("FORMS ERROR", "The given formId does not exist!")
            return
        }
        openForm(form, context)
    }

    override fun openFormWithMd5Hash(md5Hash: String, context: Context) {
        val form = formsDatabaseInteractor.getFormByMd5Hash(md5Hash)
        if (form == null) {
            Log.e("FORMS ERROR", "The given formId does not exist!")
            return
        }
        openForm(form, context)
    }

    private fun openForm(form: Form, context: Context) {
        val contentUri = FormsContract.getUri(currentProjectProvider.getCurrentProject().uuid, form.dbId)
        val formEntryIntent = Intent(context, FormEntryActivity::class.java)
        formEntryIntent.action = Intent.ACTION_EDIT
        formEntryIntent.data = contentUri
        formEntryIntent.putExtra(
            ApplicationConstants.BundleKeys.FORM_MODE,
            ApplicationConstants.FormModes.EDIT_SAVED
        )
        context.startActivity(formEntryIntent)
    }

    override fun updateForm(form: Form, tag: String, tagValue: String, listener: FormsProcessListener?) {
        val formPath: String = form.formFilePath
        var fos: FileOutputStream? = null
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document = builder.parse(File(formPath))
            document.documentElement.normalize()
            if (document != null) {
                updateDocumentBasedOnTag(document, tag, tagValue)
                val transformerFactory = TransformerFactory.newInstance()
                val transformer = transformerFactory.newTransformer()
                val source = DOMSource(document)
                fos = FileOutputStream(File(formPath))
                val result = StreamResult(fos)
                transformer.transform(source, result)
            }
        } catch (e: Exception) {
            listener?.onProcessingError(e)
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                    listener?.onProcessed()
                } catch (e: IOException) {
                    listener?.onProcessingError(e)
                }
            }
        }
    }

    override fun updateForm(
        form: Form,
        values: HashMap<String, String>,
        listener: FormsProcessListener?
    ) {
        var progress = 0
        for (entry in values.entries) {
            updateForm(form, entry.key, entry.value, object : FormsProcessListener {
                override fun onProcessed() {
                    progress++
                    if (progress == values.size)
                        listener?.onProcessed()
                }
                override fun onProcessingError(e: Exception) {
                    listener?.onProcessingError(e)
                }
            })
        }
    }

    private fun updateDocumentBasedOnTag(document: Document, tag: String, tagValue: String): Document {
        try {
            if (document.getElementsByTagName(tag).item(0).childNodes.length > 0)
                document.getElementsByTagName(tag)
                    .item(0)
                    .childNodes
                    .item(0)
                    .nodeValue = tagValue
            else
                document.getElementsByTagName(tag).item(0).appendChild(document.createTextNode(tagValue))
        } catch (e: java.lang.Exception) {
            return document
        }
        return document
    }
}
