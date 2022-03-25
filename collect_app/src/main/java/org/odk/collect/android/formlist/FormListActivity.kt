package org.odk.collect.android.formlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.network.NetworkStateProvider
import org.odk.collect.android.utilities.SnackbarUtils
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class FormListActivity : LocalizedActivity() {

    @Inject
    lateinit var viewModelFactory: FormListViewModel.Factory

    @Inject
    lateinit var networkStateProvider: NetworkStateProvider

    private val viewModel: FormListViewModel by viewModels { viewModelFactory }

    private lateinit var menuDelegate: FormListMenuDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_form_list)
        title = getString(R.string.enter_data)
        setSupportActionBar(findViewById(R.id.toolbar))

        menuDelegate = FormListMenuDelegate(this, viewModel, networkStateProvider)

        val formListAdapter = FormListAdapter()
        findViewById<RecyclerView>(R.id.formList).apply {
            adapter = formListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        viewModel.showProgressBar.observe(this) { shouldShowProgressBar ->
            findViewById<ProgressBar>(R.id.progressBar).visibility =
                if (shouldShowProgressBar.value) View.VISIBLE
                else View.GONE
        }

        viewModel.syncResult.observe(this) { result ->
            SnackbarUtils.showShortSnackbar(findViewById(R.id.formList), result.value)
        }

        viewModel.forms.observe(this) { forms ->
            formListAdapter.setData(forms.value)
        }

        viewModel.sortingOrder.observe(this) { sortingOrder ->
            formListAdapter.sort(sortingOrder)
        }

        viewModel.filterText.observe(this) { filterText ->
            formListAdapter.filter(filterText)
        }

        viewModel.fetchForms()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuDelegate.onCreateOptionsMenu(menuInflater, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menuDelegate.onPrepareOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return menuDelegate.onOptionsItemSelected(item)
    }
}
