package io.samagra.odk.collect.extension.interactors

import android.content.Context
import io.samagra.odk.collect.extension.listeners.FormsProcessListener
import org.odk.collect.forms.Form

/** Main interface to interact with forms. */
interface FormsInteractor {

    /** Opens the latest version related to the formId. */
    fun openFormWithFormId(formId: String, context: Context)

    /** Opens a form with the given md5 hash. */
    fun openFormWithMd5Hash(md5Hash: String, context: Context)

    /** Prefills the values of a form given a tag and value. */
    fun updateForm(form: Form, tag: String, tagValue: String, listener: FormsProcessListener?)

    /** Prefills the values of a form given a list of tags and values. */
    fun updateForm(form: Form, values: HashMap<String, String>,listener: FormsProcessListener?)
}
