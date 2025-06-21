package com.mall.item.es;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.mall.item.domain.po.ItemDoc;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

public class ElasticSearchTest {

    private RestHighLevelClient client;

    @BeforeEach
    public void setUp() throws Exception {
        this.client = new RestHighLevelClient(
                RestClient.builder(HttpHost.create("http://192.168.229.129:9200"))
        );
    }

    @AfterEach
    public void tearDown() throws IOException {
        client.close();
    }

    @Test
    void testMatchAll() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.matchAllQuery());
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        handleResponse(response);
    }

    @Test
    void testPageAndSort() throws IOException {
        int pageNo = 1, pageSize = 5;
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.matchAllQuery())
                .from((pageNo-1)*pageSize).size(pageSize)
                .sort("sold", SortOrder.DESC)
                .sort("price", SortOrder.DESC);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        handleResponse(response);
    }

    @Test
    void testBool() throws IOException {
        SearchRequest request = new SearchRequest("items");

        BoolQueryBuilder bool = QueryBuilders.boolQuery();
        bool.must(QueryBuilders.matchQuery("name","脱脂牛奶"));
        bool.filter(QueryBuilders.termQuery("brand","德亚"));
        bool.filter(QueryBuilders.rangeQuery("price").lte(30000));
        request.source().query(bool);

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        handleResponse(response);
    }

    @Test
    void testHighLight() throws IOException {
        SearchRequest request = new SearchRequest("items");
        request.source().query(QueryBuilders.matchQuery("name","脱脂牛奶"))
                        .highlighter(SearchSourceBuilder.highlight().field("name"));

        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        handleResponse(response);
    }

    private void handleResponse(SearchResponse response){
        SearchHits searchHits = response.getHits();
        long total = searchHits.getTotalHits().value;
        System.out.println(total);

        SearchHit[] hits = searchHits.getHits();
        for (SearchHit hit : hits) {
            String source = hit.getSourceAsString();
            ItemDoc doc = JSONUtil.toBean(source, ItemDoc.class);
            Map<String, HighlightField> hfs = hit.getHighlightFields();
            if(CollUtil.isNotEmpty(hfs)){
                HighlightField hf = hfs.get("name");
                if(hf != null){
                    String hfName = hf.getFragments()[0].toString();
                    doc.setName(hfName);
                }
            }
            System.out.println("doc = " + doc);
        }
    }

}
