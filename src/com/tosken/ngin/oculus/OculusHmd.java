package com.tosken.ngin.oculus;

import com.tosken.ngin.gl.FrameBufferObject;
import com.tosken.ngin.gl.Texture;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.ovr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.ovr.OVR.*;
import static org.lwjgl.ovr.OVRErrorCode.ovrError_DisplayLost;
import static org.lwjgl.ovr.OVRErrorCode.ovrSuccess;
import static org.lwjgl.ovr.OVRUtil.ovr_Detect;
import static org.lwjgl.ovr.OVRUtil.ovr_GetEyePoses;
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

    private final OVRMatrix4f[] projections = new OVRMatrix4f[2];
    private final OVRFovPort fovPorts[] = new OVRFovPort[2];
    private final OVRPosef eyePoses[] = new OVRPosef[2];
    private OVREyeRenderDesc eyeRenderDesc[];
    private OVRVector3f.Buffer hmdToEyeOffsets = OVRVector3f.calloc(2);
    private OVRLayerEyeFov vrEyesLayer;
    private int textureW;
    private int textureH;
    private long swapChain;

    private FrameBufferObject[] swapChainFbo;
    private PointerBuffer layers;
    private boolean recenter;

    FrameBufferObject mirrorTextureFbo;
    private long mirrorTextureChain;


    public void init() throws Exception {
        OVRDetectResult detect = OVRDetectResult.calloc();
        ovr_Detect(0, detect);
        log.info("IsOculusHMDConnected = " + detect.IsOculusHMDConnected());
        log.info("IsOculusServiceRunning = " + detect.IsOculusServiceRunning());
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
        log.info("HMD -> " + hmdDesc.ManufacturerString() + " " + hmdDesc.ProductNameString() + " " + hmdDesc.SerialNumberString() + " " + hmdDesc.Type());
        if(hmdDesc.Type() == ovrHmd_None) {
            return;
        }

        resolutionW = hmdDesc.Resolution().w();
        resolutionH = hmdDesc.Resolution().h();
        log.debug("HMD resolution W=" + resolutionW + ", H=" + resolutionH);
        if (resolutionW == 0) {
            throw new Exception("Invalid hmd resolution " + resolutionW);
        }

        // FOV
        for (int eye = 0; eye < 2; eye++) {
            fovPorts[eye] = hmdDesc.DefaultEyeFov(eye);
            log.debug("eye "+eye+" = "+fovPorts[eye].UpTan() +", "+ fovPorts[eye].DownTan()+", "+fovPorts[eye].LeftTan()+", "+fovPorts[eye].RightTan());
        }

        // projections
        for (int eye = 0; eye < 2; eye++) {
            projections[eye] = OVRMatrix4f.malloc();
            OVRUtil.ovrMatrix4f_Projection(fovPorts[eye], 0.1f, 500f, OVRUtil.ovrProjection_None, projections[eye]);
        }

        // render desc
        updateRenderDescription();
        log.debug("HmdToEyeOffset: {}, {}", eyeRenderDesc[0].HmdToEyeOffset(), eyeRenderDesc[1].HmdToEyeOffset());

        ovr_RecenterTrackingOrigin(session);
    }

    private void updateRenderDescription() {
        if (eyeRenderDesc == null) {
            eyeRenderDesc = new OVREyeRenderDesc[2];
            eyeRenderDesc[ovrEye_Left] = OVREyeRenderDesc.malloc();
            eyeRenderDesc[ovrEye_Right] = OVREyeRenderDesc.malloc();
        }

        for (int eye = 0; eye < 2; eye++) {
            ovr_GetRenderDesc(session, eye, fovPorts[eye], eyeRenderDesc[eye]);
            hmdToEyeOffsets.put(eye, eyeRenderDesc[eye].HmdToEyeOffset());
        }
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
            texture.setParameters(GL11.GL_LINEAR, GL13.GL_CLAMP_TO_BORDER);

            swapChainFbo[i] = FrameBufferObject.create();
            swapChainFbo[i].addColorAttachment(texture, 0);
            swapChainFbo[i].addDefaultDepthStencil(textureW, textureH);
        }


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

        /*viewport[0] = OVRRecti.calloc();
        viewport[0].Pos().x(0);
        viewport[0].Pos().y(0);
        viewport[0].Size().w(textureW);
        viewport[0].Size().h(textureH);

        viewport[1] = OVRRecti.calloc();
        viewport[1].Pos().x(0);
        viewport[1].Pos().y(0);
        viewport[1].Size().w(textureW);
        viewport[1].Size().h(textureH);*/


        // single layer to present a VR scene
        vrEyesLayer = OVRLayerEyeFov.calloc();
        vrEyesLayer.Header().Type(ovrLayerType_EyeFov);
        vrEyesLayer.Header().Flags(ovrLayerFlag_TextureOriginAtBottomLeft);
        for (int eye = 0; eye < 2; eye++) {
            vrEyesLayer.ColorTexture(eye, swapChain);
            vrEyesLayer.Viewport(eye, viewport[eye]);
            vrEyesLayer.Fov(eye, fovPorts[eye]);

        }
        viewport[0].free();
        viewport[1].free();

        layers = BufferUtils.createPointerBuffer(1);
        layers.put(0, vrEyesLayer);

        // FloorLevel will give tracking poses where the floor height is 0
        //ovr_SetTrackingOriginType(session, ovrTrackingOrigin_FloorLevel);
    }

    public boolean update() {
        /*ovr_GetSessionStatus(session, sessionStatus);
        if  (!sessionStatus.IsVisible() || sessionStatus.ShouldQuit()) {
            return false;
        }*/

        if (sessionStatus.ShouldRecenter() || recenter) {
            ovr_RecenterTrackingOrigin(session);
            recenter = false;
        }

        // Call ovr_GetRenderDesc each frame to get the ovrEyeRenderDesc, as the returned values (e.g. HmdToEyeOffset) may change at runtime.
        updateRenderDescription();




        // Version 1 of how to get the eye poses

        // Get both eye poses simultaneously, with IPD offset already included.
        // displayMidpointSeconds is the time when the frame will be presented to the user on the hmd (compensate latency)
        /*double displayMidpointSeconds  = ovr_GetPredictedDisplayTime(session, 0);
        OVRTrackingState hmdState = OVRTrackingState.malloc();
        ovr_GetTrackingState(session, displayMidpointSeconds, true, hmdState);

        // get head pose and free hmdState
        OVRPosef headPose = hmdState.HeadPose().ThePose();
        hmdState.free();

        //calculate eye poses
        OVRPosef.Buffer outEyePoses = OVRPosef.create(2);
        OVRUtil.ovr_CalcEyePoses(headPose, hmdToEyeOffsets, outEyePoses);*/








        // Version 2 of how to get the eye poses
        OVRPosef.Buffer outEyePoses = OVRPosef.create(2);
        DoubleBuffer outSensorSampleTime = BufferUtils.createDoubleBuffer(1);
        ovr_GetEyePoses(session, 0, true, hmdToEyeOffsets, outEyePoses, outSensorSampleTime);
        vrEyesLayer.SensorSampleTime(outSensorSampleTime.get());


        eyePoses[ovrEye_Left] = outEyePoses.get(ovrEye_Left);
        eyePoses[ovrEye_Right] = outEyePoses.get(ovrEye_Right);
        vrEyesLayer.RenderPose(ovrEye_Left, eyePoses[ovrEye_Left]);
        vrEyesLayer.RenderPose(ovrEye_Right, eyePoses[ovrEye_Right]);


        final OVRVector3f positionL = eyePoses[ovrEye_Left].Position();
        final OVRVector3f positionR = eyePoses[ovrEye_Right].Position();
        //log.debug("L x: {}, y: {}, z:{} ---- R x: {}, y: {}, z:{}", positionL.x(), positionL.y(), positionL.z(), positionR.x(), positionR.y(), positionR.z());

        return true;
    }

    public void commitFrame() {
        ovr_CommitTextureSwapChain(session, swapChain);
    }

    public boolean endFrame() {
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

    public Matrix4f getProjectionMatrix(final int eye) {
        return new Matrix4f(projections[eye].M()).transpose();
    }

    public Matrix4f getViewMatrix(final int eye) {
        final OVRPosef eyePose = eyePoses[eye];

        /*Matrix4f mat = new Matrix4f();
        mat.identity();

        Quaternionf orientation = new Quaternionf(eyePose.Orientation().x(), eyePose.Orientation().y(), eyePose.Orientation().z(), eyePose.Orientation().w());
        orientation.invert();
        mat.rotate(orientation);

        Vector3f position = new Vector3f(-eyePose.Position().x(), -eyePose.Position().y(), -eyePose.Position().z());
        mat.translate(position);

        Vector3f scenePlayerPosition = new Vector3f(0, 0, -2);
        mat.translate(scenePlayerPosition);

        return mat;*/



        // player / head position and rotation in the scene (@TODO should be fetched from application)
        Matrix4f playerRotation = new Matrix4f();
        playerRotation.identity();
        Vector3f playerPosition = new Vector3f(0, 0, 2);

        // Current eye position
        Vector3f eyePosition = new Vector3f(eyePose.Position().x(), eyePose.Position().y(), eyePose.Position().z()).mul(1);

        // Transform current eye orientation to matrix
        Quaternionf eyeOrientation = new Quaternionf(eyePose.Orientation().x(), eyePose.Orientation().y(), eyePose.Orientation().z(), eyePose.Orientation().w());
        //eyeOrientation.invert();
        Matrix4f eyeOrientationM = new Matrix4f();
        eyeOrientationM = eyeOrientation.get(eyeOrientationM);

        // Compute absolute eye position and rotation
        Vector3f transformedEyePos = playerPosition.add(playerRotation.transformPosition(eyePosition));
        Matrix4f transformedEyeRot = playerRotation.mul(eyeOrientationM);

        final Vector3f up = transformedEyeRot.transformDirection(new Vector3f(0, 1, 0));
        final Vector3f forward = transformedEyeRot.transformDirection(new Vector3f(0, 0, -1));
        final Vector3f lookAt = new Vector3f(transformedEyePos).add(forward);

        final Matrix4f viewMatrix = new Matrix4f()
                .lookAt(transformedEyePos, lookAt, up);


        return viewMatrix;

    }

    public OVRRecti getViewport(final int eye) {
        return vrEyesLayer.Viewport(eye);
    }

    public void recenter() {
        recenter = true;
    }

    public void destroy() {
        for (int eye = 0; eye < 2; eye++) {
            projections[eye].free();
        }
        for (int eye = 0; eye < 2; eye++) {
            eyeRenderDesc[eye].free();
        }

        vrEyesLayer.free();
        sessionStatus.free();

        if (swapChain != 0) {
            ovr_DestroyTextureSwapChain(session, swapChain);
        }

        if (mirrorTextureFbo != null && mirrorTextureChain != 0) {
            ovr_DestroyMirrorTexture(session, mirrorTextureChain);
        }

        ovr_Destroy(session);
        ovr_Shutdown();
    }

    public FrameBufferObject getCurrentFrameBuffer() {
        IntBuffer currentIndexB = BufferUtils.createIntBuffer(1);
        ovr_GetTextureSwapChainCurrentIndex(session, swapChain, currentIndexB);
        int index = currentIndexB.get();
        return swapChainFbo[index];
    }

    public FrameBufferObject getMirrorTexture(final int windowW, final int windowH) {
        if (mirrorTextureFbo == null) {
            // Create mirror texture and an FBO used to copy mirror texture to back buffer
            PointerBuffer outMirrorTexture = BufferUtils.createPointerBuffer(1);
            OVRMirrorTextureDesc desc = OVRMirrorTextureDesc.calloc()
                    .Format(OVR_FORMAT_R8G8B8A8_UNORM_SRGB)
                    .Width(windowW)
                    .Height(windowH);
            int result = OVRGL.ovr_CreateMirrorTextureGL(session, desc, outMirrorTexture);
            if (result != ovrSuccess) {
                log.warn("Error");
            }
            mirrorTextureChain = outMirrorTexture.get(0);

            final IntBuffer mirrorTextureId = BufferUtils.createIntBuffer(1);
            result = OVRGL.ovr_GetMirrorTextureBufferGL(session, mirrorTextureChain, mirrorTextureId);
            if (result != ovrSuccess) {
                OVRErrorInfo error = OVRErrorInfo.calloc();
                ovr_GetLastErrorInfo(error);
                final String s = error.ErrorStringString();
                log.warn(" " + s);
            }

            mirrorTextureFbo = FrameBufferObject.create(GL30.GL_READ_FRAMEBUFFER);
            mirrorTextureFbo.addColorAttachment(Texture.wrap(mirrorTextureId.get(0), windowW, windowH), 0);
            mirrorTextureFbo.addDefaultDepthStencil(windowW, windowH);
            if (!mirrorTextureFbo.isComplete()) {
                log.error("error");
            }
        }

        return mirrorTextureFbo;
    }
}
