package com.example.rental.controller;

//import com.example.rental.entity.PartnerPost;
import com.example.rental.repository.PartnerPostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Optional;

import static org.mockito.Mockito.when;

@DisplayName("ManagementPartnerPostController tests")
class ManagementPartnerPostControllerTest extends AbstractIntegrationTest {

    // CHÚ Ý: Mock Repository chứ không phải Service
    @MockitoBean
    private PartnerPostRepository partnerPostRepository;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getById_notFound_shouldReturn404() throws Exception {
        Long fakeId = 99L;

        // Bắt buộc phải trả về Optional.empty()
        when(partnerPostRepository.findById(fakeId)).thenReturn(Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/management/partner-posts/" + fakeId))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound()); // Kỳ vọng 404
    }
}
