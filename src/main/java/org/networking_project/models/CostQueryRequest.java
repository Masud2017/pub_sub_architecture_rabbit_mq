package org.networking_project.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CostQueryRequest {
        private String year;
        private String state;
        private String type;
        private String length;
        private String expense;
}
