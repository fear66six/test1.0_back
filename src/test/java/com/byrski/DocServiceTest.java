package com.byrski;

import com.byrski.domain.entity.dto.Doc;
import com.byrski.service.DocService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Order;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class DocServiceTest {

    @Autowired
    private DocService docService;

    private Doc testDoc;
    private String savedDocId;

    @BeforeEach
    public void setUp() {
        // 创建测试数据
        testDoc = createTestDoc();
    }

    @AfterEach
    public void tearDown() {
        // 清理测试数据
        try {
            if (savedDocId != null) {
                docService.deleteDoc(savedDocId);
            }
        } catch (Exception e) {
            // 忽略删除异常
        }
    }

    @Test
    @Order(1)
    public void testInsertDoc() {
        // 测试插入操作
        Doc savedDoc = docService.insertDoc(testDoc);
        
        // 保存ID用于后续测试
        savedDocId = savedDoc.getId();
        
        // 断言
        Assert.notNull(savedDoc, "Saved document should not be null");
        Assert.notNull(savedDoc.getId(), "Saved document should have an ID");
        Assert.isTrue(savedDoc.getType().equals(testDoc.getType()), "Type should match");
        Assert.isTrue(savedDoc.getActivityTemplateId().equals(testDoc.getActivityTemplateId()), 
            "ActivityTemplateId should match");
    }

    @Test
    @Order(2)
    public void testUpdateDoc() {
        // 先插入文档
        Doc savedDoc = docService.insertDoc(testDoc);
        savedDocId = savedDoc.getId();

        // 修改文档内容
        Doc.Content newContent = new Doc.Content();
        newContent.setTitle("Updated Title");
        newContent.setImageUrl("https://example.com/updated.jpg");
        newContent.setParagraphs(Arrays.asList("Updated paragraph 1", "Updated paragraph 2"));
        savedDoc.setContents(Arrays.asList(newContent));

        // 执行更新
        Doc updatedDoc = docService.updateDoc(savedDoc);

        // 断言
        Assert.notNull(updatedDoc, "Updated document should not be null");
        Assert.isTrue(updatedDoc.getId().equals(savedDocId), "Document ID should not change");
        Assert.isTrue(updatedDoc.getContents().get(0).getTitle().equals("Updated Title"), 
            "Title should be updated");
    }

    @Test
    @Order(3)
    public void testDeleteDoc() {
        // 先插入文档
        Doc savedDoc = docService.insertDoc(testDoc);
        String docId = savedDoc.getId();

        // 执行删除
        docService.deleteDoc(docId);

        // 查询验证删除结果
        List<Doc> docs = docService.findDocs(testDoc.getActivityTemplateId(), testDoc.getType());
        Assert.isTrue(docs.stream().noneMatch(doc -> docId.equals(doc.getId())), 
            "Document should be deleted");
    }

    @Test
    @Order(4)
    public void testFindDocs() {
        // 先插入文档
        Doc savedDoc = docService.insertDoc(testDoc);
        savedDocId = savedDoc.getId();

        // 执行查询
        List<Doc> foundDocs = docService.findDocs(testDoc.getActivityTemplateId(), testDoc.getType());

        // 断言
        Assert.notEmpty(foundDocs, "Should find at least one document");
        Assert.isTrue(foundDocs.stream()
            .anyMatch(doc -> doc.getActivityTemplateId().equals(testDoc.getActivityTemplateId()) 
                && doc.getType().equals(testDoc.getType())),
            "Should find document with matching activityTemplateId and type");
    }

    @Test
    @Order(5)
    public void testInsertDocWithNullValues() {
        // 测试插入空文档异常情况
        Doc emptyDoc = new Doc();
        try {
            docService.insertDoc(emptyDoc);
            Assert.isTrue(false, "Should throw exception for null values");
        } catch (Exception e) {
            Assert.isTrue(true, "Should throw exception for null values");
        }
    }

    @Test
    @Order(6)
    public void testUpdateDocWithoutId() {
        // 测试更新没有ID的文档
        Doc docWithoutId = createTestDoc();
        try {
            docService.updateDoc(docWithoutId);
            Assert.isTrue(false, "Should throw exception for missing ID");
        } catch (IllegalArgumentException e) {
            Assert.isTrue(true, "Should throw exception for missing ID");
        }
    }

    // 辅助方法：创建测试文档
    private Doc createTestDoc() {
        Doc doc = new Doc();
        doc.setType(1);
        doc.setActivityTemplateId(1001L);

        Doc.Content content = new Doc.Content();
        content.setTitle("Test Title");
        content.setImageUrl("https://example.com/test.jpg");
        content.setParagraphs(Arrays.asList(
            "Test paragraph 1",
            "Test paragraph 2",
            "Test paragraph 3"
        ));

        doc.setContents(Arrays.asList(content));
        return doc;
    }
}