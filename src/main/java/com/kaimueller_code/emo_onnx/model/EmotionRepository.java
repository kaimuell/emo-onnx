package com.kaimueller_code.emo_onnx.model;


import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface EmotionRepository extends CrudRepository<EmotionEntity, UUID> {
}
