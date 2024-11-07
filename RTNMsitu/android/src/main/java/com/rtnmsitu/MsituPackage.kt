package com.rtnmsitu;

import MsituModule

import com.facebook.react.TurboReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.module.model.ReactModuleInfo
import com.facebook.react.module.model.ReactModuleInfoProvider

class MsituPackage : TurboReactPackage() {

    override fun getModule(name: String, reactContext: ReactApplicationContext): NativeModule? {
    return if (name == MsituModule.NAME) {
        MsituModule(reactContext)
    } else {
        null
    }
    }

    override fun getReactModuleInfoProvider(): ReactModuleInfoProvider {
        return ReactModuleInfoProvider {
            mapOf(
                MsituModule.NAME to ReactModuleInfo(
                    MsituModule.NAME,
                    MsituModule.NAME,
                    canOverrideExistingModule = false,
                    needsEagerInit = false,
                    hasConstants = true,
                    isCxxModule = false,
                    isTurboModule = true
                )
            )
        }
    }


}