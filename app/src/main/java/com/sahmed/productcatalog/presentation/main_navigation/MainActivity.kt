package com.sahmed.productcatalog.presentation.main_navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayoutMediator
import com.sahmed.productcatalog.R
import com.sahmed.productcatalog.framework.di.DaggerAppComponent
import com.sahmed.productcatalog.framework.network.dto.Product
import com.sahmed.productcatalog.presentation.filter.FilterScreen
import com.sahmed.productcatalog.presentation.product_list.ProductListFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.shimmer_layout.*
import kotlinx.android.synthetic.main.view_search.*
import javax.inject.Inject

class MainActivity : AppCompatActivity(),
    FilterScreen.FilterInterface {

    // List for Applied filters, will be utilized to pass again in Filter screen to show pre
    //selected check boxes
    private var appliedFilters = mutableListOf<String>()

    @Inject
    lateinit var mainViewModel: MainViewModel

    private lateinit var filterScreen : FilterScreen

    private var priceMinSelected = FilterScreen.DEFAULT_MIN_VALUE // will be overridden in onFiltersApplied
    private var priceMaxSelected = FilterScreen.DEFAULT_MAX_VALUE // will be overridden in onFiltersApplied

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DaggerAppComponent.factory().create(this).inject(this)

        setupSearch()
        observeData()
        setupFilters()

    }

    private fun observeData() {
        shimmer_parent.visibility = View.VISIBLE
        shimmer_parent.showShimmer(true)
        mainViewModel.getCatalog()
        mainViewModel.mappedData.observe(this,Observer<Map<String, List<Product>>> { groupedData->
            shimmer_parent.showShimmer(false)
            shimmer_parent.visibility = View.GONE
            setupPager(groupedData)

        })
    }

    private fun setupFilters() {

        icon_filter.setOnClickListener {
            filterScreen =
                FilterScreen()
            filterScreen.priceMinSelected = priceMinSelected
            filterScreen.priceMaxSelected = priceMaxSelected
            if(appliedFilters.isNotEmpty())filterScreen.queryList = appliedFilters

            filterScreen.show(supportFragmentManager,"")
        }
    }

    private fun setupSearch() {
        toolbar_search_input.addTextChangedListener ( object: TextWatcher {
            override fun afterTextChanged(inputView: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(chars: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val query = chars.toString().trim()
                mainViewModel.performSearch(query)


            }

        } )



    }



    /**Fun set up view pager and tabs according to data grouped by brand
     *
     */
    private fun setupPager(groupedData: Map<String, List<Product>>) {
        val adapter =
            TabsAdapter(
                supportFragmentManager,
                lifecycle
            )
        var list = mutableListOf<FragmentContainer>()

        groupedData.keys.forEach{
            var container  =
                FragmentContainer(
                    ProductListFragment(),
                    it,
                    groupedData.get(it)!!
                )
            list.add(container)
        }
        pager.let {
            adapter.list = list
            it.adapter = adapter
        }

        TabLayoutMediator(tab_layout, pager) { tab, position ->
            tab.text = list.get(position).title
        }.attach()
    }

    //Filter Screen/FilterInterface
    override fun onFiltersApplied(
        queryList: MutableList<String>?,
        priceRangeMinValue: Int,
        priceRangeMaxValue: Int
    ) {
        indicator.visibility = View.VISIBLE
        priceMinSelected = priceRangeMinValue
        priceMaxSelected = priceRangeMaxValue
        appliedFilters = queryList!!
        mainViewModel.filterData(appliedFilters,priceMinSelected,priceMaxSelected)

    }

    override fun onFiltersCleared() {
        indicator.visibility = View.GONE
        mainViewModel.clearFilteredData()
    }
}