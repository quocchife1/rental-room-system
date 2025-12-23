package com.example.rental.dto.room;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomImageResponse {
    private Long id;
    private String imageUrl;
    private boolean isThumbnail;
}