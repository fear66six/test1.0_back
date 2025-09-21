package com.byrski.service;

import com.byrski.domain.entity.vo.response.ProductDetailVo;

public interface ProductService {

    ProductDetailVo getProductDetail(String productId);
}
