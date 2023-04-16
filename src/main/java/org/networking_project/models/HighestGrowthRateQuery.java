package org.networking_project.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class HighestGrowthRateQuery {
    private List<String> years;
    private String type;
    private String length;
}
