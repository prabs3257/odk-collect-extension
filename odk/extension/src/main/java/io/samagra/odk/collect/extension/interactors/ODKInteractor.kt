package io.samagra.odk.collect.extension.interactors

import android.content.Context
import io.samagra.odk.collect.extension.listeners.FormsProcessListener
import io.samagra.odk.collect.extension.listeners.ODKProcessListener

/** Main interface to interact with ODK.
 *  Typically this interface should be used to carry out all the
 *  operations instead of using sub components.
 */
interface ODKInteractor {

    /** Sets up the odk according to a given configHandler.
     *  If [lazyDownload] is set to true, it will **not** download
     *  all the available forms right away else, it will download
     *  all the forms along with setup. This method **MUST** be called
     *  before calling any other method. */
    fun setupODK(settingsJson: String, lazyDownload: Boolean, listener: ODKProcessListener)

    /** Resets everything in odk and deletes all data. */
    fun resetODK(listener: ODKProcessListener)

    /** Opens the latest version related to the formId. Deletes any
     *  saved instance of a form with this particular formId. */
    fun openForm(formId: String, context: Context, listener: FormsProcessListener)

    /** Opens a saved form. If no saved instance is found, opens a new form. */
    fun openSavedForm(formId: String, context: Context, listener: FormsProcessListener)
}
