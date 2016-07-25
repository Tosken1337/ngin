package com.tosken.ngin.oculus;

import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.ovr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.ovr.OVR.*;
import static org.lwjgl.ovr.OVRErrorCode.ovrSuccess;
import static org.lwjgl.ovr.OVRKeys.OVR_KEY_EYE_HEIGHT;
import static org.lwjgl.ovr.OVRUtil.ovr_Detect;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;

/**
 * Created by Sebastian Greif on 12.07.2016.
 * Copyright di support 2016
 */
public class OculusHmd {
    private static final Logger log = LoggerFactory.getLogger(OculusHmd.class);


    private long session;
    private OVRSessionStatus sessionStatus;
    private OVRHmdDesc hmdDesc;
    private int resolutionW;
    private int resolutionH;
    private float canvasRatio;

    private final OVRMatrix4f[] projections = new OVRMatrix4f[2];
    private final OVRFovPort fovPorts[] = new OVRFovPort[2];
    private final OVRPosef eyePoses[] = new OVRPosef[2];
    private final OVREyeRenderDesc eyeRenderDesc[] = new OVREyeRenderDesc[2];
    private OVRLayerEyeFov layer0;
    private int textureW;
    private int textureH;
    private Vector3f playerEyePos;


    public void init() throws Exception {
        OVRDetectResult detect = OVRDetectResult.calloc();
        ovr_Detect(0, detect);
        log.info("OVRDetectResult.IsOculusHMDConnected = " + detect.IsOculusHMDConnected());
        log.info("OVRDetectResult.IsOculusServiceRunning = " + detect.IsOculusServiceRunning());
        detect.free();
        if (!detect.IsOculusHMDConnected()) {
            return;
        }


        OVRInitParams initParams = OVRInitParams.calloc();
        final int result = OVR.novr_Initialize(initParams.address());

        if (OVRErrorCode.OVR_FAILURE(result)) {
            throw new Exception("Unable to initialize ovr. Result code " + result);
        }

        log.info("OVR SDK " + OVR.ovr_GetVersionString());
        initParams.free();

        PointerBuffer pHmd = memAllocPointer(1);
        OVRGraphicsLuid luid = OVRGraphicsLuid.calloc();
        if (ovr_Create(pHmd, luid) != ovrSuccess) {
            throw new Exception("Unable to initialize ovr");
        }


        session = pHmd.get(0);
        memFree(pHmd);
        luid.free();
        sessionStatus = OVRSessionStatus.calloc();

        hmdDesc = OVRHmdDesc.malloc();
        ovr_GetHmdDesc(session, hmdDesc);
        log.info("ovr_GetHmdDesc = " + hmdDesc.ManufacturerString() + " " + hmdDesc.ProductNameString() + " " + hmdDesc.SerialNumberString() + " " + hmdDesc.Type());
        if(hmdDesc.Type() == ovrHmd_None) {
            return;
        }

        resolutionW = hmdDesc.Resolution().w();
        resolutionH = hmdDesc.Resolution().h();
        canvasRatio = (float)resolutionW/resolutionH;
        log.debug("hmd resolution W=" + resolutionW + ", H=" + resolutionH);
        if (resolutionW == 0) {
            System.exit(0);
        }

        // FOV
        for (int eye = 0; eye < 2; eye++) {
            fovPorts[eye] = hmdDesc.DefaultEyeFov(eye);
            log.debug("eye "+eye+" = "+fovPorts[eye].UpTan() +", "+ fovPorts[eye].DownTan()+", "+fovPorts[eye].LeftTan()+", "+fovPorts[eye].RightTan());
        }

        playerEyePos = new Vector3f(0.0f, -ovr_GetFloat(session, OVR_KEY_EYE_HEIGHT, 1.65f), 0.0f);

        // projections
        for (int eye = 0; eye < 2; eye++) {
            projections[eye] = OVRMatrix4f.malloc();
            OVRUtil.ovrMatrix4f_Projection(fovPorts[eye], 0.1f, 500f, OVRUtil.ovrProjection_None, projections[eye]);
        }

        // render desc
        for (int eye = 0; eye < 2; eye++) {
            eyeRenderDesc[eye] = OVREyeRenderDesc.malloc();
            ovr_GetRenderDesc(session, eye,  fovPorts[eye], eyeRenderDesc[eye]);
            log.debug("ipd eye "+eye+" = "+eyeRenderDesc[eye].HmdToEyeOffset().x());
        }

        ovr_RecenterTrackingOrigin(session);
    }

    public int getResolutionW() {
        return resolutionW;
    }

    public int getResolutionH() {
        return resolutionH;
    }
}
