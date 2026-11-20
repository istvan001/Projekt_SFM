package com.example.mozi.repository;


import com.example.mozi.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends  JpaRepository<Movie,Long> {

}
