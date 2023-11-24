package es.amplia.oda.core.commons.utils.operation.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Operation<T> {
    private Request<T> request;
}
