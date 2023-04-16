package org.networking_project.models;

import com.google.errorprone.annotations.NoAllocation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HighestGrowthRateResponse {
    private String state;
    private String expense;
}
