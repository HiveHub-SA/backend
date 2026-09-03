package com.hivehub.app.apiarios.videos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoApiarioRepository extends JpaRepository<VideoApiario, Long> {
    List<VideoApiario> findByApiarioIdOrderByCreatedAtDesc(Long apiarioId);
}