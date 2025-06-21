package com.mall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.item.domain.po.Item;
import com.mall.item.domain.po.ItemDoc;
import com.mall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;


@SpringBootTest(properties = "spring.profiles.active=local")
public class ElasticDocumentTest {

    private RestHighLevelClient client;
    @Autowired
    private IItemService itemService;

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
    public void testAddDocument() throws IOException {
        Item item = itemService.getById(1087063L);
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        String doc = JSONUtil.toJsonStr(itemDoc);

        //System.out.println(doc);

        IndexRequest indexRequest = new IndexRequest("items").id(itemDoc.getId());
        indexRequest.source(doc, XContentType.JSON);
        client.index(indexRequest, RequestOptions.DEFAULT);
    }

    @Test
    public void testGetDocument() throws IOException {
        GetRequest request = new GetRequest("items").id("1087063");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String json = response.getSourceAsString();
        ItemDoc doc = JSONUtil.toBean(json, ItemDoc.class);
        System.out.println(doc);
    }

    @Test
    public void testDeleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("items","1087063");
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testUpdateDocument() throws IOException {
        // 1.准备Request
        UpdateRequest request = new UpdateRequest("items", "1087063");
        // 2.准备请求参数
        request.doc(
                "price", 58800,
                "commentCount", 1
        );
        // 3.发送请求
        client.update(request, RequestOptions.DEFAULT);
    }

    @Test
    void testBulkDocument() throws IOException {
        int pageNo = 1, pageSize = 500;
        while (true) {
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));

            List<Item> records = page.getRecords();
            if (records == null || records.isEmpty()) {
                return;
            }

            BulkRequest bulkRequest = new BulkRequest();
            for (Item item : records) {
                bulkRequest.add(new IndexRequest("items")
                        .id(item.getId().toString())
                        .source(JSONUtil.toJsonStr(BeanUtil.copyProperties(item, ItemDoc.class)), XContentType.JSON));
            }
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
            pageNo++;
        }
    }
}
