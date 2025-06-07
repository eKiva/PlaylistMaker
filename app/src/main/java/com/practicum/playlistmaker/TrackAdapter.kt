package com.practicum.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


class TrackAdapter(private var tracks: MutableList<Track>): RecyclerView.Adapter<TrackViewHolder> () {

    interface OnTrackClickListener {
        fun onTrackClick(track: Track, position: Int)
    }


    private var onClickListener: OnTrackClickListener? = null

    constructor(tracks: MutableList<Track>, onTrackClickListener: OnTrackClickListener) : this(tracks) {
        this.tracks = tracks
        this.onClickListener = onTrackClickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return TrackViewHolder(view)
    }


    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks.get(position))

        val track = tracks.get(position)

        holder.itemView.setOnClickListener {onClickListener?.onTrackClick(track, position) }
    }


    override fun getItemCount(): Int {
        return tracks.size
    }
}