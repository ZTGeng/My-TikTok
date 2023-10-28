package com.ztgeng.mytiktok

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import com.ztgeng.mytiktok.utils.PreferencesHelper

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // 获取PreferenceCategory
        val backButtonCategory = findPreference<PreferenceCategory>("back_button_category")

        // 创建返回按钮Preference
        val backButtonPreference = Preference(requireContext()).apply {
            key = "back_button"
            title = resources.getString(R.string.back_button)
            isSelectable = true
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                // 返回到上一个Fragment或Activity
                parentFragmentManager.popBackStack()
                true
            }
        }

        // 将返回按钮Preference添加到PreferenceCategory
        backButtonCategory?.addPreference(backButtonPreference)

        val serverIpPreference = findPreference<EditTextPreference>("server_ip")
        serverIpPreference?.setOnPreferenceChangeListener { preference, newValue ->
            PreferencesHelper.serverIp = newValue.toString()
            preference.summary = PreferencesHelper.serverIp
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        return view
    }

    override fun onResume() {
        super.onResume()
        val serverIpPreference = findPreference<EditTextPreference>("server_ip")
        serverIpPreference?.summary = PreferencesHelper.serverIp
    }
}