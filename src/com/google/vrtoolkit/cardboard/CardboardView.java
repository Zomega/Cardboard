package com.google.vrtoolkit.cardboard;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowManager;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CardboardView extends GLSurfaceView {
	private static final String TAG = "CardboardView";
	private static final float DEFAULT_Z_NEAR = 0.1F;
	private static final float DEFAULT_Z_FAR = 100.0F;
	private RendererHelper mRendererHelper;
	private HeadTracker mHeadTracker;
	private HeadMountedDisplay mHmd;
	private DistortionRenderer mDistortionRenderer;
	private CardboardDeviceParamsObserver mCardboardDeviceParamsObserver;
	private boolean mVRMode = true;
	private volatile boolean mDistortionCorrectionEnabled = true;
	private volatile float mDistortionCorrectionScale = 1.0F;
	private float mZNear = 0.1F;
	private float mZFar = 100.0F;

	public CardboardView(Context context) {
		super(context);
		init(context);
	}

	public CardboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void setRenderer(Renderer renderer) {
		this.mRendererHelper = (renderer != null ? new RendererHelper(renderer)
				: null);
		super.setRenderer(this.mRendererHelper);
	}

	public void setRenderer(StereoRenderer renderer) {
		setRenderer(renderer != null ? new StereoRendererHelper(renderer)
				: (Renderer) null);
	}

	public void setVRModeEnabled(boolean enabled) {
		this.mVRMode = enabled;
		if (this.mRendererHelper != null) {
			this.mRendererHelper.setVRModeEnabled(enabled);
		}
	}

	public boolean getVRMode() {
		return this.mVRMode;
	}

	public HeadMountedDisplay getHeadMountedDisplay() {
		return this.mHmd;
	}

	public void updateCardboardDeviceParams(
			CardboardDeviceParams cardboardDeviceParams) {
		if ((cardboardDeviceParams == null)
				|| (cardboardDeviceParams.equals(this.mHmd.getCardboard()))) {
			return;
		}
		if (this.mCardboardDeviceParamsObserver != null) {
			this.mCardboardDeviceParamsObserver
					.onCardboardDeviceParamsUpdate(cardboardDeviceParams);
		}
		this.mHmd.setCardboard(cardboardDeviceParams);
		if (this.mRendererHelper != null) {
			this.mRendererHelper
					.setCardboardDeviceParams(cardboardDeviceParams);
		}
	}

	public void setCardboardDeviceParamsObserver(
			CardboardDeviceParamsObserver observer) {
		this.mCardboardDeviceParamsObserver = observer;
	}

	public CardboardDeviceParams getCardboardDeviceParams() {
		return this.mHmd.getCardboard();
	}

	public void updateScreenParams(ScreenParams screenParams) {
		if ((screenParams == null)
				|| (screenParams.equals(this.mHmd.getScreen()))) {
			return;
		}
		this.mHmd.setScreen(screenParams);
		if (this.mRendererHelper != null) {
			this.mRendererHelper.setScreenParams(screenParams);
		}
	}

	public ScreenParams getScreenParams() {
		return this.mHmd.getScreen();
	}

	public void setInterpupillaryDistance(float distance) {
		this.mHmd.getCardboard().setInterpupillaryDistance(distance);
		if (this.mRendererHelper != null) {
			this.mRendererHelper.setInterpupillaryDistance(distance);
		}
	}

	public float getInterpupillaryDistance() {
		return this.mHmd.getCardboard().getInterpupillaryDistance();
	}

	public void setFovY(float fovY) {
		this.mHmd.getCardboard().setFovY(fovY);
		if (this.mRendererHelper != null) {
			this.mRendererHelper.setFOV(fovY);
		}
	}

	public float getFovY() {
		return this.mHmd.getCardboard().getFovY();
	}

	public void setZPlanes(float zNear, float zFar) {
		this.mZNear = zNear;
		this.mZFar = zFar;
		if (this.mRendererHelper != null) {
			this.mRendererHelper.setZPlanes(zNear, zFar);
		}
	}

	public float getZNear() {
		return this.mZNear;
	}

	public float getZFar() {
		return this.mZFar;
	}

	public void setDistortionCorrectionEnabled(boolean enabled) {
		this.mDistortionCorrectionEnabled = enabled;
		if (this.mRendererHelper != null) {
			this.mRendererHelper.setDistortionCorrectionEnabled(enabled);
		}
	}

	public boolean getDistortionCorrectionEnabled() {
		return this.mDistortionCorrectionEnabled;
	}

	public void setDistortionCorrectionScale(float scale) {
		this.mDistortionCorrectionScale = scale;
		if (this.mRendererHelper != null) {
			this.mRendererHelper.setDistortionCorrectionScale(scale);
		}
	}

	public float getDistortionCorrectionScale() {
		return this.mDistortionCorrectionScale;
	}

	public void onResume() {
		if (this.mRendererHelper == null) {
			return;
		}
		super.onResume();
		this.mHeadTracker.startTracking();
	}

	public void onPause() {
		if (this.mRendererHelper == null) {
			return;
		}
		super.onPause();
		this.mHeadTracker.stopTracking();
	}

	public void setRenderer(GLSurfaceView.Renderer renderer) {
		throw new RuntimeException(
				"Please use the CardboardView renderer interfaces");
	}

	public void onDetachedFromWindow() {
		if (this.mRendererHelper != null) {
			synchronized (this.mRendererHelper) {
				this.mRendererHelper.shutdown();
				try {
					this.mRendererHelper.wait();
				} catch (InterruptedException e) {
					Log.e("CardboardView",
							"Interrupted during shutdown: " + e.toString());
				}
			}
		}
		super.onDetachedFromWindow();
	}

	private void init(Context context) {
		setEGLContextClientVersion(2);
		setPreserveEGLContextOnPause(true);

		WindowManager windowManager = (WindowManager) context
				.getSystemService("window");

		this.mHeadTracker = new HeadTracker(context);
		this.mHmd = new HeadMountedDisplay(windowManager.getDefaultDisplay());
	}

	public static abstract interface Renderer {
		public abstract void onDrawFrame(HeadTransform paramHeadTransform,
				EyeParams paramEyeParams1, EyeParams paramEyeParams2);

		public abstract void onFinishFrame(Viewport paramViewport);

		public abstract void onSurfaceChanged(int paramInt1, int paramInt2);

		public abstract void onSurfaceCreated(EGLConfig paramEGLConfig);

		public abstract void onRendererShutdown();
	}

	public static abstract interface StereoRenderer {
		public abstract void onNewFrame(HeadTransform paramHeadTransform);

		public abstract void onDrawEye(EyeTransform paramEyeTransform);

		public abstract void onFinishFrame(Viewport paramViewport);

		public abstract void onSurfaceChanged(int paramInt1, int paramInt2);

		public abstract void onSurfaceCreated(EGLConfig paramEGLConfig);

		public abstract void onRendererShutdown();
	}

	public static abstract interface CardboardDeviceParamsObserver {
		public abstract void onCardboardDeviceParamsUpdate(
				CardboardDeviceParams paramCardboardDeviceParams);
	}

	private class RendererHelper implements GLSurfaceView.Renderer {
		private final HeadTransform mHeadTransform;
		private final EyeParams mMonocular;
		private final EyeParams mLeftEye;
		private final EyeParams mRightEye;
		private final float[] mLeftEyeTranslate;
		private final float[] mRightEyeTranslate;
		private final CardboardView.Renderer mRenderer;
		private boolean mShuttingDown;
		private HeadMountedDisplay mHmd;
		private boolean mVRMode;
		private boolean mDistortionCorrectionEnabled;
		private float mDistortionCorrectionScale;
		private float mZNear;
		private float mZFar;
		private boolean mProjectionChanged;
		private boolean mInvalidSurfaceSize;

		public RendererHelper(CardboardView.Renderer renderer) {
			this.mRenderer = renderer;
			this.mHmd = new HeadMountedDisplay(CardboardView.this.mHmd);
			this.mHeadTransform = new HeadTransform();
			this.mMonocular = new EyeParams(0);
			this.mLeftEye = new EyeParams(1);
			this.mRightEye = new EyeParams(2);
			updateFieldOfView(this.mLeftEye.getFov(), this.mRightEye.getFov());
			CardboardView.this.mDistortionRenderer = new DistortionRenderer();

			this.mLeftEyeTranslate = new float[16];
			this.mRightEyeTranslate = new float[16];

			this.mVRMode = CardboardView.this.mVRMode;
			this.mDistortionCorrectionEnabled = CardboardView.this.mDistortionCorrectionEnabled;
			this.mDistortionCorrectionScale = CardboardView.this.mDistortionCorrectionScale;
			this.mZNear = CardboardView.this.mZNear;
			this.mZFar = CardboardView.this.mZFar;

			this.mProjectionChanged = true;
		}

		public void shutdown() {
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					synchronized (CardboardView.RendererHelper.this) {
						CardboardView.RendererHelper.this.mShuttingDown = true;
						CardboardView.RendererHelper.this.mRenderer
								.onRendererShutdown();
						CardboardView.RendererHelper.this.notifyAll();
					}
				}
			});
		}

		public void setCardboardDeviceParams(CardboardDeviceParams newParams) {
			final CardboardDeviceParams deviceParams = new CardboardDeviceParams(
					newParams);
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					CardboardView.RendererHelper.this.mHmd
							.setCardboard(deviceParams);
					CardboardView.RendererHelper.this.mProjectionChanged = true;
				}
			});
		}

		public void setScreenParams(ScreenParams newParams) {
			final ScreenParams screenParams = new ScreenParams(newParams);
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					CardboardView.RendererHelper.this.mHmd
							.setScreen(screenParams);
					CardboardView.RendererHelper.this.mProjectionChanged = true;
				}
			});
		}

		public void setInterpupillaryDistance(final float interpupillaryDistance) {
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					CardboardView.RendererHelper.this.mHmd.getCardboard()
							.setInterpupillaryDistance(interpupillaryDistance);
					CardboardView.RendererHelper.this.mProjectionChanged = true;
				}
			});
		}

		public void setFOV(final float fovY) {
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					CardboardView.RendererHelper.this.mHmd.getCardboard()
							.setFovY(fovY);
					CardboardView.RendererHelper.this.mProjectionChanged = true;
				}
			});
		}

		public void setZPlanes(final float zNear, final float zFar) {
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					CardboardView.RendererHelper.this.mZNear = zNear;
					CardboardView.RendererHelper.this.mZFar = zFar;
					CardboardView.RendererHelper.this.mProjectionChanged = true;
				}
			});
		}

		public void setDistortionCorrectionEnabled(final boolean enabled) {
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					CardboardView.RendererHelper.this.mDistortionCorrectionEnabled = enabled;
					CardboardView.RendererHelper.this.mProjectionChanged = true;
				}
			});
		}

		public void setDistortionCorrectionScale(final float scale) {
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					CardboardView.RendererHelper.this.mDistortionCorrectionScale = scale;
					CardboardView.this.mDistortionRenderer
							.setResolutionScale(scale);
				}
			});
		}

		public void setVRModeEnabled(final boolean enabled) {
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					if (CardboardView.RendererHelper.this.mVRMode == enabled) {
						return;
					}
					CardboardView.RendererHelper.this.mVRMode = enabled;
					if ((CardboardView.RendererHelper.this.mRenderer instanceof CardboardView.StereoRendererHelper)) {
						CardboardView.StereoRendererHelper stereoHelper = (CardboardView.StereoRendererHelper) CardboardView.RendererHelper.this.mRenderer;
						stereoHelper.setVRModeEnabled(enabled);
					}
					CardboardView.RendererHelper.this.mProjectionChanged = true;
					CardboardView.RendererHelper.this.onSurfaceChanged(
							(GL10) null, CardboardView.RendererHelper.this.mHmd
									.getScreen().getWidth(),
							CardboardView.RendererHelper.this.mHmd.getScreen()
									.getHeight());
				}
			});
		}

		public void onDrawFrame(GL10 gl) {
			if ((this.mShuttingDown) || (this.mInvalidSurfaceSize)) {
				return;
			}
			ScreenParams screen = this.mHmd.getScreen();
			CardboardDeviceParams cdp = this.mHmd.getCardboard();

			CardboardView.this.mHeadTracker.getLastHeadView(
					this.mHeadTransform.getHeadView(), 0);

			float halfInterpupillaryDistance = cdp.getInterpupillaryDistance() * 0.5F;
			if (this.mVRMode) {
				Matrix.setIdentityM(this.mLeftEyeTranslate, 0);
				Matrix.setIdentityM(this.mRightEyeTranslate, 0);

				Matrix.translateM(this.mLeftEyeTranslate, 0,
						halfInterpupillaryDistance, 0.0F, 0.0F);

				Matrix.translateM(this.mRightEyeTranslate, 0,
						-halfInterpupillaryDistance, 0.0F, 0.0F);

				Matrix.multiplyMM(this.mLeftEye.getTransform().getEyeView(), 0,
						this.mLeftEyeTranslate, 0,
						this.mHeadTransform.getHeadView(), 0);

				Matrix.multiplyMM(this.mRightEye.getTransform().getEyeView(),
						0, this.mRightEyeTranslate, 0,
						this.mHeadTransform.getHeadView(), 0);
			} else {
				System.arraycopy(this.mHeadTransform.getHeadView(), 0,
						this.mMonocular.getTransform().getEyeView(), 0,
						this.mHeadTransform.getHeadView().length);
			}
			if (this.mProjectionChanged) {
				this.mMonocular.getViewport().setViewport(0, 0,
						screen.getWidth(), screen.getHeight());
				if (!this.mVRMode) {
					float aspectRatio = screen.getWidth() / screen.getHeight();
					Matrix.perspectiveM(this.mMonocular.getTransform()
							.getPerspective(), 0, cdp.getFovY(), aspectRatio,
							this.mZNear, this.mZFar);
				} else if (this.mDistortionCorrectionEnabled) {
					updateFieldOfView(this.mLeftEye.getFov(),
							this.mRightEye.getFov());
					CardboardView.this.mDistortionRenderer.onProjectionChanged(
							this.mHmd, this.mLeftEye, this.mRightEye,
							this.mZNear, this.mZFar);
				} else {
					float distEyeToScreen = cdp.getVisibleViewportSize()
							/ 2.0F
							/ (float) Math
									.tan(Math.toRadians(cdp.getFovY()) / 2.0D);

					float left = screen.getWidthMeters() / 2.0F
							- halfInterpupillaryDistance;
					float right = halfInterpupillaryDistance;
					float bottom = cdp.getVerticalDistanceToLensCenter()
							- screen.getBorderSizeMeters();

					float top = screen.getBorderSizeMeters()
							+ screen.getHeightMeters()
							- cdp.getVerticalDistanceToLensCenter();

					FieldOfView leftEyeFov = this.mLeftEye.getFov();
					leftEyeFov.setLeft((float) Math.toDegrees(Math.atan2(left,
							distEyeToScreen)));

					leftEyeFov.setRight((float) Math.toDegrees(Math.atan2(
							right, distEyeToScreen)));

					leftEyeFov.setBottom((float) Math.toDegrees(Math.atan2(
							bottom, distEyeToScreen)));

					leftEyeFov.setTop((float) Math.toDegrees(Math.atan2(top,
							distEyeToScreen)));

					FieldOfView rightEyeFov = this.mRightEye.getFov();
					rightEyeFov.setLeft(leftEyeFov.getRight());
					rightEyeFov.setRight(leftEyeFov.getLeft());
					rightEyeFov.setBottom(leftEyeFov.getBottom());
					rightEyeFov.setTop(leftEyeFov.getTop());

					leftEyeFov.toPerspectiveMatrix(this.mZNear, this.mZFar,
							this.mLeftEye.getTransform().getPerspective(), 0);

					rightEyeFov.toPerspectiveMatrix(this.mZNear, this.mZFar,
							this.mRightEye.getTransform().getPerspective(), 0);

					this.mLeftEye.getViewport().setViewport(0, 0,
							screen.getWidth() / 2, screen.getHeight());

					this.mRightEye.getViewport().setViewport(
							screen.getWidth() / 2, 0, screen.getWidth() / 2,
							screen.getHeight());
				}
				this.mProjectionChanged = false;
			}
			if (this.mVRMode) {
				if (this.mDistortionCorrectionEnabled) {
					CardboardView.this.mDistortionRenderer.beforeDrawFrame();
					if (this.mDistortionCorrectionScale == 1.0F) {
						this.mRenderer.onDrawFrame(this.mHeadTransform,
								this.mLeftEye, this.mRightEye);
					} else {
						int leftX = this.mLeftEye.getViewport().x;
						int leftY = this.mLeftEye.getViewport().y;
						int leftWidth = this.mLeftEye.getViewport().width;
						int leftHeight = this.mLeftEye.getViewport().height;
						int rightX = this.mRightEye.getViewport().x;
						int rightY = this.mRightEye.getViewport().y;
						int rightWidth = this.mRightEye.getViewport().width;
						int rightHeight = this.mRightEye.getViewport().height;

						this.mLeftEye
								.getViewport()
								.setViewport(
										(int) (leftX * this.mDistortionCorrectionScale),
										(int) (leftY * this.mDistortionCorrectionScale),
										(int) (leftWidth * this.mDistortionCorrectionScale),
										(int) (leftHeight * this.mDistortionCorrectionScale));

						this.mRightEye
								.getViewport()
								.setViewport(
										(int) (rightX * this.mDistortionCorrectionScale),
										(int) (rightY * this.mDistortionCorrectionScale),
										(int) (rightWidth * this.mDistortionCorrectionScale),
										(int) (rightHeight * this.mDistortionCorrectionScale));

						this.mRenderer.onDrawFrame(this.mHeadTransform,
								this.mLeftEye, this.mRightEye);

						this.mLeftEye.getViewport().setViewport(leftX, leftY,
								leftWidth, leftHeight);

						this.mRightEye.getViewport().setViewport(rightX,
								rightY, rightWidth, rightHeight);
					}
					CardboardView.this.mDistortionRenderer.afterDrawFrame();
				} else {
					this.mRenderer.onDrawFrame(this.mHeadTransform,
							this.mLeftEye, this.mRightEye);
				}
			} else {
				this.mRenderer.onDrawFrame(this.mHeadTransform,
						this.mMonocular, null);
			}
			this.mRenderer.onFinishFrame(this.mMonocular.getViewport());
		}

		public void onSurfaceChanged(GL10 gl, int width, int height) {
			if (this.mShuttingDown) {
				return;
			}
			ScreenParams screen = this.mHmd.getScreen();
			if ((width != screen.getWidth()) || (height != screen.getHeight())) {
				if (!this.mInvalidSurfaceSize) {
					GLES20.glClear(16384);
					Log.w("CardboardView", "Surface size " + width + "x"
							+ height
							+ " does not match the expected screen size "
							+ screen.getWidth() + "x" + screen.getHeight()
							+ ". Rendering is disabled.");
				}
				this.mInvalidSurfaceSize = true;
			} else {
				this.mInvalidSurfaceSize = false;
			}
			this.mRenderer.onSurfaceChanged(width, height);
		}

		public void onSurfaceCreated(GL10 gl, EGLConfig config) {
			if (this.mShuttingDown) {
				return;
			}
			this.mRenderer.onSurfaceCreated(config);
		}

		private void updateFieldOfView(FieldOfView leftEyeFov,
				FieldOfView rightEyeFov) {
			CardboardDeviceParams cdp = this.mHmd.getCardboard();
			ScreenParams screen = this.mHmd.getScreen();
			Distortion distortion = cdp.getDistortion();

			float idealFovAngle = (float) Math.toDegrees(Math.atan2(
					cdp.getLensDiameter() / 2.0F, cdp.getEyeToLensDistance()));

			float eyeToScreenDist = cdp.getEyeToLensDistance()
					+ cdp.getScreenToLensDistance();

			float outerDist = (screen.getWidthMeters() - cdp
					.getInterpupillaryDistance()) / 2.0F;

			float innerDist = cdp.getInterpupillaryDistance() / 2.0F;
			float bottomDist = cdp.getVerticalDistanceToLensCenter()
					- screen.getBorderSizeMeters();

			float topDist = screen.getHeightMeters()
					+ screen.getBorderSizeMeters()
					- cdp.getVerticalDistanceToLensCenter();

			float outerAngle = (float) Math.toDegrees(Math.atan2(
					distortion.distort(outerDist), eyeToScreenDist));

			float innerAngle = (float) Math.toDegrees(Math.atan2(
					distortion.distort(innerDist), eyeToScreenDist));

			float bottomAngle = (float) Math.toDegrees(Math.atan2(
					distortion.distort(bottomDist), eyeToScreenDist));

			float topAngle = (float) Math.toDegrees(Math.atan2(
					distortion.distort(topDist), eyeToScreenDist));

			leftEyeFov.setLeft(Math.min(outerAngle, idealFovAngle));
			leftEyeFov.setRight(Math.min(innerAngle, idealFovAngle));
			leftEyeFov.setBottom(Math.min(bottomAngle, idealFovAngle));
			leftEyeFov.setTop(Math.min(topAngle, idealFovAngle));

			rightEyeFov.setLeft(Math.min(innerAngle, idealFovAngle));
			rightEyeFov.setRight(Math.min(outerAngle, idealFovAngle));
			rightEyeFov.setBottom(Math.min(bottomAngle, idealFovAngle));
			rightEyeFov.setTop(Math.min(topAngle, idealFovAngle));
		}
	}

	private class StereoRendererHelper implements CardboardView.Renderer {
		private final CardboardView.StereoRenderer mStereoRenderer;
		private boolean mVRMode;

		public StereoRendererHelper(CardboardView.StereoRenderer stereoRenderer) {
			this.mStereoRenderer = stereoRenderer;
			this.mVRMode = CardboardView.this.mVRMode;
		}

		public void setVRModeEnabled(final boolean enabled) {
			CardboardView.this.queueEvent(new Runnable() {
				public void run() {
					CardboardView.StereoRendererHelper.this.mVRMode = enabled;
				}
			});
		}

		public void onDrawFrame(HeadTransform head, EyeParams leftEye,
				EyeParams rightEye) {
			this.mStereoRenderer.onNewFrame(head);
			GLES20.glEnable(3089);

			leftEye.getViewport().setGLViewport();
			leftEye.getViewport().setGLScissor();
			this.mStereoRenderer.onDrawEye(leftEye.getTransform());
			if (rightEye == null) {
				return;
			}
			rightEye.getViewport().setGLViewport();
			rightEye.getViewport().setGLScissor();
			this.mStereoRenderer.onDrawEye(rightEye.getTransform());
		}

		public void onFinishFrame(Viewport viewport) {
			viewport.setGLViewport();
			viewport.setGLScissor();
			this.mStereoRenderer.onFinishFrame(viewport);
		}

		public void onSurfaceChanged(int width, int height) {
			if (this.mVRMode) {
				this.mStereoRenderer.onSurfaceChanged(width / 2, height);
			} else {
				this.mStereoRenderer.onSurfaceChanged(width, height);
			}
		}

		public void onSurfaceCreated(EGLConfig config) {
			this.mStereoRenderer.onSurfaceCreated(config);
		}

		public void onRendererShutdown() {
			this.mStereoRenderer.onRendererShutdown();
		}
	}
}
