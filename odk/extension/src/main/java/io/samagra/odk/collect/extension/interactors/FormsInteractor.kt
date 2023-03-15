package io.samagra.odk.collect.extension.interactors

import android.content.Context
import io.samagra.odk.collect.extension.listeners.FormsProcessListener
import org.odk.collect.forms.Form

/** FormsInteractor Interface provides methods to interact with ODK forms. Developers can use this
 * interface to open a form with a specific form ID or MD5 hash and also pre-fill form values.
 *
 * @author Chinmoy Chakraborty
 */
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
