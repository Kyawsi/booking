package com.booking.system.entity.response;


import lombok.*;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListResponse {
    private List items;
    private long totalRecords;

    public static <T> ListResponse makeListResponse(List<T> items, long totalRecords) {
        return new ListResponse(items, totalRecords);
    }
}