package com.example.locationapplication

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.ar.core.*
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_ar_image_scanning.*
import java.io.IOException


class ArActivityImageScanning : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private  var shouldAddModel = true;
    private  lateinit var arSceneView : ArSceneView
    var mSession: Session? = null
    private var sessionConfigured = false

    //Four Ar nodes for the puzzle
    private lateinit var redNode: TransformableNode
    private lateinit var greenNode: TransformableNode
    private lateinit var blueNode: TransformableNode
    private lateinit var yellowNode: TransformableNode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ar_image_scanning)
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment

        //Adding delay,as AR session takes sometime to get intialised.
        val handler =  Handler()
        handler.postDelayed( Runnable() {
        arFragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)},2000)

        //Hiding the plane discovery, as we do not need for this application.
        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
        arSceneView= arFragment.getArSceneView();
        //Button action after user is done solving the puzzle
        buttonDone.setOnClickListener{
            checkPosition()
        }
    }

    private fun onUpdateFrame(frameTime: FrameTime) {

       val frame = arFragment.arSceneView.arFrame
        //Getting all the augumented images from database.
       val augmentedImages = frame.getUpdatedTrackables(AugmentedImage::class.java)
        //Check for model to be added once only.
       if(shouldAddModel) {
           for (augmentedImage in augmentedImages) {
               if (augmentedImage.trackingState == TrackingState.TRACKING) {
                    //Checking if the image detected is the correct one
                   if (augmentedImage.name == "Ub" && shouldAddModel) {
                       if (arFragment.arSceneView.session != null) {

                           //Creating the middle point anchor, around which all the objects will be placed.
                           val session = arFragment.arSceneView.session
                           val anchort = session.createAnchor(
                               frame.getCamera().getPose()
                                   .compose(Pose.makeTranslation(0f, 0f, -1f))
                                   .extractTranslation()
                           )

                           //Places the different objects
                           placeObject(
                               arFragment,
                               anchort,
                               Uri.parse("model.sfb"), frame
                           )
                           shouldAddModel = false
                       }
                   }
               }
           }
       }
   }

    private fun placeObject(arFragment: ArFragment, anchor: Anchor, uri: Uri,frame:Frame) {

        //Creating anchor node for Red cube, at 0,0 and z = -1.0
        val session = arFragment.arSceneView.session
        val anchor2 = session.createAnchor(
            frame.getCamera().getPose()
                .compose(Pose.makeTranslation(0.0f, 0.0f, -1.0f))
                .extractTranslation())


        //Added the rednode to the scenes
        MaterialFactory.makeOpaqueWithColor(this,Color(android.graphics.Color.RED) )
                    .thenAccept { material ->

                        val anchorNode = AnchorNode(anchor2)
                        redNode = TransformableNode(arFragment.transformationSystem)
                        redNode.scaleController.maxScale = 0.09f
                        redNode.scaleController.minScale = 0.07f
                        redNode.renderable = ShapeFactory.makeCube(Vector3(3f, 3f, 3f),
                            Vector3(0.0f, 0.0f, 0.0f),
                            material)
                        redNode.setParent(anchorNode)
                        arFragment.arSceneView.scene.addChild(anchorNode)
                        redNode.select()
                    }


        //Creating anchor node for Green cube, at 0,0 and z = -1.0
        val anchor3 = session.createAnchor(
            frame.getCamera().getPose()
                .compose(Pose.makeTranslation(0.5f, 0.0f, -1.2f))
                .extractTranslation())

        //Added the Greennode to the scenes
        MaterialFactory.makeOpaqueWithColor(this,Color(android.graphics.Color.GREEN) )
            .thenAccept { material ->

                val anchorNode = AnchorNode(anchor3)
                greenNode = TransformableNode(arFragment.transformationSystem)
                greenNode.scaleController.maxScale = 0.09f
                greenNode.scaleController.minScale = 0.07f
                greenNode.renderable = ShapeFactory.makeCube(Vector3(3f, 3f, 3f),
                    Vector3(0.0f, 0.0f, 0.0f),
                    material)
                greenNode.setParent(anchorNode)
                arFragment.arSceneView.scene.addChild(anchorNode)
                greenNode.select()
            }


        //Creating anchor node for Blue cube, at 1.2,0 and z = -1.2
        val anchor4 = session.createAnchor(
            frame.getCamera().getPose()
                .compose(Pose.makeTranslation(1.2f, 0.0f, -1.2f))
                .extractTranslation())

        //Added the Bluenode to the scenes
        MaterialFactory.makeOpaqueWithColor(this,Color(android.graphics.Color.BLUE) )
            .thenAccept { material ->
                val anchorNode = AnchorNode(anchor4)
                blueNode = TransformableNode(arFragment.transformationSystem)
                blueNode.scaleController.maxScale = 0.09f
                blueNode.scaleController.minScale = 0.07f
                blueNode.renderable = ShapeFactory.makeCube(Vector3(3f, 3f, 3f),
                    Vector3(0.0f, 0.0f, 0.0f),
                    material)
                blueNode.setParent(anchorNode)
                arFragment.arSceneView.scene.addChild(anchorNode)
                blueNode.select()
            }

        //Creating anchor node for Blue cube, at 1.2,0 and z = -1.2
        val anchor5 = session.createAnchor(
            frame.getCamera().getPose()
                .compose(Pose.makeTranslation(0.7f, 0.0f, -1.0f))
                .extractTranslation())

        //Added the yellownode to the scenes
        MaterialFactory.makeOpaqueWithColor(this,Color(android.graphics.Color.YELLOW) )
            .thenAccept { material ->

                val anchorNode = AnchorNode(anchor5)
                yellowNode = TransformableNode(arFragment.transformationSystem)
                yellowNode.scaleController.maxScale = 0.09f
                yellowNode.scaleController.minScale = 0.07f
                yellowNode.renderable = ShapeFactory.makeCube(Vector3(3f, 3f, 3f),
                    Vector3(0.0f, 0.0f, 0.0f),
                    material)
                yellowNode.setParent(anchorNode)
                arFragment.arSceneView.scene.addChild(anchorNode)
                yellowNode.select()
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
        //Intialising session object. This will be helpful while creating anchor points
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
                Log.e("hello", "Exception creating session", exception)
                return
            }
            sessionConfigured = true

        }

        //Call to configure session , which will fetch all the augmented images database
        if (sessionConfigured) {
            configureSession()
            sessionConfigured = false

            arSceneView.setupSession(mSession)
        }


    }

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

    //Adds all the images we want to be detected by ArCore to its database
    fun setupAugmentedImagesDb(config: Config, session: Session): Boolean {
        val augmentedImageDatabase: AugmentedImageDatabase
        val bitmap = loadAugmentedImage() ?: return false
        print(bitmap)
        augmentedImageDatabase = AugmentedImageDatabase(session)
        augmentedImageDatabase.addImage("Ub", bitmap)
        config.setAugmentedImageDatabase(augmentedImageDatabase)
        return true
    }

    //Convert an image to bitmap
    private fun loadAugmentedImage(): Bitmap? {
        try {
            assets.open("Ub.jpeg").use { `is` -> return BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            Log.e("ImageLoad", "IO Exception", e)
        }

        return null
    }

    //Function to get screen center point
    private fun getScreenCenter(): android.graphics.Point {
        val vw = findViewById<View>(android.R.id.content)
        return Point(vw.width / 2, vw.height / 2)
    }

    //To check the relative positions of  diffrent color nodes for authentication
    private fun checkPosition(){
        val redPosition = redNode.worldPosition
        val bluePosition = blueNode.getWorldPosition()
        val greenPosition = greenNode.getWorldPosition()
        val yellowPosition = yellowNode.getWorldPosition()

        val rgdx = greenPosition.x - redPosition.x

        val rgdz = greenPosition.z - redPosition.z

        val ybdx = yellowPosition.x - bluePosition.x

        val ybdz = yellowPosition.z - bluePosition.z


        //Checking  the distance between red and yellow and between blue and yellow
        if(rgdx < 0.28 && rgdx>0 && rgdz<0.1 && rgdz > -0.1 && ybdx < 0.28 && ybdx>0 && ybdz<0.1 && ybdz > -0.1){

            buttonDone.text = "Authenticated"
            val intent = Intent(this@ArActivityImageScanning, ArActivity::class.java);
            startActivity(intent)

        }else{
            buttonDone.text = "Try Again"
        }
    }
}
