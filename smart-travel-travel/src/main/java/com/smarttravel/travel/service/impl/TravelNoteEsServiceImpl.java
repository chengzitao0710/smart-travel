package com.smarttravel.travel.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import com.smarttravel.common.dto.Result;
import com.smarttravel.common.utils.SystemConstants;
import com.smarttravel.travel.entity.TravelNote;
import com.smarttravel.travel.entity.TravelNoteDoc;
import com.smarttravel.travel.repository.TravelNoteRepository;
import com.smarttravel.travel.service.TravelNoteEsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class TravelNoteEsServiceImpl implements TravelNoteEsService {

    @Resource
    private TravelNoteRepository travelNoteRepository;

    @Resource
    private ElasticsearchOperations elasticsearchOperations;

    public void indexNote(TravelNote note) {
        TravelNoteDoc doc = TravelNoteDoc.from(note);
        travelNoteRepository.save(doc);
        log.debug("ES索引游记成功: id={}", note.getId());
    }

    public void deleteNote(Long id) {
        travelNoteRepository.deleteById(id);
        log.debug("ES删除游记成功: id={}", id);
    }

    public Result searchNotes(String keyword, Integer current, String tags, String sort, Long scenicId) {
        if (keyword == null || keyword.isEmpty()) {
            return Result.fail("搜索关键词不能为空");
        }
        try {
            Highlight highlight = new Highlight(
                    List.of(
                            new HighlightField("title", HighlightFieldParameters.builder().withPreTags("<em>").withPostTags("</em>").build()),
                            new HighlightField("content", HighlightFieldParameters.builder().withPreTags("<em>").withPostTags("</em>").withFragmentSize(100).build())
                    )
            );
            HighlightQuery highlightQuery = new HighlightQuery(highlight, TravelNoteDoc.class);

            NativeQuery query = NativeQuery.builder()
                    .withQuery(q -> q
                            .bool(b -> {
                                    b.must(m -> m.term(t -> t.field("status").value(SystemConstants.BLOG_STATUS_PUBLISHED)));
                                    b.must(m -> m.multiMatch(mm -> mm.query(keyword).fields("title", "content")));
                                    if (tags != null && !tags.isEmpty()) {
                                        b.filter(f -> f.term(t -> t.field("tags").value(tags)));
                                    }
                                    if (scenicId != null) {
                                        b.must(m -> m.term(t -> t.field("scenicId").value(scenicId)));
                                    }

                                    return b;
                                })
                            )
                    .withHighlightQuery(highlightQuery)
                    .withPageable(PageRequest.of(current - 1, SystemConstants.DEFAULT_PAGE_SIZE))
                    .withSort(s -> {
                        if ("hot".equals(sort)) {
                            s.field(f -> f.field("liked").order(SortOrder.Desc));
                        } else {
                            s.field(f -> f.field("createTime").order(SortOrder.Desc));
                        }
                        return s;
                    })
                    .build();
            SearchHits<TravelNoteDoc> searchHits = elasticsearchOperations.search(query, TravelNoteDoc.class);
            List<HashMap<String, Object>> results = searchHits.getSearchHits().stream()
                    .map(hit -> {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("doc", hit.getContent());
                        map.put("highlight", hit.getHighlightFields());
                        return map;
                    })
                    .toList();

            return Result.ok(results, searchHits.getTotalHits());
        } catch (Exception e) {
            log.error("ES搜索游记失败: keyword={}", keyword, e);
            return Result.fail("搜索服务异常，请稍后重试");
        }
    }
}