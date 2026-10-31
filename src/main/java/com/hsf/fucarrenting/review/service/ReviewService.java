package com.hsf.fucarrenting.review.service;

import com.hsf.fucarrenting.review.dto.ReviewCreateRequest;
import com.hsf.fucarrenting.review.dto.ReviewResponse;
import org.springframework.transaction.annotation.Transactional;

public interface ReviewService {
    @Transactional
    ReviewResponse createReview(Integer customerId, ReviewCreateRequest request);
}
