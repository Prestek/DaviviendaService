package com.prestek.davivienda.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateApplicationRequest {
    private Double amount;
    private String userId;
}
