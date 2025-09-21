package com.byrski.infrastructure.repository;

import com.byrski.domain.entity.dto.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findProductsByActivityId(Long activityId);

    List<Product> findProductsBySnowfieldId(Long snowfieldId);
}
