package org.networking_project.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EconomicStateResponse {
    private String year;
    private String length;
    private String type;
    private String expense;
}
