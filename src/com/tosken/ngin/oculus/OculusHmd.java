package com.tosken.ngin.oculus;

import com.tosken.ngin.gl.FrameBufferObject;
import com.tosken.ngin.gl.Texture;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.ovr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;

import static org.lwjgl.ovr.OVR.*;
import static org.lwjgl.ovr.OVRErrorCode.ovrError_DisplayLost;
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
    OVRVector3f.Buffer hmdToEyeOffsets = OVRVector3f.calloc(2);
    private OVRLayerEyeFov vrEyesLayer;
    private int textureW;
    private int textureH;
    private Vector3f playerEyePos;
    private long swapChain;

    private FrameBufferObject[] swapChainFbo;
    private PointerBuffer layers;


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
            ovr_GetRenderDesc(session, eye, fovPorts[eye], eyeRenderDesc[eye]);
            hmdToEyeOffsets.put(eye, eyeRenderDesc[eye].HmdToEyeOffset());
        }

        ovr_RecenterTrackingOrigin(session);
    }

    public void initGL() {
        float pixelsPerDisplayPixel = 1.0f;

        OVRSizei leftTextureSize = OVRSizei.malloc();
        ovr_GetFovTextureSize(session, ovrEye_Left, fovPorts[ovrEye_Left], pixelsPerDisplayPixel, leftTextureSize);
        log.debug("leftTextureSize W="+leftTextureSize.w() +", H="+ leftTextureSize.h());

        OVRSizei rightTextureSize = OVRSizei.malloc();
        ovr_GetFovTextureSize(session, ovrEye_Right, fovPorts[ovrEye_Right], pixelsPerDisplayPixel, rightTextureSize);
        log.debug("rightTextureSize W="+rightTextureSize.w() +", H="+ rightTextureSize.h());

        textureW = leftTextureSize.w() + rightTextureSize.w();
        textureH = Math.max(leftTextureSize.h(), rightTextureSize.h());
        log.debug("request textureW=" + textureW + ", textureH=" + textureH);
        leftTextureSize.free();
        rightTextureSize.free();

        // TextureSets
        OVRTextureSwapChainDesc swapChainDesc = OVRTextureSwapChainDesc.calloc()
                .Type(ovrTexture_2D)
                .ArraySize(1)
                .Format(OVR_FORMAT_R8G8B8A8_UNORM_SRGB)
                .Width(textureW)
                .Height(textureH)
                .MipLevels(1)
                .SampleCount(1)
                .StaticImage(false);

        PointerBuffer textureSetPB = BufferUtils.createPointerBuffer(1);
        if (OVRGL.ovr_CreateTextureSwapChainGL(session, swapChainDesc, textureSetPB) != ovrSuccess) {
            throw new RuntimeException("Failed to create Swap Texture Set");
        }

        swapChain = textureSetPB.get(0);
        swapChainDesc.free();

        // create FrameBuffers for Oculus SDK generated textures
        int textureCount = 0; // texture count is the number of back buffers in the swap chain allowing double or dribble buffering.
        IntBuffer chainLengthB = BufferUtils.createIntBuffer(1);
        ovr_GetTextureSwapChainLength(session, textureSetPB.get(0), chainLengthB);
        textureCount = chainLengthB.get();
        log.debug("chain length="+textureCount);

        swapChainFbo = new FrameBufferObject[textureCount];
        for (int i = 0; i < textureCount; i++) {
            IntBuffer textureIdB = BufferUtils.createIntBuffer(1);
            // Get texture id of the texture
            OVRGL.ovr_GetTextureSwapChainBufferGL(session, swapChain, i, textureIdB);
            int textureId = textureIdB.get();
            log.debug("Creating fbo for swap chain texture {} using texture id {}", i, textureId);

            Texture texture = Texture.wrap(textureId, textureW, textureH);

            swapChainFbo[i] = FrameBufferObject.create();
            swapChainFbo[i].addColorAttachment(texture, 0);
            swapChainFbo[i].addDefaultDepthStencil(textureW, textureH);
        }


        // //@TODO check: Viewport: - The rectangle of the texture that is actually used, specified in 0-1 texture "UV" coordinate space (not pixels).
        // eye viewports
        OVRRecti viewport[] = new OVRRecti[2];
        viewport[0] = OVRRecti.calloc();
        viewport[0].Pos().x(0);
        viewport[0].Pos().y(0);
        viewport[0].Size().w(textureW / 2);
        viewport[0].Size().h(textureH);

        viewport[1] = OVRRecti.calloc();
        viewport[1].Pos().x(textureW / 2);
        viewport[1].Pos().y(0);
        viewport[1].Size().w(textureW / 2);
        viewport[1].Size().h(textureH);


        // single layer to present a VR scene
        //@TODO check: Viewport: - The rectangle of the texture that is actually used, specified in 0-1 texture "UV" coordinate space (not pixels).
        vrEyesLayer = OVRLayerEyeFov.calloc();
        vrEyesLayer.Header().Type(ovrLayerType_EyeFov);
        vrEyesLayer.Header().Flags(ovrLayerFlag_TextureOriginAtBottomLeft);
        for (int eye = 0; eye < 2; eye++) {
            vrEyesLayer.ColorTexture(textureSetPB);
            //vrEyesLayer.ColorTexture(eye, swapChain);
            vrEyesLayer.Viewport(eye, viewport[eye]);
            vrEyesLayer.Fov(eye, fovPorts[eye]);

            viewport[eye].free();
        }

        layers = BufferUtils.createPointerBuffer(1);
        layers.put(0, vrEyesLayer);
    }

    public boolean update() {
        /*ovr_GetSessionStatus(session, sessionStatus);
        if  (!sessionStatus.IsVisible() || sessionStatus.ShouldQuit()) {
            return false;
        }*/

        if (sessionStatus.ShouldRecenter()) {
            ovr_RecenterTrackingOrigin(session);
        }



        // Get both eye poses simultaneously, with IPD offset already included.
        // displayMidpointSeconds is the time when the frame will be presented to the user on the hmd (compensate latency)
        double displayMidpointSeconds  = ovr_GetPredictedDisplayTime(session, 0);
        OVRTrackingState hmdState = OVRTrackingState.malloc();
        ovr_GetTrackingState(session, displayMidpointSeconds, true, hmdState);

        // get head pose and free hmdState
        OVRPosef headPose = hmdState.HeadPose().ThePose();
        hmdState.free();

        //calculate eye poses
        OVRPosef.Buffer outEyePoses = OVRPosef.create(2);
        OVRUtil.ovr_CalcEyePoses(headPose, hmdToEyeOffsets, outEyePoses);

        eyePoses[ovrEye_Left] = outEyePoses.get(0);
        eyePoses[ovrEye_Right] = outEyePoses.get(1);
        vrEyesLayer.RenderPose(ovrEye_Left, eyePoses[ovrEye_Left]);
        vrEyesLayer.RenderPose(ovrEye_Right, eyePoses[ovrEye_Right]);

        final OVRVector3f position = eyePoses[ovrEye_Left].Position();
        log.debug("Eye position x: {}, y: {}, z:{}", position.x(), position.y(), position.z());

        return true;
    }

    public boolean endFrame() {
        ovr_CommitTextureSwapChain(session, swapChain);
        int result = ovr_SubmitFrame(session, 0, null, layers);
        if (result == ovrError_DisplayLost) {
            throw new RuntimeException("Display lost. Need to recreate it");
        }
        return result == ovrSuccess;
    }

    public int getResolutionW() {
        return resolutionW;
    }

    public int getResolutionH() {
        return resolutionH;
    }

    public OVRRecti getViewport(final int eye) {
        return vrEyesLayer.Viewport(eye);
    }

    public void destroy() {
        //@TODO destroy swap chain and free fields
    }

    public FrameBufferObject getCurrentFrameBuffer() {
        IntBuffer currentIndexB = BufferUtils.createIntBuffer(1);
        ovr_GetTextureSwapChainCurrentIndex(session, swapChain, currentIndexB);
        int index = currentIndexB.get();
        return swapChainFbo[index];
    }
}
