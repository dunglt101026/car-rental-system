package com.hsf.fucarrenting.review.service;

import com.hsf.fucarrenting.common.exception.BusinessException;
import com.hsf.fucarrenting.common.exception.ResourceNotFoundException;
import com.hsf.fucarrenting.rental.dto.RentalResponse;
import com.hsf.fucarrenting.rental.entity.CarRental;
import com.hsf.fucarrenting.rental.mapper.RentalMapper;
import com.hsf.fucarrenting.rental.repository.CarRentalRepository;
import com.hsf.fucarrenting.rental.entity.RentalStatus;
import com.hsf.fucarrenting.review.entity.Review;
import com.hsf.fucarrenting.review.dto.ReviewCreateRequest;
import com.hsf.fucarrenting.review.dto.ReviewResponse;
import com.hsf.fucarrenting.review.mapper.ReviewMapper;
import com.hsf.fucarrenting.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final CarRentalRepository carRentalRepository;
    private final ReviewMapper reviewMapper;
    private final RentalMapper rentalMapper;

    /**
     * Returns the rental shown on the review form, after the same checks as {@link #createReview}.
     */
    @Override
    public RentalResponse getReviewableRental(Integer customerId, Integer carRentId) {
        return rentalMapper.toResponse(loadReviewableRental(customerId, carRentId));
    }

    @Transactional
    @Override
    public ReviewResponse createReview(Integer customerId, ReviewCreateRequest request) {
        CarRental rental = loadReviewableRental(customerId, request.carRentId());

        if (request.reviewStar() == null || request.reviewStar() < 1 || request.reviewStar() > 5) {
            throw new BusinessException("Số sao đánh giá phải từ 1 đến 5 sao.");
        }

        if (request.comment() == null || request.comment().trim().isEmpty()) {
            throw new BusinessException("Nội dung đánh giá không được để trống.");
        }

        Review review = Review.builder()
                .carRental(rental)
                .reviewStar(request.reviewStar())
                .comment(request.comment().trim())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created: id={}, rentalId={}, star={}", saved.getId(), rental.getCarRentId(), saved.getReviewStar());

        return reviewMapper.toResponse(saved);
    }

    private CarRental loadReviewableRental(Integer customerId, Integer carRentId) {
        CarRental rental = carRentalRepository.findWithDetailsByCarRentId(carRentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn thuê xe với mã: " + carRentId));

        if (!rental.getCustomer().getCustomerId().equals(customerId)) {
            throw new BusinessException("Bạn không có quyền đánh giá đơn thuê xe này.");
        }

        if (rental.getStatus() != RentalStatus.Completed) {
            throw new BusinessException("Chỉ đơn thuê xe ở trạng thái 'Completed' mới có thể gửi đánh giá.");
        }

        if (reviewRepository.existsByCarRental_CarRentId(carRentId)) {
            throw new BusinessException("Đơn thuê xe này đã được gửi đánh giá trước đó.");
        }
        return rental;
    }
}

