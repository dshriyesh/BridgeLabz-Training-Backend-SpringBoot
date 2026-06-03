package com.fundoonotes.service;

import com.fundoonotes.dto.request.LabelRequest;
import com.fundoonotes.dto.response.LabelResponse;

import java.util.List;

public interface LabelService {
    LabelResponse create(LabelRequest request);
    LabelResponse update(Long id, LabelRequest request);
    void delete(Long id);
    List<LabelResponse> list();
}
