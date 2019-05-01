package com.example.locationapplication

    import android.app.Activity
    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import android.support.design.widget.Snackbar
    import android.view.View
    import android.widget.SeekBar
    import com.google.android.gms.maps.CameraUpdateFactory
    import com.google.android.gms.maps.GoogleMap
    import com.google.android.gms.maps.OnMapReadyCallback
    import com.google.android.gms.maps.SupportMapFragment
    import com.google.android.gms.maps.model.LatLng
    import kotlinx.android.synthetic.main.activity_new_reminder2.*
    import kotlin.math.roundToInt


class NewReminderActivity : BaseActivity(), OnMapReadyCallback {

    private  lateinit var map: GoogleMap

    private var reminder = Reminder(latLng = null, radius = null, message = null)

    private val radiusBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            updateRadiusWithProgress(progress)

            showReminderUpdate()
        }
    }

    private fun updateRadiusWithProgress(progress: Int) {
        val radius = getRadius(progress)
        reminder.radius = radius
        radiusDescription.text = getString(R.string.radius_description, radius.roundToInt().toString())
    }

    companion object {
        private const val EXTRA_LAT_LNG = "EXTRA_LAT_LNG"
        private const val EXTRA_ZOOM = "EXTRA_ZOOM"

        fun newIntent(context: Context, latLng: LatLng, zoom: Float): Intent {
            val intent = Intent(context, NewReminderActivity::class.java)
            intent
                .putExtra(EXTRA_LAT_LNG, latLng)
                .putExtra(EXTRA_ZOOM, zoom)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reminder2)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map1) as SupportMapFragment
        mapFragment.getMapAsync(this)

        instructionTitle.visibility = View.GONE
        instructionSubtitle.visibility = View.GONE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.GONE

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isMapToolbarEnabled = false

        centerCamera()

        showConfigureLocationStep()
    }

    private fun centerCamera() {
        val latLng = intent.extras.get(EXTRA_LAT_LNG) as LatLng
        val zoom = intent.extras.get(EXTRA_ZOOM) as Float
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun showConfigureLocationStep() {
        marker.visibility = View.VISIBLE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.VISIBLE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.GONE
        instructionTitle.text = getString(R.string.instruction_where_description)
        next.setOnClickListener {
            reminder.latLng = map.cameraPosition.target
            showConfigureRadiusStep()
        }

        showReminderUpdate()
    }

    private fun showConfigureRadiusStep() {
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.GONE
        radiusBar.visibility = View.VISIBLE
        radiusDescription.visibility = View.VISIBLE
        message.visibility = View.GONE
        instructionTitle.text = getString(R.string.instruction_radius_description)
        next.setOnClickListener {
            showConfigureMessageStep()
        }
        radiusBar.setOnSeekBarChangeListener(radiusBarChangeListener)
        updateRadiusWithProgress(radiusBar.progress)

        map.animateCamera(CameraUpdateFactory.zoomTo(15f))

        showReminderUpdate()
    }

    private fun getRadius(progress: Int) = 100 + (2 * progress.toDouble() + 1) * 100

    private fun showConfigureMessageStep() {
        marker.visibility = View.GONE
        instructionTitle.visibility = View.VISIBLE
        instructionSubtitle.visibility = View.GONE
        radiusBar.visibility = View.GONE
        radiusDescription.visibility = View.GONE
        message.visibility = View.VISIBLE
        instructionTitle.text = getString(R.string.instruction_message_description)
        next.setOnClickListener {
            hideKeyboard(this, message)

            reminder.message = message.text.toString()

            if (reminder.message.isNullOrEmpty()) {
                message.error = getString(R.string.error_required)
            } else {
                addReminder(reminder)
            }
        }
        message.requestFocusWithKeyboard()

        showReminderUpdate()
    }

    private fun addReminder(reminder: Reminder) {
        getRepository().add(reminder,
            success = {
                setResult(Activity.RESULT_OK)
                finish()
            },
            failure = {
                Snackbar.make(main, it, Snackbar.LENGTH_LONG).show()
            })
    }

    private fun showReminderUpdate() {
        map.clear()
        showReminderInMap(this, map, reminder)
    }
}
