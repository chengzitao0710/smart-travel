package com.smarttravel.travel.repository;

import com.smarttravel.travel.entity.TravelNoteDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface TravelNoteRepository extends ElasticsearchRepository<TravelNoteDoc, Long> {
}