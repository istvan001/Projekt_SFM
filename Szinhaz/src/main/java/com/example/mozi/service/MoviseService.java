package com.example.mozi.service;

import com.example.mozi.dto.MovieRequest;
import com.example.mozi.dto.RoomRequest;
import com.example.mozi.model.Movie;
import com.example.mozi.model.Room;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.mozi.repository.MovieRepository;

import java.util.List;

@Service
public class MoviseService {

    private final MovieRepository movieRepository;

    public MoviseService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getAllMovie() {
        return movieRepository.findAll();
    }

    public Movie getMovieById(Long id)
    {
        return movieRepository.findById(id).orElseThrow();
    }
    public Movie createRegularMovie(MovieRequest movieRequest) {
        Movie movie = new Movie.Builder(movieRequest.getTitle(),
                movieRequest.getAuthor(), movieRequest.getLength(), movieRequest.getGenre()).build();

        return movieRepository.save(movie);
    }
    public Movie create18PlusMovie(MovieRequest movieRequest)
    {
        Movie movie = new Movie.Builder(movieRequest.getTitle(),
                movieRequest.getAuthor(),
                movieRequest.getLength(),
                movieRequest.getGenre()).isForAdultsOnly(true).build();

        return movieRepository.save(movie);
    }

    @Transactional
    public Movie setRoom(Long id, RoomRequest roomRequest)
    {
        Movie movie = movieRepository.findById(id).orElseThrow();
        movie.setRoom(roomRequest.setRoomNum());

        return movie;
    }

    public void deleteMovie(Long id)
    {
        Movie movie = movieRepository.findById(id).orElseThrow();
        movieRepository.delete(movie);
    }
}
