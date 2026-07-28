package com.smarttravel.scenic.service.impl;

import cn.hutool.core.util.StrUtil;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.scenic.entity.Scenic;
import com.smarttravel.scenic.entity.ScenicDoc;
import com.smarttravel.scenic.repository.ScenicRepository;
import com.smarttravel.scenic.service.ScenicEsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ScenicEsServiceImpl implements ScenicEsService {
    @Resource
    private ScenicRepository scenicRepository;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    public void indexScenic(Scenic scenic) {
        ScenicDoc doc = ScenicDoc.from(scenic);
        scenicRepository.save(doc);
        log.debug("ES索引景点成功: id={}", scenic.getId());
    }

    public void updateScenic(Scenic scenic) {
        ScenicDoc doc = ScenicDoc.from(scenic);
        scenicRepository.save(doc);
        log.debug("ES更新景点成功: id={}", scenic.getId());
    }

    public void deleteScenic(Long id) {
        scenicRepository.deleteById(id);
        log.debug("ES删除景点成功: id={}", id);
    }

    public Result searchScenic(String keyword, Long typeId, String area, String sort, Integer current) {
        try {
            Sort sortSpec = buildSort(sort);
            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .bool(b -> {
                                b.must(m -> m.term(t -> t.field("status").value(SystemConstants.SCENIC_STATUS_ON)));
                                if (StrUtil.isNotBlank(keyword)) {
                                    b.must(m -> m.multiMatch(mm -> mm
                                            .fields("name", "description", "tags", "address")
                                            .query(keyword)));
                                }
                                if (typeId != null) {
                                    b.filter(f -> f.term(t -> t.field("typeId").value(typeId)));
                                }
                                if (StrUtil.isNotBlank(area)) {
                                    b.filter(f -> f.term(t -> t.field("area").value(area)));
                                }
                                return b;
                            })
                    )
                    .withPageable(PageRequest.of(current - 1, SystemConstants.DEFAULT_PAGE_SIZE, sortSpec))
                    .build();

            SearchHits<ScenicDoc> searchHits = elasticsearchOperations.search(query, ScenicDoc.class);
            List<ScenicDoc> docs = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .toList();

            return Result.ok(docs, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("ES搜索景点失败: keyword={}, typeId={}, area={}, sort={}, current={}", keyword, typeId, area, sort, current, e);
            return Result.fail("搜索服务异常，请稍后重试");
        }
    }

    private Sort buildSort(String sort) {
        if ("hot".equals(sort)) {
            return Sort.by(Sort.Order.desc("sold"), Sort.Order.desc("id"));
        } else if ("top".equals(sort)) {
            return Sort.by(Sort.Order.desc("score"), Sort.Order.desc("id"));
        }
        return Sort.by(Sort.Order.desc("id"));
    }

}