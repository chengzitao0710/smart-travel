package com.smarttravel.scenic.repository;

import com.smarttravel.scenic.entity.ScenicDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ScenicRepository extends ElasticsearchRepository<ScenicDoc, Long> {
}