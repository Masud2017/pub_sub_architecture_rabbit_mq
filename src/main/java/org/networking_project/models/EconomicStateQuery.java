package org.networking_project.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class EconomicStateQuery {
    private String year;
    private String length;
    private String type;
}
