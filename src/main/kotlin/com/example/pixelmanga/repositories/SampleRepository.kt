package com.example.pixelmanga.repositories

import com.example.pixelmanga.entities.Sample
import org.springframework.data.jpa.repository.JpaRepository

interface SampleRepository : JpaRepository<Sample, Long> {
}