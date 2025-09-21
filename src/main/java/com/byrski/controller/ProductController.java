package com.byrski.controller;

import com.byrski.domain.entity.RestBean;
import com.byrski.domain.entity.vo.response.ProductDetailVo;
import com.byrski.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product")
@Slf4j
public class ProductController extends AbstractController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/detail")
    public RestBean<ProductDetailVo> getProductDetail(@RequestParam String productId) {
        return handleRequest(productId, log, new ExecuteCallbackWithResult<>() {
            @Override
            public ProductDetailVo doInTransactionWithResult(String productId) {
                return productService.getProductDetail(productId);
            }
        });
    }
}
