package com.tosken.ngin.oculus;

import org.lwjgl.PointerBuffer;
import org.lwjgl.ovr.OVR;
import org.lwjgl.ovr.OVRErrorCode;
import org.lwjgl.ovr.OVRGraphicsLuid;
import org.lwjgl.ovr.OVRInitParams;

import static org.lwjgl.ovr.OVR.ovr_Create;
import static org.lwjgl.ovr.OVRErrorCode.ovrSuccess;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;

/**
 * Created by Sebastian Greif on 12.07.2016.
 * Copyright di support 2016
 */
public class OculusHmd {


    public void init() throws Exception {
        OVRInitParams initParams = OVRInitParams.calloc();

        final int result = OVR.novr_Initialize(initParams.address());

        if (OVRErrorCode.OVR_FAILURE(result)) {
            throw new Exception("Unable to initialize ovr. Result code " + result);
        }

        System.out.println("OVR SDK " + OVR.ovr_GetVersionString());
        initParams.free();

        PointerBuffer pHmd = memAllocPointer(1);
        OVRGraphicsLuid luid = OVRGraphicsLuid.calloc();
        if (ovr_Create(pHmd, luid) != ovrSuccess) {
            System.out.println("create failed, try debug");
            //debug headset is now enabled via the Oculus Configuration util . tools -> Service -> Configure
            return;
        }

    }
}
