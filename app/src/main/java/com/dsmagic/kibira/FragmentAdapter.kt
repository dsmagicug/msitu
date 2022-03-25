package com.dsmagic.kibira

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import java.util.ArrayList

class FragmentAdapter (fm:FragmentManager): FragmentStatePagerAdapter(fm
    ) {
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleLIst = ArrayList<String>()

    override fun getCount(): Int {
       return  mFragmentList.size
    }

    override fun getItem(position: Int): Fragment {

       return mFragmentList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mFragmentTitleLIst[position]
    }

    fun addFragment(fragment: Fragment,title: String){
        mFragmentList.add(fragment)
        mFragmentTitleLIst.add(title)
    }
}
