package nl.ing.lovebird.errorhandling;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@Schema(name = "Error", description = "Describes an error in the system")
@ApiModel(value = "Error", description = "Describes an error in the system")
public class ErrorDTO {
    @Schema(description = "The error's reference code. Use it when contacting the support team.")
    @ApiModelProperty("The error's reference code. Use it when contacting the support team.")
    private String code;
    @Schema(description = "A description of the error.  These messages are for informational purposes only, are subject to change, and therefore should not be used programmatically.")
    @ApiModelProperty("A description of the error.  These messages are for informational purposes only, are subject to change, and therefore should not be used programmatically.")
    private String message;
}
