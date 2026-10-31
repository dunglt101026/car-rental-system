package com.hsf.fucarrenting.review;

import org.springframework.transaction.annotation.Transactional;

public interface ReviewService {
    @Transactional
    ReviewResponse createReview(Integer customerId, ReviewCreateRequest request);
}
