package com.example.locationapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log.e
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_ar.*
import java.io.IOException


class ArActivity : AppCompatActivity() {
    private lateinit var arFragment:  ArFragment
    private  lateinit var arSceneView :ArSceneView
    var mSession: Session? = null
    private var sessionConfigured = false
    private var currPosition:Pose? = null
    private var prevPosition:Pose? = null
    private var xConstraint = 1310
    private var yConstraint = 290
    private var displacementx = 0.0
    private var displacementy = 0.0
    private var oldConstarintx = 1310
    private var oldConstarinty = 290

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_ar)

        //this text view will be used for debugging
        text_trans.text = "Hello"

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
        arSceneView= arFragment.getArSceneView();

    }


    fun setupAugmentedImagesDb(config: Config, session: Session): Boolean {
        val augmentedImageDatabase: AugmentedImageDatabase
        val bitmap = loadAugmentedImage() ?: return false
        print(bitmap)
        augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("Ub", bitmap)
        config.setAugmentedImageDatabase(augmentedImageDatabase)
        return true
    }

    private fun loadAugmentedImage(): Bitmap? {
        try {
            assets.open("Ub.jpeg").use { `is` -> return BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            e("ImageLoad", "IO Exception", e)
        }
        return null
    }


    private fun onUpdateFrame(frameTime: FrameTime){
        val frame = arFragment.arSceneView.arFrame
        val camera = frame.camera

        //Checking if Ar camera is tracking state
        if (camera.trackingState === TrackingState.TRACKING) {
            val cameraPose = camera.displayOrientedPose
            val translation = cameraPose.getTranslation()
                //cameraPose.extractTranslation()

            //TODO
            val rotation = cameraPose.extractRotation()

            //Calculating the displacement
            if (prevPosition != null){
                val prevTrans = (prevPosition as Pose).getTranslation()
                val x_tran = prevTrans.get(0) - translation.get(0)
                val y_tran = prevTrans.get(1) - translation.get(1)


                displacementx += x_tran
                displacementy += y_tran

                val relativeDisplacemnetX =  displacementx / 0.05
                val relativeDisplacemnetY =  displacementy/ 0.04


               //Updating constraint only when significant change is there
                if(oldConstarintx != xConstraint - relativeDisplacemnetX.toInt() && oldConstarinty != yConstraint +  relativeDisplacemnetY.toInt()){
                     val params = currentLocationbutton.layoutParams as ConstraintLayout.LayoutParams
                      params.topMargin = xConstraint + relativeDisplacemnetX.toInt()
                      params.leftMargin = yConstraint -  relativeDisplacemnetY.toInt()
                      currentLocationbutton.requestLayout()

                }

                //Debugging text
                text_trans.text = (xConstraint - relativeDisplacemnetX.toInt()).toString() +
                        "---------" + (xConstraint - relativeDisplacemnetX.toInt()).toString() +
                        "---------------" + displacementx +
                        "-----------" +displacementy + "----------**" +
                relativeDisplacemnetX.toString() +"----------**" + relativeDisplacemnetX.toString()

                prevPosition = currPosition
                currPosition = cameraPose

            }else{
                prevPosition = currPosition
                currPosition = cameraPose
            }

        }else{
            text_trans.text = "Not tracking"
            print("Not tracking")
        }
    }


    //Configuring session.
    private fun configureSession() {
        val config = Config(mSession)
        if  (mSession != null) {
            if (!setupAugmentedImagesDb(config,mSession as Session)) {
                Toast.makeText(this, "Unable to setup augmented", Toast.LENGTH_SHORT).show()
            }
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            (mSession as Session).configure(config)
        }
    }


    public override fun onPause() {
        super.onPause()
        if (mSession != null) {

            arSceneView.pause()
            (mSession as Session).pause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mSession == null) {
            var message: String? = null
            var exception: Exception? = null
            try {
                mSession = Session(this)
            } catch (e: UnavailableArcoreNotInstalledException) {
                message = "Please install ARCore"
                exception = e
            } catch (e: UnavailableApkTooOldException) {
                message = "Please update ARCore"
                exception = e
            } catch (e: UnavailableSdkTooOldException) {
                message = "Please update android"
                exception = e
            } catch (e: Exception) {
                message = "AR is not supported"
                exception = e
            }

            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                e("hello", "Exception creating session", exception)
                return
            }
            sessionConfigured = true

        }
        if (sessionConfigured) {
            configureSession()
            sessionConfigured = false

            arSceneView.setupSession(mSession)
        }


    }

}
