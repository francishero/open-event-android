package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.content_event.view.*
import kotlinx.android.synthetic.main.fragment_event.view.*
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber


class EventDetailsFragment : Fragment() {
    val EVENT_ID = "EVENT_ID"
    private val eventViewModel by viewModel<EventDetailsViewModel>()
    private lateinit var rootView: View
    private var eventId: Long = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            eventId = bundle.getLong(EVENT_ID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_event, container, false)
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)

        eventViewModel.event.observe(this, Observer {
            it?.let {
                loadEvent(it)
            }
            Timber.d("Fetched events of id %d", eventId)
        })

        eventViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        eventViewModel.loadEvent(eventId)

        return rootView
    }

    private fun loadEvent(event: Event) {
        val startsAt = EventUtils.getLocalizedDateTime(event.startsAt)
        val endsAt = EventUtils.getLocalizedDateTime(event.endsAt)
        rootView.event_name.text = event.name
        rootView.event_organiser_name.text = event.organizerName
        setTextField(rootView.event_description, event.description)
        setTextField(rootView.event_organiser_description, event.organizerDescription)
        rootView.event_location_text_view.text = event.locationName

        rootView.starts_on.text = "${startsAt.dayOfMonth} ${startsAt.month} ${startsAt.year}"
        rootView.ends_on.text = "${endsAt.dayOfMonth} ${endsAt.month} ${endsAt.year}"

        event.originalImageUrl?.let {
            Picasso.get()
                    .load(it)
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(rootView.logo)
        }

        rootView.event_share_fab.setOnClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, EventUtils.getSharableInfo(event))
            sendIntent.type = "text/plain"
            rootView.context.startActivity(Intent.createChooser(sendIntent, "Share Event Details"))
        }
    }

    override fun onDestroyView() {
        val activity = activity as? MainActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setTextField(textView: TextView, value: String?) {
        if (value.isNullOrEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = value
        }
    }
}