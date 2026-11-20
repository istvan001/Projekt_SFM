package com.example.mozi.MovieController;

import com.example.mozi.dto.MovieRequest;
import com.example.mozi.dto.RoomRequest;
import jakarta.validation.Valid;
import com.example.mozi.model.Movie;
import com.example.mozi.model.Room;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.mozi.service.MoviseService;

import java.util.List;

@RequestMapping("/api/movies")
@RestController
public class MovieController {

    private final MoviseService moviseService;

    public MovieController(MoviseService moviseService) {
        this.moviseService = moviseService;
    }

    @GetMapping
    public List<Movie> getAllMovie()
    {
        return moviseService.getAllMovie();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable Long id)
    {
        Movie movie = moviseService.getMovieById(id);

        return ResponseEntity.ok(movie);
    }

    @PostMapping
    public ResponseEntity<Movie> createMovie(@Valid @RequestBody MovieRequest movieRequest)
    {
        Movie movie = moviseService.createRegularMovie(movieRequest);

        return  ResponseEntity.ok(movie);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movie> setRoom (@Valid @PathVariable Long id, @RequestBody RoomRequest roomRequest)
    {
        Movie movie = moviseService.getMovieById(id);
        movie.setRoom(roomRequest);

        return ResponseEntity.ok(movie);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id)
    {
        moviseService.deleteMovie(id);

        return ResponseEntity.noContent().build();
    }
}
