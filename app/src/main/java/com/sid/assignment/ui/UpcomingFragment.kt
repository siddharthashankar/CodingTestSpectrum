package com.sid.assignment.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sid.assignment.R
import com.sid.assignment.adapter.MoviesAdapter
import com.sid.assignment.appComponent
import com.sid.assignment.model.Movie
import kotlinx.coroutines.launch
import javax.inject.Inject

class UpcomingFragment : Fragment() {
    @Inject
    lateinit var factory: ViewModelProvider.Factory
    private lateinit var viewModel: MoviesViewModel
    private lateinit var upcomingMovies: RecyclerView
    private lateinit var upcomingMoviesAdapter: MoviesAdapter
    private lateinit var upcomingMoviesLayoutMgr: LinearLayoutManager
    private var upcomingMoviesPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this, factory).get(MoviesViewModel::class.java)
    }

    //inflate the layout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?{
       return inflater.inflate(R.layout.fragment_upcoming, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Upcoming
        upcomingMovies = view.findViewById(R.id.upcoming_movies)
        upcomingMoviesLayoutMgr = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false
        )
        upcomingMovies.layoutManager = upcomingMoviesLayoutMgr
        upcomingMoviesAdapter = MoviesAdapter(mutableListOf()) { movie -> showMovieDetails(movie) }
        upcomingMovies.adapter = upcomingMoviesAdapter

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.upcomingMovies.observe(viewLifecycleOwner, Observer { movies ->
            upcomingMoviesAdapter.appendMovies(movies)
            attachUpcomingMoviesOnScrollListener()
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { onError() })
    }

    private fun onUpcomingMoviesFetched(movies: List<Movie>) {
        upcomingMoviesAdapter.appendMovies(movies)
        attachUpcomingMoviesOnScrollListener()
    }

    private fun attachUpcomingMoviesOnScrollListener() {
        upcomingMovies.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val totalItemCount = upcomingMoviesLayoutMgr.itemCount
                val visibleItemCount = upcomingMoviesLayoutMgr.childCount
                val firstVisibleItem = upcomingMoviesLayoutMgr.findFirstVisibleItemPosition()

                if (firstVisibleItem + visibleItemCount >= totalItemCount / 2) {
                    upcomingMovies.removeOnScrollListener(this)
                    upcomingMoviesPage++
                    lifecycleScope.launch {
                        viewModel.getUpcomingMovies(upcomingMoviesPage)
                    }
                }
            }
        })
    }

    private fun showMovieDetails(movie: Movie) {
        val intent = Intent(activity, MovieDetailsActivity::class.java)
        intent.putExtra(MOVIE_ID, movie.id)
        intent.putExtra(MOVIE_BACKDROP, movie.backdropPath)
        intent.putExtra(MOVIE_POSTER, movie.posterPath)
        intent.putExtra(MOVIE_TITLE, movie.title)
        intent.putExtra(MOVIE_RATING, movie.rating)
        intent.putExtra(MOVIE_RELEASE_DATE, movie.releaseDate)
        intent.putExtra(MOVIE_OVERVIEW, movie.overview)
        intent.putExtra(MOVIE_VOTECOUNT, movie.voteCount)
        startActivity(intent)
    }

    private fun onError() {
        Toast.makeText(activity, getString(R.string.error_fetch_movies), Toast.LENGTH_SHORT).show()
    }

}
