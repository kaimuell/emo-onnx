package com.kaimueller_code.emo_onnx.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.nio.file.Files;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;


@SpringBootTest
@AutoConfigureMockMvc
class BitmapControllerTest {

    @Autowired
    MockMvc mockMvc;

@Test
        public void testFileUpload() throws Exception {
    ClassPathResource resource = new ClassPathResource("test_b.jpg");
    File file = resource.getFile();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", file.getName(), "image/jpeg", Files.readAllBytes(file.toPath()));
    // Send the POST request with the image file
        mockMvc.perform(multipart("/emotion").file(mockMultipartFile))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("Fröhlich"));
}
}